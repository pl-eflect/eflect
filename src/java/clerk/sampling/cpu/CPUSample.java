package clerk.sampling.cpu;

import static jrapl.util.EnergyCheckUtils.SOCKETS;

import chappie.profiling.TimestampedSample;
import chappie.profiling.MergableSample;
import chappie.util.TimeUtil;
import java.lang.Math;
import java.time.Instant;
import java.util.Arrays;
import jlibc.proc.CPU;

/** Data structure for jiffies difference by socket between CPU snapshots. */
public final class CPUSample implements MergableSample<CPUSample>, TimestampedSample {
  public static final CPUSample EMPTY = new CPUSample(new long[SOCKETS], new long[SOCKETS], Instant.MAX);

  private static final int CORES = Runtime.getRuntime().availableProcessors();

  private final Instant timestamp;
  private final long[] jiffies = new long[SOCKETS];
  private final long[] systemJiffies = new long[SOCKETS];

  CPUSample(CPU[] first, CPU[] second) {
    timestamp = Instant.now();
    if (first.length == CORES && second.length == CORES) {
      for (int core = 0; core < CORES; core++) {
        // TODO(timurbey): need a generic map
        int socket = (int)(core / (CORES / SOCKETS));

        long jiffies = second[core].getNiceJiffies() - first[core].getNiceJiffies();
        jiffies += second[core].getIRQJiffies() - first[core].getIRQJiffies();
        jiffies += second[core].getSoftIRQJiffies() - first[core].getSoftIRQJiffies();
        jiffies += second[core].getStealJiffies() - first[core].getStealJiffies();
        jiffies += second[core].getGuestJiffies() - first[core].getGuestJiffies();
        jiffies += second[core].getGuestNiceJiffies() - first[core].getGuestNiceJiffies();
        this.systemJiffies[socket] += jiffies;

        jiffies += second[core].getUserJiffies() - first[core].getUserJiffies();
        jiffies += second[core].getKernelJiffies() - first[core].getKernelJiffies();
        this.jiffies[socket] += jiffies;
      }
    }
  }

  /** private constructor to prevent mutation during merges */
  private CPUSample(long[] jiffies, long[] systemJiffies, Instant timestamp) {
    this.timestamp = timestamp;
    for (int socket = 0; socket < SOCKETS; socket++) {
      this.jiffies[socket] = jiffies[socket];
      this.systemJiffies[socket] = systemJiffies[socket];
    }
  }

  /** Adds the values of two cpu samples and takes the greater timestamp. */
  @Override
  public CPUSample merge(CPUSample other) {
    long[] jiffies = new long[SOCKETS];
    long[] systemJiffies = new long[SOCKETS];
    for (int socket = 0; socket < SOCKETS; socket++) {
      jiffies[socket] = this.jiffies[socket] + other.jiffies[socket];
      systemJiffies[socket] = this.systemJiffies[socket] + other.systemJiffies[socket];
    }
    return new CPUSample(
      jiffies,
      systemJiffies,
      TimeUtil.maxBelowUpper(this.timestamp, other.timestamp)); // !TimeUtil.equal(other.timestamp, Instant.MAX) ? TimeUtil.max(this.timestamp, other.timestamp) : this.timestamp);
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  /** formats to csv-like (ts,jiffskt1,jiffskt2,...)*/
  @Override
  public String toString() {
    return String.join(",",
      timestamp.toString(),
      String.join(",", Arrays.stream(jiffies).mapToObj(Long::toString).toArray(String[]::new)));
  }

  public long getJiffies(int socket) {
    return jiffies[socket];
  }

  public long getSystemJiffies(int socket) {
    return systemJiffies[socket];
  }
}