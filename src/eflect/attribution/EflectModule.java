package eflect.attribution;

import chappie.attribution.AttributionSource;
import chappie.attribution.EnergyAttributer;
import chappie.sampling.Sample;
import chappie.sampling.energy.EnergySampler;
import clerk.DataProcessor;
import eflect.sampling.tasks.TasksSampler;
import eflect.sampling.cpu.CpuSampler;
import dagger.Module;
import dagger.Provides;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/** Module to provide jiffies-level energy attribution (jRAPL only). */
@Module
public interface EflectModule {
  @Provides
  static DataProcessor<Sample, Iterable<EflectFootprint>> provideAttributer() {
    return new EflectAttributer();
  }

  @Provides
  @AttributionSource
  static Iterable<Supplier<Sample>> provideAttributionSamplers() {
    return List.of(new EnergySampler(), new CpuSampler(), new TasksSampler());
  }
}
