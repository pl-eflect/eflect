package eflect.data;

import java.time.Instant;

/** Sample of consumed global energy. */
public final class EnergySample implements Sample {
  private final Instant timestamp;
  private final double[][] stats;

  public EnergySample(Instant timestamp, double[][] stats) {
    this.timestamp = timestamp;
    this.stats = stats;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  /** Returns the energy broken down by domain and component. */
  public double[][] getEnergy() {
    return stats;
  }
}
