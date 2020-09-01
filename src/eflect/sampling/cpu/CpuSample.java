package eflect.sampling.cpu;

import static chappie.utils.TimeUtils.maxBelowUpper;
import static jrapl.util.EnergyCheckUtils.SOCKETS;

import chappie.sampling.TimestampedSample;
import chappie.sampling.MergableSample;
import java.time.Instant;
import java.util.Arrays;
import jlibc.proc.Cpu;

/** Data structure for jiffies difference by socket between CPU snapshots. */
public final class CpuSample implements MergableSample<CpuSample>, TimestampedSample {
  public static final CpuSample EMPTY = new CpuSample(new long[SOCKETS], Instant.MAX);

  private static final int CORES = Runtime.getRuntime().availableProcessors();

  private final Instant timestamp;
  private final long[] jiffies = new long[SOCKETS];

  CpuSample(Cpu[] first, Cpu[] second) {
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
        jiffies += second[core].getUserJiffies() - first[core].getUserJiffies();
        jiffies += second[core].getKernelJiffies() - first[core].getKernelJiffies();
        this.jiffies[socket] += jiffies;
      }
    }
  }

  /** private constructor to prevent mutation during merges */
  private CpuSample(long[] jiffies, Instant timestamp) {
    this.timestamp = timestamp;
    for (int socket = 0; socket < SOCKETS; socket++) {
      this.jiffies[socket] = jiffies[socket];
    }
  }

  /** Adds the values of two cpu samples and takes the greater timestamp. */
  @Override
  public CpuSample merge(CpuSample other) {
    long[] jiffies = new long[SOCKETS];
    for (int socket = 0; socket < SOCKETS; socket++) {
      jiffies[socket] = this.jiffies[socket] + other.jiffies[socket];
    }
    return new CpuSample(
      jiffies,
      maxBelowUpper(this.timestamp, other.timestamp));
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
}
