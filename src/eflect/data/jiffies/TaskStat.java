package eflect.data.jiffies;

/** A class that contains relevant information from /proc/[pid]/task/[tid]/stat. */
final class TaskStat {
  final long id;
  final String name;
  final int cpu;
  final long jiffies;

  TaskStat(long id, String name, int cpu, long jiffies) {
    this.id = id;
    this.name = name;
    this.cpu = cpu;
    this.jiffies = jiffies;
  }
}
