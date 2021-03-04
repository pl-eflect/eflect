package eflect.data.jiffies;

import eflect.data.Sample;
import java.time.Instant;

/** A sample of jiffies consumed as reported by /proc/stat. */
public final class ProcStatSample implements Sample {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  // indices for user, nice, system, irq, softirq, steal, guest, guest_nice
  private static final int[] JIFFY_INDICES = new int[] {1, 2, 3, 6, 7, 8, 9, 10};

  private final Instant timestamp;
  private final String[] stats;

  public ProcStatSample(Instant timestamp, String[] stats) {
    this.timestamp = timestamp;
    this.stats = stats;
  }

  /** Return the stored timestamp. */
  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  /** Parse and return the jiffies from the stat strings. */
  public long[] getJiffies() {
    long[] jiffies = new long[CPU_COUNT];
    for (String s : stats) {
      String[] stat = s.split(" ");
      if (stat.length < 11) {
        continue;
      }
      int cpu = Integer.parseInt(stat[0].substring(3));
      for (int i : JIFFY_INDICES) {
        jiffies[cpu] += Long.parseLong(stat[i]);
      }
    }
    return jiffies;
  }
}
