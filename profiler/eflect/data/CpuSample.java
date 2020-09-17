package eflect.data;

import static jrapl.util.EnergyCheckUtils.SOCKETS;

import java.time.Instant;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/** Snapshot of the machine's jiffies. */
public final class CpuSample implements Sample {
  private static final int CPUS = Runtime.getRuntime().availableProcessors();
  private static final String statFile = String.join(File.separator, "/proc", "stat");

  private static int[] readMachineJiffies() {
    int[] jiffies = new int[SOCKETS];
    File cpus = new File(statFile);

    try (BufferedReader reader = new BufferedReader(new FileReader(cpus))) {
      reader.readLine();
      for (int i = 0; i < CPUS; i++) {
        String[] stats = reader.readLine().split(" ");
        // TODO: this mapping is the relation SKT = CPU * SKT_COUNT // CPU_COUNT, which is not guaranteed to be true
        int socket = i * SOCKETS / CPUS;
        int jiffy = Integer.parseInt(stats[1])
          + Integer.parseInt(stats[2])
          + Integer.parseInt(stats[3])
          + Integer.parseInt(stats[6])
          + Integer.parseInt(stats[7])
          + Integer.parseInt(stats[8])
          + Integer.parseInt(stats[9])
          + Integer.parseInt(stats[10]);

        jiffies[socket] += jiffy;
      }
    } catch (Exception e) {
      /* unsafe */
    } finally {
      return jiffies;
    }
  }

  private final int[] jiffies;
  private final Instant timestamp;

  public CpuSample() {
    this.jiffies = readMachineJiffies();
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
