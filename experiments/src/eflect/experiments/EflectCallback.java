package eflect.experiments;

import eflect.Eflect;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class EflectCallback extends Callback {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private String benchmark;
  private int iteration = 0;

  public EflectCallback(CommandLineArgs args) {
    super(args);
  }

  /** Starts eflect. */
  @Override
  public void start(String benchmark) {
    this.benchmark = benchmark;
    Eflect.getInstance().start();
    super.start(benchmark);
  }

  /** Stops eflect and dumps the data. */
  @Override
  public void stop(long duration) {
    super.stop(duration);
    Eflect.getInstance().stop();
    Eflect.getInstance().dump(Integer.toString(iteration++));
  }

  /** Shutdowns eflect if DaCapo is done. */
  @Override
  public boolean runAgain() {
    boolean doRun = super.runAgain();
    if (!doRun) {
      Eflect.getInstance().shutdown();
    }
    return doRun;
  }
}
