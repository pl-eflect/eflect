package eflect.data;

import java.time.Instant;

/** Interface for a piece of timestamped data. */
public interface Sample {
  /** Returns the timestamp associated with the sample. */
  Instant getTimestamp();
}
