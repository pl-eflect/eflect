package eflect;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import eflect.data.*;

/** Manages a system that collects and processes data through a user API. */
public final class EflectProfiler {
  // temporary factories
  private static List<Supplier<Sample>> createSources() {
    return List.of(TaskSample::new, CpuSample::new, RaplSample::new);
  }

  private static String threadName(Class cls) {
    final AtomicInteger counter = new AtomicInteger();
    return String.join("-",
      "eflect",
      String.format("%02d", counter.getAndIncrement()),
      cls.getSimpleName());
  }

  private static ScheduledExecutorService createExecutor(int size) {
    return newScheduledThreadPool(
      size,
      r -> new Thread(
        r,
        threadName(r.getClass())));
  }

  private boolean isRunning = false;
  private List<Supplier<Sample>> sources;
  private EflectProcessor processor;
  private ScheduledExecutorService executor;
  private Duration period;

  public EflectProfiler() { }

  /**
   * Starts running the profiler, which feeds the output of the data sources
   * into the processor. All sources are sampled based on the sampler.
   *
   * NOTE: the profiler will ignore this call if it is already running.
   */
  public void start() {
    if (!isRunning) {
      period = Duration.ofMillis(Integer.parseInt(System.getProperty("eflect.sampling.rate", "41")));
      sources = createSources();
      processor = new EflectProcessor();
      executor = createExecutor(sources.size());
      for (Supplier<Sample> source: sources) {
        executor.execute(() -> runAndReschedule(() -> processor.add(source.get())));
      }
      isRunning = true;
    }
  }

  /**
   * Stops running the profiler by canceling all scheduled tasks and dumping
   * the stored data.
   *
   * NOTE: the profiler will ignore this call if it is not running.
   */
  public Iterable<EnergyFootprint> stop() {
    if (isRunning) {
      executor.shutdown();
      Iterable<EnergyFootprint> result = dump();

      sources = null;
      processor = null;
      executor = null;

      return result;
    } else {
      return List.of();
    }
  }

  public Iterable<EnergyFootprint> dump() {
    return processor.process();
  }

  /** Runs the workload and then schedules it to run at the next period start. */
  private void runAndReschedule(Runnable r) {
    Instant start = Instant.now();
    r.run();
    Duration rescheduleTime = period.minus(Duration.between(start, Instant.now()));

    if (rescheduleTime.toMillis() > 0) {
      executor.schedule(() -> runAndReschedule(r), rescheduleTime.toMillis(), MILLISECONDS);
    } else if (rescheduleTime.toNanos() > 0) {
      executor.schedule(() -> runAndReschedule(r), rescheduleTime.toNanos(), NANOSECONDS);
    } else {
      executor.execute(() -> runAndReschedule(r));
    }
  }

  private static Iterable<EnergyFootprint> profile(Runnable workload) {
    EflectProfiler profiler = new EflectProfiler();
    profiler.start();
    workload.run();
    return profiler.stop();
  }

  private static double sum(Iterable<EnergyFootprint> profiles) {
    double sum = 0;
    int count = 0;
    for (EnergyFootprint profile: profiles) {
      sum += profile.getEnergy();
      count++;
    }
    return sum;
  }

  public static void main(String[] args) throws Exception {
    Runnable workload = () -> {
      try {
        Thread.sleep(10000);
      } catch (Exception e) { }
    };
    int iters = 6;

    for (int i = 0; i < iters; i++) {
      System.out.println("workload " + workload.getClass().getSimpleName() + " consumed " + sum(profile(workload)) + "J");
    }
  }
}
