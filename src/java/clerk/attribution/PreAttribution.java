package clerk.attribution;

import static jrapl.util.EnergyCheckUtils.SOCKETS;

import chappie.sampling.energy.EnergySample;
import chappie.profiling.MergableSample;
import chappie.profiling.Sample;
import chappie.profiling.SampleProcessor;
import chappie.profiling.TimestampedSample;
import chappie.util.TimeUtil;
import clerk.sampling.cpu.CPUSample;
import clerk.sampling.tasks.TasksSample;
import java.time.Instant;

final class PreAttribution implements SampleProcessor<ClerkEnergyAttribution> {
  private Instant start = Instant.MAX;
  private Instant end = Instant.MIN;
  private EnergySample energy = EnergySample.EMPTY;
  private CPUSample cpu = CPUSample.EMPTY;
  private TasksSample tasks = TasksSample.EMPTY;

  @Override
  public void add(Sample s) {
    if (s instanceof EnergySample) {
      this.energy = ((EnergySample) s).merge(this.energy);
    } else if (s instanceof TasksSample) {
      this.tasks = ((TasksSample) s).merge(this.tasks);
    } else if (s instanceof CPUSample) {
      this.cpu = ((CPUSample) s).merge(this.cpu);
    } else {
      return; // prevents other timestamped from touching
    }

    Instant timestamp = ((TimestampedSample) s).getTimestamp();
    start = TimeUtil.min(timestamp, start);
    end = TimeUtil.maxBelowUpper(timestamp, end);
  }

  @Override
  public ClerkEnergyAttribution process() {
    return new ClerkEnergyAttribution(start, end, energy, tasks, cpu);
  }

  @Override
  public String toString() {
    return String.join("\n",
      start.toString() + "->" + end.toString(),
      energy.toString(),
      tasks.toString(),
      cpu.toString());
  }

  PreAttribution merge(PreAttribution other) {
    PreAttribution merged = new PreAttribution();
    // TODO(timur): these work for the wrong reason. need to decide on generic
    // merge...
    merged.add(other.energy.merge(this.energy));
    merged.add(other.tasks.merge(this.tasks));
    merged.add(other.cpu.merge(this.cpu));
    return merged;
  }

  // there is an error somewhere in here; it's letting through samples
  // that have invalid values.
  boolean valid() {
    if (!attributable()) {
      return false;
    }

    int energyConsumed = 0;
    for (int socket = 0; socket < SOCKETS; socket++) {
      energyConsumed += energy.getEnergy(socket);
    }

    if (energyConsumed == 0) {
      return false;
    }

    int sysJiffies = 0;
    int appJiffies = 0;
    for (int socket = 0; socket < SOCKETS; socket++) {
      if (tasks.getJiffies(socket) > cpu.getJiffies(socket)) {
        return false;
      }
    }

    return true;
  }

  boolean attributable() {
    if (TimeUtil.equal(energy.getTimestamp(), Instant.MAX) ||
      TimeUtil.equal(tasks.getTimestamp(), Instant.MAX) ||
      TimeUtil.equal(cpu.getTimestamp(), Instant.MAX)) {
      return false;
    }

    int sysJiffies = 0;
    for (int socket = 0; socket < SOCKETS; socket++) {
      sysJiffies += cpu.getJiffies(socket);
    }

    if (sysJiffies == 0) {
      return false;
    }

    return true;
  }

  Instant getTimestamp() {
    return end;
  }
}
