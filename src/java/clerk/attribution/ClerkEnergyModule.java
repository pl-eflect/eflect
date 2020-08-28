package clerk.attribution;

import chappie.attribution.AttributionSampler;
import chappie.attribution.EnergyAttributer;
import chappie.profiling.Sampler;
import chappie.profiling.SamplingRate;
import chappie.sampling.energy.EnergySampler;
import clerk.sampling.tasks.TasksSampler;
import clerk.sampling.cpu.CPUSampler;
import dagger.Module;
import dagger.Provides;
import java.time.Duration;
import java.util.Set;

/** Module to provide jiffies-level energy attribution (jRAPL only). */
@Module
public interface ClerkEnergyModule {
  @Provides
  static EnergyAttributer provideAttributer() {
    return new ClerkEnergyAttributer();
  }

  @Provides
  @SamplingRate
  static Duration provideSamplingRate() {
    return Duration.ofMillis(Long.parseLong(System.getProperty("clerk.rate", "4")));
  }

  @Provides
  @AttributionSampler
  static Set<Sampler> provideAttributionSamplers() {
    return Set.of(new EnergySampler(), new CPUSampler(), new TasksSampler());
  }
}
