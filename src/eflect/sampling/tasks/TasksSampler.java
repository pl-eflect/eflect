package eflect.sampling.tasks;

import chappie.sampling.Sample;
import java.util.HashMap;
import java.util.function.Supplier;
import jlibc.proc.Task;

/** Sampler for relative task jiffies. */
public final class TasksSampler implements Supplier<Sample> {
  private static HashMap<Integer, Task> sampleTasks() {
    HashMap<Integer, Task> tasks = new HashMap<>();
    for (Task task: Task.getAllTasks()) {
      tasks.put(task.getId(), task);
    }
    return tasks;
  }

  private HashMap<Integer, Task> last = sampleTasks();

  public TasksSampler() { }

  /** Returns the jiffies consumed by all tasks since the last sample. */
  @Override
  public Sample get() {
    HashMap<Integer, Task> current = sampleTasks();
    TasksSample sample = new TasksSample(last, current);
    for (Task task: current.values()) {
      last.put(task.getId(), task);
    }
    return sample;
  }
}
