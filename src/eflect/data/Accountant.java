package eflect.data;

/** A processor that report the quality of the result from {@link process()}. */
public interface Accountant<O> extends SampleProcessor<O> {
  /** Enum that indicates how the user should interpret the result. */
  public enum Result {
    UNACCOUNTABLE,
    UNDERACCOUNTED,
    OVERACCOUNTED,
    ACCOUNTED,
  }

  /** Returns information about the result that will returned from {@link process()}. */
  Result account();

  /** Adds the data from another accountant to this one. */
  <T extends Accountant<O>> void add(T other);

  /** Discards the data at the beginning of the interval. */
  void discardStart();

  /** Discards the data at the end of the interval. */
  void discardEnd();
}
