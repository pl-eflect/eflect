package eflect.data;

/** A processor that report the quality of the result from {@link process()}. */
public interface Accountant<O> extends Processor<O> {
  /** Enum that indicates how the user should interpret the result. */
  public enum Result {
    UNACCOUNTABLE,
    UNDERACCOUNTED,
    OVERACCOUNTED,
    ACCOUNTED,
  }

  /** Returns the quality of the result from {@link process()}. */
  Result account();

  /** Adds the data of this accountant with another. */
  <T extends Accountant<O>> void add(T other);

  /** Discards the data at the beginning of the interval. */
  void discardStart();

  /** Discards the data at the end of the interval. */
  void discardEnd();
}
