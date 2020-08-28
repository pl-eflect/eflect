package clerk.sampling.tasks;

import static jrapl.util.EnergyCheckUtils.SOCKETS;

import chappie.profiling.MergableSample;
import chappie.profiling.TimestampedSample;
import chappie.util.TimeUtil;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jlibc.proc.Task;

/** Collection of relative task snapshots that can be accessed by socket. */
public class TasksSample implements MergableSample<TasksSample>, TimestampedSample {
  public static final TasksSample EMPTY = new TasksSample(new long[SOCKETS], Instant.MAX);

  private static final int CORES = Runtime.getRuntime().availableProcessors();
  private static final List<String> C_JVM_THREADS = List.of(
    "CompilerThre",
    "Conc#",
    "Refine#",
    "Young RemSet",
    "Main Marker",
    "GC Thread#"
  );
  private static final List<String> JAVA_JVM_THREADS = List.of(
    "Common-Cleaner",
    "Finalizer",
    "Java2D-Disposer",
    "process reaper",
    "Reference Handl",
    "Signal Dispatch"
  );
  private static final List<String> FRAMEWORK_THREADS = List.of(
    "chappie"
  );
  private static final boolean isApplicationTask(Task task) {
    String taskName = task.getName();

    for (String name: C_JVM_THREADS) {
      if (taskName.contains(name)) {
        return false;
      }
    }

    for (String name: JAVA_JVM_THREADS) {
      if (taskName.contains(name)) {
        return false;
      }
    }

    for (String name: FRAMEWORK_THREADS) {
      if (taskName.contains(name)) {
        return false;
      }
    }

    return true;
  }

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
      TimeUtil.maxBelowUpper(this.timestamp, other.timestamp));
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
