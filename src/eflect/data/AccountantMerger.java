package eflect.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.function.Supplier;

/** Processor that merges accountants into groups of accounted data. */
public final class AccountantMerger<O> implements SampleProcessor<Collection<O>> {
  private final TreeMap<Instant, Accountant<Collection<O>>> data = new TreeMap<>();
  private final Supplier<Accountant<Collection<O>>> accountantFactory;
  private final int mergeAttempts;

  public AccountantMerger(Supplier<Accountant<Collection<O>>> accountantFactory) {
    this.accountantFactory = accountantFactory;
    this.mergeAttempts = Integer.MAX_VALUE;
  }

  public AccountantMerger(
      Supplier<Accountant<Collection<O>>> accountantFactory, int mergeAttempts) {
    this.accountantFactory = accountantFactory;
    this.mergeAttempts = mergeAttempts;
  }

  /** Put the sample into a timestamped bucket. */
  @Override
  public final void add(Sample s) {
    synchronized (data) {
      if (!data.containsKey(s.getTimestamp())) {
        data.put(s.getTimestamp(), accountantFactory.get());
      }
      data.get(s.getTimestamp()).add(s);
    }
  }

  /**
   * Accounts each timestamp, forward aggregating data that isn't {@link ACCOUNTED}. If the final
   * aggregate is accountable, it is also returned.
   */
  @Override
  public final Collection<O> process() {
    ArrayList<O> results = new ArrayList<>();
    Accountant<Collection<O>> accountant = null;
    int attempts = 0;
    synchronized (data) {
      for (Instant timestamp : data.keySet()) {
        if (accountant == null) {
          accountant = data.get(timestamp);
        } else {
          accountant.add(data.get(timestamp));
        }
        if (accountant.account() == Accountant.Result.ACCOUNTED
            || (attempts > mergeAttempts
                && accountant.account() != Accountant.Result.UNACCOUNTABLE)) {
          results.addAll(accountant.process());
          accountant.discardStart(); // we don't need any previous data because it's been accounted
        } else {
          attempts++;
        }
      }
      data.clear();
      if (accountant != null && accountant.account() != Accountant.Result.UNACCOUNTABLE) {
        results.addAll(accountant.process());
      } else if (accountant != null) {
        // put the data at the beginning so it will be picked up next time
        data.put(Instant.EPOCH, accountant);
      }
    }
    return results;
  }
}
