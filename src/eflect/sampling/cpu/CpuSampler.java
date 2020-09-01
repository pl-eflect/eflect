package eflect.sampling.cpu;

import chappie.sampling.Sample;
import java.util.function.Supplier;
import jlibc.proc.Cpu;

/** Relative sampler for cpu jiffies so individual users can track usage. */
public final class CpuSampler implements Supplier<Sample> {
  private Cpu[] last;

  public CpuSampler() { last = Cpu.getCpus(); }

  /** Returns the cpu sample of jiffies used since the last sample. */
  @Override
  public Sample get() {
    Cpu[] current = Cpu.getCpus();
    CpuSample record = new CpuSample(last, current);
    last = current;
    return record;
  }
}
