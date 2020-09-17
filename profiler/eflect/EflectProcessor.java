package eflect;

import eflect.data.Sample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * A processor the stores samples along timestamp-indexed storage and can
 * collapse samples into {@link EnergyFootprint}s.
 */
public final class EflectProcessor {
  private TreeMap<Instant, EflectSampleMerger> data = new TreeMap<>();

  /** Places the sample in a sorted, time-indexed bucket. */
  public void add(Sample s) {
    synchronized(this) {
      Instant timestamp = Instant.now();
      data.putIfAbsent(timestamp, new EflectSampleMerger());
      data.get(timestamp).add(s);
    }
  }

  /**
   * Grabs the stored data and forward scans the data for valid footprints. If
   * a timestamp cannot produce a valid footprint, the data is merged with the
   * next timestamp until a valid footprint is produced. Once all data is
   * consumed, the final sub-processor is replaced into storage.
   */
  public Iterable<EnergyFootprint> process() {
    ArrayList<EnergyFootprint> profiles = new ArrayList<>();
    EflectSampleMerger merger = new EflectSampleMerger();

    TreeMap<Instant, EflectSampleMerger> data;
    synchronized (this) {
      data = this.data;
      this.data = new TreeMap<>();
    }

    int attempts = 0;
    Instant lastTimestamp = Instant.EPOCH;
    for (Instant timestamp: data.keySet()) {
      // TODO: update this logic if we change the merger
      merger = merger.merge(data.get(timestamp));
      if (merger.valid()) {
        profiles.add(merger.process());
        merger = new EflectSampleMerger();
      }
      lastTimestamp = timestamp;
    }
    // TODO: need to collapse the last frame only when discarding the profiler?
    //   probably want to store the excess jiffies to not lose any data
    //   for online profiling
    if (merger.computable()) {
      profiles.add(merger.process());
    } else {
      synchronized(this) {
        merger = merger.merge(data.getOrDefault(lastTimestamp, new EflectSampleMerger()));
        data.put(lastTimestamp, merger);
      }
    }
    return profiles;
  }
}
