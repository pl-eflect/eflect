package eflect.attribution;

import static jrapl.util.EnergyCheckUtils.SOCKETS;

import chappie.sampling.MergableSample;
import chappie.sampling.Sample;
import chappie.sampling.SampleProcessor;
import chappie.sampling.TimestampedSample;
import chappie.sampling.energy.EnergySample;
import chappie.utils.TimeUtils;
import eflect.sampling.cpu.CpuSample;
import eflect.sampling.tasks.TasksSample;
import java.time.Instant;

final class PreAttribution implements SampleProcessor<EflectFootprint> {
  private Instant start = Instant.MAX;
  private Instant end = Instant.MIN;
  private EnergySample energy = EnergySample.EMPTY;
  private CpuSample cpu = CpuSample.EMPTY;
  private TasksSample tasks = TasksSample.EMPTY;

  @Override
  public void add(Sample s) {
    if (s instanceof EnergySample) {
      this.energy = ((EnergySample) s).merge(this.energy);
    } else if (s instanceof TasksSample) {
      this.tasks = ((TasksSample) s).merge(this.tasks);
    } else if (s instanceof CpuSample) {
      this.cpu = ((CpuSample) s).merge(this.cpu);
    } else {
      // prevent other samples from touching this
      return;
    }

    Instant timestamp = ((TimestampedSample) s).getTimestamp();
    start = TimeUtils.min(timestamp, start);
    end = TimeUtils.maxBelowUpper(timestamp, end);
  }

  @Override
  public EflectFootprint process() {
    return new EflectFootprint(start, end, energy, tasks, cpu);
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

    for (int socket = 0; socket < SOCKETS; socket++) {
      if (tasks.getJiffies(socket) > cpu.getJiffies(socket)) {
        return false;
      }
    }

    return true;
  }

  boolean attributable() {
    if (TimeUtils.equal(energy.getTimestamp(), Instant.MAX) ||
      TimeUtils.equal(tasks.getTimestamp(), Instant.MAX) ||
      TimeUtils.equal(cpu.getTimestamp(), Instant.MAX)) {
      return false;
    }

    for (int socket = 0; socket < SOCKETS; socket++) {
      if (cpu.getJiffies(socket) < 0) {
        return false;
      }
    }

    return true;
  }

  Instant getTimestamp() {
    return end;
  }
}
