package eflect.util;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Logger;

/** Utility to access data from /proc */
public class ProcUtil {
  // /proc/pid/task
  private static final long PID = ProcessHandle.current().pid();
  private static final Logger logger = LoggerUtil.getLogger();

  /** Reads this application's thread's jiffies stat files. */
  public static ArrayList<String> readTaskStats() {
    ArrayList<String> stats = new ArrayList<String>();
    File tasks = new File(String.join(File.separator, "/proc", Long.toString(PID), "task"));
    for (File task : tasks.listFiles()) {
      File statFile = new File(task, "stat");
      if (!statFile.exists()) {
        continue;
      }
      try {
        stats.add(Files.readString(Path.of(statFile.getPath())));
      } catch (Exception e) {
        logger.info("unable to read task " + statFile);
      }
    }
    return stats;
  }

  // /proc/stat
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String SYSTEM_STAT_FILE = String.join(File.separator, "/proc", "stat");

  /** Reads the jiffies by cpu from /proc/stat. */
  public static String[] readProcStat() {
    String[] stats = new String[CPU_COUNT];
    try (BufferedReader reader = Files.newBufferedReader(Path.of(SYSTEM_STAT_FILE))) {
      reader.readLine(); // first line is total summary; we need by cpu
      for (int i = 0; i < CPU_COUNT; i++) {
        stats[i] = reader.readLine();
      }
    } catch (Exception e) {
      logger.info("unable to read " + SYSTEM_STAT_FILE);
      e.printStackTrace();
    } finally {
      return stats;
    }
  }

  private ProcUtil() {}
}
