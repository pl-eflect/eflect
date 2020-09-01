package eflect;

import chappie.attribution.AttributionModule;
import chappie.sampling.Sample;
import clerk.Profiler;
import clerk.concurrent.SchedulingModule;
import eflect.attribution.EflectFootprint;
import eflect.attribution.EflectModule;
import dagger.Component;
import java.util.ArrayList;

public class EflectProfiler {
  @Component(modules = {
    SchedulingModule.class,
    EflectModule.class,
    AttributionModule.class
  })
  interface ProfilerFactory {
    Profiler<Sample, Iterable<EflectFootprint>> newProfiler();
  }

  private static final ProfilerFactory profilerFactory = DaggerEflectProfiler_ProfilerFactory.builder().build();

  private static Profiler profiler;
  private static Iterable<EflectFootprint> profiles = new ArrayList<EflectFootprint>();

  // starts a profiler if there is not one
  public static void start() {
    if (profiler == null) {
      profiler = profilerFactory.newProfiler();
      profiler.start();
    }
  }

  // stops the profiler if there is one
  public static void stop() {
    if (profiler != null) {
      profiles = (Iterable<EflectFootprint>) profiler.stop();
      profiler = null;
    }
  }

  // restart the profiler so that we start fresh
  public static Iterable<EflectFootprint> dump() {
    if (profiler != null) {
      stop();
      start();
    }
    return profiles;
  }
}
