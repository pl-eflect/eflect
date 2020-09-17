package eflect.data;

import static jrapl.util.EnergyCheckUtils.SOCKETS;

import java.time.Instant;
import java.util.Arrays;
import jrapl.util.EnergyCheckUtils;

/** Snapshot of the machine's rapl counters. */
public final class RaplSample implements Sample {
  public static double[] readEnergy() {
    double[] energy = new double[SOCKETS];
    double[] stats = EnergyCheckUtils.getEnergyStats();
    for (int i = 0; i < SOCKETS; ++i) {
      energy[i] = stats[3 * i + 1] + stats[3 * i + 2] + stats[3 * i];
    }
    return energy;
  }

  private final double[] energy;
  private final Instant timestamp;

  public RaplSample() {
    this.energy = readEnergy();
    this.timestamp = Instant.now();
  }

  public double[] getEnergy() {
    return energy;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }
}
