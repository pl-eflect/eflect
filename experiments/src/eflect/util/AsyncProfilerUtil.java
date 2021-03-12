package eflect.util;

import java.time.Duration;
import java.time.Instant;
import one.profiler.AsyncProfiler;
import one.profiler.Events;

/** Utility wrapper around the async-profiler to start and stop it as needed. */
public final class AsyncProfilerUtil {
  private static final Duration asyncPeriod =
      Duration.ofMillis(Integer.parseInt(System.getProperty("eflect.async.period", "2")));
  private static final Duration asyncCollectionPeriod =
      Duration.ofMillis(
          Integer.parseInt(System.getProperty("eflect.async.collection.period", "500")));

  private static boolean asyncRunning = false;
  private static Instant last = Instant.now();

  public static synchronized String readAsyncProfiler() {
    Instant now = Instant.now();
    if (Duration.between(last, now).toMillis() > asyncCollectionPeriod.toMillis()) {
      if (!asyncRunning) {
        AsyncProfiler.getInstance().start(Events.CPU, asyncPeriod.getNano());
        asyncRunning = true;
      }
      AsyncProfiler.getInstance().stop();
      String traces = AsyncProfiler.getInstance().dumpRecords();
      AsyncProfiler.getInstance().resume(Events.CPU, asyncPeriod.getNano());
      last = now;
      return traces;
    }
    return "0,-1,dummy\n";
  }

  private AsyncProfilerUtil() {}
}
