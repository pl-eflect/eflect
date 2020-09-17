package eflect.data;

import java.time.Instant;

/** Interface for a piece of data. */
public interface Sample {
  Instant getTimestamp();
}
