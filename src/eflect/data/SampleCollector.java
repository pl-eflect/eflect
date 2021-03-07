package eflect.data;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/** A clerk that collects data at a fixed period. */
public class SampleCollector<O> {
  private final ArrayList<Future<?>> futures = new ArrayList<>();
  private final Iterable<Supplier<Sample>> sources;
  private final SampleProcessor<O> processor;
  private final ScheduledExecutorService executor;
  private final Duration period;

  private boolean isRunning = false;

  public SampleCollector(
      Collection<Supplier<Sample>> sources,
      SampleProcessor<O> processor,
      ScheduledExecutorService executor,
      Duration period) {
    this.sources = sources;
    this.processor = processor;
    this.executor = executor;
    this.period = period;
  }

  /** Pipes data into the processor. */
  public final void start() {
    if (!isRunning) {
      stopFutures();
      startCollecting();
      isRunning = true;
    }
  }

  /** Stops piping data into the processor. */
  public final void stop() {
    if (isRunning) {
      stopFutures();
      isRunning = false;
    }
  }

  /** Return the processor's output. */
  public final O read() {
    return processor.process();
  }

  private void startCollecting() {
    for (Supplier<Sample> source : sources) {
      addFuture(executor.submit(() -> runAndReschedule(() -> processor.add(source.get()))));
    }
  }

  private void addFuture(Future<?> future) {
    synchronized (futures) {
      futures.add(future);
    }
  }

  private void stopFutures() {
    synchronized (futures) {
      // make sure the previous futures are done or cancelled
      for (Future<?> future : futures) {
        // attempt to cancel the future; if we can't, get the result safely
        if (!future.isDone() && !future.isCancelled() && !future.cancel(false)) {
          try {
            future.get();
          } catch (Exception e) {
            System.out.println("could not consume a future");
            e.printStackTrace();
          }
        }
      }
      futures.clear();
    }
  }

  private void runAndReschedule(Runnable r) {
    if (!isRunning) {
      return;
    }

    Instant start = Instant.now();
    r.run();
    Duration rescheduleTime = period.minus(Duration.between(start, Instant.now()));

    if (rescheduleTime.toNanos() > 0) {
      addFuture(
          executor.schedule(() -> runAndReschedule(r), rescheduleTime.toNanos(), NANOSECONDS));
    } else {
      addFuture(executor.submit(() -> runAndReschedule(r)));
    }
  }
}
