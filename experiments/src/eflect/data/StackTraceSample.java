package eflect.data;

import java.time.Instant;

/** Sample of a thread's current executing trace. */
public final class StackTraceSample implements Sample {
  private final Instant timestamp;
  private final long id;
  private final String stackTrace;

  public StackTraceSample(Instant timestamp, long id, String stackTrace) {
    this.timestamp = timestamp;
    this.id = id;
    this.stackTrace = stackTrace;
  }

  /** Returns the stored timestamp. */
  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  /** Returns the executing thread's id. */
  public long getId() {
    return id;
  }

  /** Returns the executing stack trace. */
  public String getStackTrace() {
    return stackTrace;
  }
}
