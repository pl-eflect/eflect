package eflect.experiments;

import eflect.ChappieEflect;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

/** Callback that records eflect footprints. */
public final class ChappieEflectCallback extends Callback {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private String benchmark;
  private int iteration = 0;

  public ChappieEflectCallback(CommandLineArgs args) {
    super(args);
  }

  /** Starts eflect. */
  @Override
  public void start(String benchmark) {
    this.benchmark = benchmark;
    ChappieEflect.getInstance().start();
    super.start(benchmark);
  }

  /** Stops eflect and dumps the data. */
  @Override
  public void stop(long duration) {
    super.stop(duration);
    ChappieEflect.getInstance().stop();
    ChappieEflect.getInstance().dump(Integer.toString(iteration++));
  }

  /** Shutdowns eflect if DaCapo is done. */
  @Override
  public boolean runAgain() {
    boolean doRun = super.runAgain();
    if (!doRun) {
      ChappieEflect.getInstance().shutdown();
    }
    return doRun;
  }
}
