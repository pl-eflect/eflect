package eflect;

import static eflect.util.LoggerUtil.getLogger;
import static eflect.util.WriterUtil.writeCsv;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.data.EnergyFootprint;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/** A wrapper around {@link EflectCollector} that also monitors runtime stats and calmness. */
public final class Eflect {
  private static final Logger logger = getLogger();
  private static final AtomicInteger counter = new AtomicInteger();
  private static final ThreadFactory threadFactory =
      r -> {
        Thread t = new Thread(r, "eflect-" + counter.getAndIncrement());
        t.setDaemon(true);
        return t;
      };
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String FOOTPRINT_HEADER =
      "id,name,start,end,domain,app_energy,total_energy,trace";

  private static Eflect instance;

  /** Creates an instance of the underlying class if it hasn't been created yet. */
  public static synchronized Eflect getInstance() {
    if (instance == null) {
      instance = new Eflect();
    }
    return instance;
  }

  private final int mergeAttempts;
  private final String outputPath;
  private final long periodMillis;
  private final Instant[] time = new Instant[2];
  private final double[][][] energy = new double[2][][];

  private ScheduledExecutorService executor;
  private EflectCollector eflect;
  private Collection<EnergyFootprint> footprints;

  private Eflect() {
    this.mergeAttempts = Integer.parseInt(System.getProperty("eflect.attempts", "100"));
    this.outputPath = System.getProperty("eflect.output", ".");
    this.periodMillis = Long.parseLong(System.getProperty("eflect.period", "64"));
  }

  /**
   * Creates and starts instances of eflect and the calmness monitor.
   *
   * <p>If there is no existing executor, a new thread pool is spun-up.
   *
   * <p>If the period is 0, an eflect will not be created.
   */
  public void start() {
    start(periodMillis);
  }

  /** Starts up eflect if needed. */
  public void start(long periodMillis) {
    logger.info("starting eflect");
    if (executor == null) {
      executor = newScheduledThreadPool(5, threadFactory);
    }
    footprints = new ArrayList<>();
    Duration period = Duration.ofMillis(periodMillis);
    eflect = new EflectCollector(mergeAttempts, executor, period);
    eflect.start();
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
  public void dump() {
    File dataDirectory = getOutputDirectory();
    writeCsv(dataDirectory.getPath(), "footprint.csv", FOOTPRINT_HEADER, footprints);
  }

  public void dump(String tag) {
    File dataDirectory = getOutputDirectory();
    writeCsv(dataDirectory.getPath(), "footprint-" + tag + ".csv", FOOTPRINT_HEADER, footprints);
  }

  /** Shutdown the executor. */
  public void shutdown() {
    executor.shutdown();
    executor = null;
  }

  private File getOutputDirectory() {
    File outputDir = new File(outputPath);
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
    return outputDir;
  }
}
