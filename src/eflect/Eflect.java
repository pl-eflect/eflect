package eflect;

import static eflect.util.ProcUtil.readProcStat;
import static eflect.util.ProcUtil.readTaskStats;

import eflect.data.AccountantMerger;
import eflect.data.EnergyAccountant;
import eflect.data.EnergyFootprint;
import eflect.data.EnergySample;
import eflect.data.Sample;
import eflect.data.SampleCollector;
import eflect.data.jiffies.JiffiesAccountant;
import eflect.data.jiffies.ProcStatSample;
import eflect.data.jiffies.ProcTaskSample;
import eflect.util.Rapl;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/** A clerk that uses the eflect algorithm as a {@link Processor}. */
public final class Eflect extends SampleCollector<Collection<EnergyFootprint>> {
  // system constants
  private static final int SOCKET_COUNT = Rapl.getInstance().getSocketCount();
  private static final int COMPONENT_COUNT = 3; // hard-coded value from rapl
  private static final double WRAP_AROUND_ENERGY = Rapl.getInstance().getWrapAroundEnergy();
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private static Collection<Supplier<Sample>> getSources() {
    Supplier<Sample> stat = () -> new ProcStatSample(Instant.now(), readProcStat());
    Supplier<Sample> task = () -> new ProcTaskSample(Instant.now(), readTaskStats());
    Supplier<Sample> rapl =
        () -> new EnergySample(Instant.now(), Rapl.getInstance().getEnergyStats());
    return List.of(stat, task, rapl);
  }

  public Eflect(int mergeAttempts, ScheduledExecutorService executor, Duration period) {
    super(
        getSources(),
        new AccountantMerger<EnergyFootprint>(
            () ->
                new EnergyAccountant(
                    SOCKET_COUNT,
                    COMPONENT_COUNT,
                    WRAP_AROUND_ENERGY,
                    new JiffiesAccountant(SOCKET_COUNT, cpu -> cpu / (CPU_COUNT / SOCKET_COUNT))),
            mergeAttempts),
        executor,
        period);
  }
}
