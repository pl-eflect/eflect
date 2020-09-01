package eflect.attribution;

import static java.lang.Math.max;
import static jrapl.util.EnergyCheckUtils.SOCKETS;

import chappie.attribution.EnergyFootprint;
import chappie.sampling.energy.EnergySample;
import eflect.sampling.cpu.CpuSample;
import eflect.sampling.tasks.TasksSample;
import java.time.Instant;

/**
* Representation of the application's power usage. Included is the total energy
* consumed, the energy attributed to the application, and the amount of energy
* each task in the attribution should be assigned.
*/
public final class EflectFootprint extends EnergyFootprint {
  private final double[] total = new double[SOCKETS];

  EflectFootprint(
      Instant start,
      Instant end,
      EnergySample energy,
      TasksSample tasks,
      CpuSample cpu) {
    super(start, end);
    for (int socket = 0; socket < SOCKETS; socket++) {
      total[socket] = max(energy.getEnergy(socket), 0);

      // compute the attribution factor
      double factor = 0;
      if (cpu.getJiffies(socket) > 0) {
        factor = (double)(Math.min(tasks.getJiffies(socket), cpu.getJiffies(socket))) / cpu.getJiffies(socket);
      }
      this.energy[socket] = max(factor * total[socket], 0);
    }
  }

  public double getFraction() {
    double energy = 0;
    double total = 0;
    for (int i = 0; i < SOCKETS; i++) {
      energy += this.energy[i];
      total += this.total[i];
    }
    return energy / total;
  }

  public double getFraction(int socket) {
    return this.energy[socket] / this.total[socket];
  }

  @Override
  public String toString() {
    String[] attribution = new String[SOCKETS];
    for (int socket = 0; socket < SOCKETS; socket++) {
      attribution[socket] = String.join(",",
        Long.toString(start.toEpochMilli()),
        Long.toString(end.toEpochMilli()),
        Integer.toString(socket + 1),
        Double.toString(energy[socket]));
    }
    return String.join(System.lineSeparator(), attribution);
  }
}
