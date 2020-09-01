package eflect.sampling.tasks;

import static chappie.utils.TimeUtils.maxBelowUpper;
import static jrapl.util.EnergyCheckUtils.SOCKETS;

import chappie.sampling.MergableSample;
import chappie.sampling.TimestampedSample;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jlibc.proc.Task;

/** Collection of relative task snapshots that can be accessed by socket. */
public class TasksSample implements MergableSample<TasksSample>, TimestampedSample {
  private static final int CORES = Runtime.getRuntime().availableProcessors();
  public static final TasksSample EMPTY = new TasksSample(new long[SOCKETS], Instant.MAX);

  private final Instant timestamp;
  private final long[] jiffies = new long[SOCKETS];

  TasksSample(Map<Integer, Task> first, Map<Integer, Task> second) {
    timestamp = Instant.now();
    // not quite right yet; there's a bit more to do for this
    for (int id: second.keySet()) {
      if (first.containsKey(id)) {
        int socket = (int)(second.get(id).getCPU() / (CORES / SOCKETS)); // not generalized
        jiffies[socket] += second.get(id).getUserJiffies() - first.get(id).getUserJiffies();
        jiffies[socket] += second.get(id).getKernelJiffies() - first.get(id).getKernelJiffies();
      }
    }
  }

  /** private constructor to prevent mutation during merges */
  private TasksSample(long[] jiffies, Instant timestamp) {
    this.timestamp = timestamp;
    for (int socket = 0; socket < SOCKETS; socket++) {
      this.jiffies[socket] = jiffies[socket];
    }
  }

  @Override
  public TasksSample merge(TasksSample other) {
    long[] jiffies = new long[SOCKETS];
    long[] applicationJiffies = new long[SOCKETS];
    for (int socket = 0; socket < SOCKETS; socket++) {
      jiffies[socket] = this.jiffies[socket] + other.jiffies[socket];
    }
    return new TasksSample(
      jiffies,
      maxBelowUpper(this.timestamp, other.timestamp));
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return String.join(",",
      timestamp.toString(),
      String.join(",", Arrays.stream(jiffies).mapToObj(Long::toString).toArray(String[]::new)));
  }

  public long getJiffies(int socket) {
    return jiffies[socket];
  }
}
