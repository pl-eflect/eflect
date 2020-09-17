package eflect;

import java.time.Duration;
import java.time.Instant;

/** Representation of the application's energy usage over a time interval. */
// TODO: this can't be usefully exchanged; look into a proto?
public final class EnergyFootprint {
  private final Instant start;
  private final Instant end;
  private final Duration duration;
  private final double[] applicationEnergy;
  private final double[] totalEnergy;

  EnergyFootprint(Instant start, Instant end, double[] applicationEnergy, double[] totalEnergy) {
    this.start = start;
    this.end = end;
    this.duration = Duration.between(start, end);

    this.applicationEnergy = applicationEnergy;
    this.totalEnergy = totalEnergy;
  }

  public Instant getStart() {
    return start;
  }

  public Instant getEnd() {
    return end;
  }

  public double getEnergy() {
    double energy = 0;
    for (int i = 0; i < this.applicationEnergy.length; i++) {
      energy += this.applicationEnergy[i];
    }
    return energy;
  }

  public double getEnergy(int socket) {
    return this.applicationEnergy[socket];
  }

  public double getPower() {
    return getEnergy() / duration.toMillis() / 1000;
  }

  public double getPower(int socket) {
    return getEnergy(socket) / duration.toMillis() / 1000;
  }

  @Override
  public String toString() {
    return start.toString() + " -> " + end.toString() + ": " + getEnergy() + " / " + getTotalEnergy();
  }

  private double getTotalEnergy() {
    double energy = 0;
    for (int i = 0; i < this.totalEnergy.length; i++) {
      energy += this.totalEnergy[i];
    }
    return energy;
  }
}
