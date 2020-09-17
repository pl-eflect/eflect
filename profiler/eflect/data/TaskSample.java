package eflect.data;

import static eflect.utils.OsUtils.getProcessId;
import static jrapl.util.EnergyCheckUtils.SOCKETS;

import java.time.Instant;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Snapshot of the application's jiffies. */
public class TaskSample implements Sample {
  private static int[] readApplicationJiffies() {
    int[] jiffies = new int[SOCKETS];
    File[] tasks = new File(String.join(
      File.separator,
      "/proc",
      Integer.toString(getProcessId()),
      "task"))
    .listFiles();

    for (int i = 0; i < tasks.length; i++) {
      int tid = Integer.parseInt(tasks[i].getName());
      String task = String.join(
        File.separator,
        "/proc",
        Integer.toString(getProcessId()),
        "task",
        Integer.toString(tid),
        "stat");

      if (Files.notExists(Paths.get(task)))
        continue;

      try {
        String[] stat = Files.readString(Paths.get(task)).split(" ");

        int offset = stat.length - 52;
        // TODO: this mapping is the relation SKT = CPU * SKT_COUNT // CPU_COUNT, which is not guaranteed to be true
        int socket = Integer.parseInt(stat[38 + offset]) / 20;
        int jiffy = Integer.parseInt(stat[13 + offset]) + Integer.parseInt(stat[14 + offset]);

        jiffies[socket] += jiffy;
      } catch (Exception e) {
        // unsafe
      }
    }
    return jiffies;
  }

  private final int[] jiffies;
  private final Instant timestamp;

  public TaskSample() {
    this.jiffies = readApplicationJiffies();
    this.timestamp = Instant.now();
  }

  public int[] getJiffies() {
    return jiffies;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }
}
