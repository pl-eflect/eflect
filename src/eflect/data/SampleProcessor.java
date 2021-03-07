package eflect.data;

/** Interface that consumes and produces data. */
public interface SampleProcessor<O> {
  /** Adds data to the processor. */
  void add(Sample s);

  /** Processes the data and returns the result. */
  O process();
}
