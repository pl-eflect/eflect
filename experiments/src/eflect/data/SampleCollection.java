package eflect.data;

import java.util.Collection;

/** Interface for a collection of {@link Sample}s. */
public interface SampleCollection extends Sample {
  /** Returns the samples stored in this collection. */
  Collection<Sample> getSamples();
}
