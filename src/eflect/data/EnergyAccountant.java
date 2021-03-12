package eflect.data;

import eflect.util.TimeUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/** Accountant that merges samples into {@link EnergyFootprint}s. */
public final class EnergyAccountant implements Accountant<Collection<EnergyFootprint>> {
  private final int domainCount;
  private final int componentCount;
  private final double wrapAround;
  private final Accountant<Collection<ThreadActivity>> activityAccountant;
  private final double[][] energyMin;
  private final double[][] energyMax;

  private Instant start = Instant.MAX;
  private Instant end = Instant.MIN;

  public EnergyAccountant(
      int domainCount,
      int componentCount,
      double wrapAround,
      Accountant<Collection<ThreadActivity>> activityAccountant) {
    this.domainCount = domainCount;
    this.componentCount = componentCount;
    this.wrapAround = wrapAround;
    this.activityAccountant = activityAccountant;
    energyMin = new double[domainCount][componentCount];
    energyMax = new double[domainCount][componentCount];
    for (int domain = 0; domain < domainCount; domain++) {
      Arrays.fill(energyMin[domain], -1);
      Arrays.fill(energyMax[domain], -1);
    }
  }

  /** Put the sample data into the correct container. */
  @Override
  public void add(Sample s) {
    if (s instanceof EnergySample) {
      addEnergy(((EnergySample) s).getEnergy());
    } else {
      activityAccountant.add(s);
    }
    // need to know the range of time for samples
    Instant timestamp = s.getTimestamp();
    start = TimeUtil.min(timestamp, start);
    end = TimeUtil.max(timestamp, end);
  }

  /** Add all samples from the other accountant if it is a {@link JiffiesAccountant}. */
  @Override
  public <T extends Accountant<Collection<EnergyFootprint>>> void add(T o) {
    if (o instanceof EnergyAccountant) {
      EnergyAccountant other = (EnergyAccountant) o;
      addEnergy(other.energyMin);
      addEnergy(other.energyMax);
      activityAccountant.add(other.activityAccountant);
      start = TimeUtil.min(start, other.start);
      end = TimeUtil.max(end, other.end);
    }
  }

  /**
   * Attempts to account the stored data.
   *
   * <p>Returns Result.UNACCOUNTABLE if a domain has no energy.
   *
   * <p>Returns the result of the {@link ActivityAccountant} otherwise.
   */
  @Override
  public Accountant.Result account() {
    // check the timestamps
    if (TimeUtil.equal(start, end)
        || TimeUtil.equal(start, Instant.MAX)
        || TimeUtil.equal(end, Instant.MIN)) {
      return Accountant.Result.UNACCOUNTABLE;
    }

    // check the energy
    synchronized (energyMin) {
      for (int domain = 0; domain < domainCount; domain++) {
        for (int component = 0; component < componentCount; component++) {
          if (energyMax[domain][component] < 0 || energyMin[domain][component] < 0) {
            return Accountant.Result.UNACCOUNTABLE;
          }
        }
      }
    }

    return activityAccountant.account();
  }

  /** Returns the data if it's accountable. Otherwise, return an empty list. */
  @Override
  public Collection<EnergyFootprint> process() {
    if (account() != Accountant.Result.UNACCOUNTABLE) {
      ArrayList<EnergyFootprint> footprints = new ArrayList<>();
      double[] energy = new double[domainCount];
      synchronized (energyMin) {
        for (int domain = 0; domain < domainCount; domain++) {
          for (int component = 0; component < componentCount; component++) {
            double componentEnergy = energyMax[domain][component] - energyMin[domain][component];
            energy[domain] += componentEnergy;
          }
        }
        for (int domain = 0; domain < domainCount; domain++) {
          if (energy[domain] < 0) {
            energy[domain] += wrapAround;
          }
        }
      }
      for (ThreadActivity thread : activityAccountant.process()) {
        double taskEnergy = thread.activity * energy[thread.domain];
        footprints.add(
            new EnergyFootprint.Builder()
                .setId(thread.id)
                .setName(thread.name)
                .setStart(start)
                .setEnd(end)
                .setDomain(thread.domain)
                .setEnergy(taskEnergy)
                .setTotalEnergy(energy[thread.domain])
                .build());
      }
      return footprints;
    } else {
      return new ArrayList<EnergyFootprint>();
    }
  }

  /** Sets the min values to the max values. */
  @Override
  public void discardStart() {
    start = end;
    synchronized (energyMin) {
      for (int domain = 0; domain < domainCount; domain++) {
        for (int component = 0; component < componentCount; component++) {
          energyMin[domain][component] = energyMax[domain][component];
        }
      }
    }
    activityAccountant.discardStart();
  }

  /** Sets the max values to the min values. */
  @Override
  public void discardEnd() {
    end = start;
    synchronized (energyMin) {
      for (int domain = 0; domain < domainCount; domain++) {
        for (int component = 0; component < componentCount; component++) {
          energyMax[domain][component] = energyMin[domain][component];
        }
      }
    }
    activityAccountant.discardEnd();
  }

  private void addEnergy(double[][] energy) {
    for (int domain = 0; domain < domainCount; domain++) {
      for (int component = 0; component < componentCount; component++) {
        double componentEnergy = energy[domain][component];
        if (componentEnergy < 0) {
          continue;
        }
        synchronized (energyMin) {
          if (energyMin[domain][component] < 0 || componentEnergy < energyMin[domain][component]) {
            energyMin[domain][component] = componentEnergy;
          }
          if (componentEnergy > energyMax[domain][component]) {
            energyMax[domain][component] = componentEnergy;
          }
        }
      }
    }
  }
}
