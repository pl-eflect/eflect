package eflect;

import static eflect.util.LoggerUtil.getLogger;
import static eflect.util.WriterUtil.writeCsv;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.data.EnergyFootprint;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/** A singleton wrapper around {@link EflectCollector} that manages the machinery. */
public final class Eflect {
  private static final Logger logger = getLogger();
  private static final String FOOTPRINT_HEADER =
      "id,name,start,end,domain,app_energy,total_energy,trace";

  private static Eflect instance;

  /** Creates an instance of the underlying class if it hasn't been created yet, then returns it. */
  public static synchronized Eflect getInstance() {
    if (instance == null) {
      instance = new Eflect();
    }
    return instance;
  }

  private final AtomicInteger counter = new AtomicInteger();
  private final ThreadFactory threadFactory =
      r -> {
        Thread t = new Thread(r, "eflect-" + counter.getAndIncrement());
        t.setDaemon(true);
        return t;
      };

  private final int mergeAttempts;
  private final String outputPath;
  private final long periodMillis;

  private ScheduledExecutorService executor;
  private EflectCollector eflect;
  private Collection<EnergyFootprint> footprints;
  private boolean isRunning;

  private Eflect() {
    this.mergeAttempts = Integer.parseInt(System.getProperty("eflect.attempts", "100"));
    this.outputPath = System.getProperty("eflect.output", ".");
    this.periodMillis = Long.parseLong(System.getProperty("eflect.period", "64"));
  }

  /**
   * Creates and starts a new instance of {@link EflectCollector}.
   *
   * <p>If there is no existing executor, a new thread pool is spun-up.
   */
  public void start(long periodMillis) {
    if (executor == null) {
      executor = newScheduledThreadPool(3, threadFactory);
    }
    logger.info("starting eflect");
    footprints = new ArrayList<>();
    eflect = new EflectCollector(mergeAttempts, executor, Duration.ofMillis(periodMillis));
    eflect.start();
  }

  /** Starts eflect with an environment defined default period. */
  public void start() {
    start(periodMillis);
  }

  /** Stops any running collectors. */
  public void stop() {
    eflect.stop();
    logger.info("stopped eflect");
    footprints = eflect.read();
  }

  /** Returns the last data produced by {@code stop}. */
  public Collection<EnergyFootprint> read() {
    return footprints;
  }

  /** Writes the footprints as a csv. */
  public void dump(String outputDirName, String fileName) {
    writeCsv(checkOutputDir(outputDirName), fileName, FOOTPRINT_HEADER, footprints);
  }

  public void dump() {
    dump(outputPath, "footprint.csv");
  }

  public void dump(String tag) {
    dump(outputPath, "footprint" + tag + ".csv");
  }

  /** Shutdown the executor. */
  public void shutdown() {
    executor.shutdown();
    executor = null;
  }

  private String checkOutputDir(String outputPath) {
    File outputDir = new File(outputPath);
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
    return outputDir.getPath();
  }
}
