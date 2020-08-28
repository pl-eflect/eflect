package clerk.attribution;

import static java.lang.Math.max;
import static jrapl.util.EnergyCheckUtils.SOCKETS;

import chappie.attribution.EnergyAttribution;
import chappie.sampling.energy.EnergySample;
import clerk.sampling.cpu.CPUSample;
import clerk.sampling.tasks.TasksSample;
import java.time.Instant;

/**
* Representation of the application's power usage. Included is the total energy
* consumed, the energy attributed to the application, and the amount of energy
* each task in the attribution should be assigned.
*/
public final class ClerkEnergyAttribution implements EnergyAttribution {
  private final Instant start;
  private final Instant end;
  private final double[] attributed = new double[SOCKETS];
  private final double[] total = new double[SOCKETS];

  private final long[] appJiff = new long[SOCKETS];
  private final long[] totalJiff = new long[SOCKETS];

  ClerkEnergyAttribution(
      Instant start,
      Instant end,
      EnergySample energy,
      TasksSample tasks,
      CPUSample cpu) {
    this.start = start;
    this.end = end;
    for (int socket = 0; socket < SOCKETS; socket++) {
      // assign the energy
      // TODO(timur): need to figure out why negative values have made it to
      // here; i'm guessing some of my defaults are leaking through
      total[socket] = max(energy.getEnergy(socket), 0);
      appJiff[socket] = tasks.getJiffies(socket);
      totalJiff[socket] = cpu.getJiffies(socket);

      // compute the attribution factor
      double factor = 0;
      if (cpu.getJiffies(socket) > 0) {
        factor = (double)(Math.min(tasks.getJiffies(socket), cpu.getJiffies(socket))) / cpu.getJiffies(socket);
      }
      attributed[socket] = max(factor * total[socket], 0);
    }
  }

  @Override
  public double getApplicationEnergy() {
    double energy = 0;
    for (int i = 0; i < SOCKETS; i++) {
      energy += this.attributed[i];
    }
    return energy;
  }

  @Override
  public double getTotalEnergy() {
    double energy = 0;
    for (int i = 0; i < SOCKETS; i++) {
      energy += this.total[i];
    }
    return energy;
  }

  @Override
  public Instant getStart() {
    return start;
  }

  @Override
  public Instant getEnd() {
    return end;
  }

  @Override
  public String toString() {
    String[] attribution = new String[SOCKETS];
    for (int socket = 0; socket < SOCKETS; socket++) {
      attribution[socket] = String.join(",",
        Long.toString(start.toEpochMilli()),
        Long.toString(end.toEpochMilli()),
        Integer.toString(socket + 1),
        Double.toString(appJiff[socket]),
        Double.toString(totalJiff[socket]),
        Double.toString(attributed[socket]),
        Double.toString(total[socket]));
    }
    return String.join(System.lineSeparator(), attribution);
  }
}
