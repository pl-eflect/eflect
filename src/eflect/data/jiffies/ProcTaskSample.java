package eflect.data.jiffies;

import eflect.data.Sample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/** A sample of jiffies consumed as reported by /proc/[pid]/task/[tid]/stat. */
public final class ProcTaskSample implements Sample {
  private static final int STAT_LENGTH = 52;
  private static final int TID_INDEX = 0;
  private static final int CPU_INDEX = 38;
  private static final int[] JIFFY_INDICES = new int[] {13, 14};

  private final Instant timestamp;
  private final Iterable<String> stats;

  public ProcTaskSample(Instant timestamp, Iterable<String> stats) {
    this.timestamp = timestamp;
    this.stats = stats;
  }

  /** Return the stored timestamp. */
  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  /** Parse and return the stat details from the stat strings. */
  public Collection<TaskStat> getTaskStats() {
    ArrayList<TaskStat> taskStats = new ArrayList<>();
    for (String s : stats) {
      String[] stats = s.split(" ");
      if (stats.length < STAT_LENGTH) {
        continue;
      }
      int offset = stats.length - STAT_LENGTH;

      String name = String.join(" ", Arrays.copyOfRange(stats, 1, 2 + offset));
      name = name.substring(1, name.length() - 1);

      long jiffies = 0;
      for (int i : JIFFY_INDICES) {
        jiffies += Long.parseLong(stats[i + offset]);
      }

      taskStats.add(
          new TaskStat(
              Long.parseLong(stats[TID_INDEX]),
              name,
              Integer.parseInt(stats[CPU_INDEX + offset]),
              jiffies));
    }
    return taskStats;
  }
}
