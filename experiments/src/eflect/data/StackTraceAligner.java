package eflect.data;

import eflect.util.RangeMap;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

/** Processor that aligns stack traces to energy footprints. */
public final class StackTraceAligner implements SampleProcessor<Collection<EnergyFootprint>> {
  private final TreeMap<Instant, Collection<StackTraceSample>> samples = new TreeMap<>();
  private final SampleProcessor<Collection<EnergyFootprint>> energyProcessor;

  public StackTraceAligner(SampleProcessor<Collection<EnergyFootprint>> energyProcessor) {
    this.energyProcessor = energyProcessor;
  }

  /** Put the sample data into the correct container. */
  @Override
  public void add(Sample s) {
    if (s instanceof SampleCollection) {
      Collection<Sample> samples = ((SampleCollection) s).getSamples();
      for (Sample sample : samples) {
        add(sample);
      }
    } else if (s instanceof StackTraceSample) {
      addStackTrace((StackTraceSample) s);
    } else {
      energyProcessor.add(s);
    }
  }

  /**
   * Returns the energy footprints of the underlying accountant and aligns stack traces collected in
   * corresponding time ranges.
   */
  @Override
  public Collection<EnergyFootprint> process() {
    RangeMap<Instant, HashMap<Long, EnergyFootprint.Builder>> footprintGroups = getFootprints();
    synchronized (samples) {
      for (Instant timestamp : samples.keySet()) {
        if (footprintGroups.contains(timestamp)) {
          for (StackTraceSample sample : samples.get(timestamp)) {
            if (footprintGroups.get(timestamp).containsKey(sample.getId())) {
              footprintGroups
                  .get(timestamp)
                  .get(sample.getId())
                  .addStackTrace(sample.getStackTrace());
            }
          }
        }
      }
      samples.clear();
    }
    ArrayList<EnergyFootprint> footprints = new ArrayList<>();
    for (HashMap<?, EnergyFootprint.Builder> group : footprintGroups.values()) {
      for (EnergyFootprint.Builder builder : group.values()) {
        EnergyFootprint footprint = builder.build();
        if (footprint.energy > 0) {
          footprints.add(footprint);
        }
      }
    }
    return footprints;
  }

  private void addStackTrace(StackTraceSample sample) {
    synchronized (samples) {
      if (!samples.containsKey(sample.getTimestamp())) {
        samples.put(sample.getTimestamp(), new ArrayList<StackTraceSample>());
      }
      samples.get(sample.getTimestamp()).add(sample);
    }
  }

  private RangeMap<Instant, HashMap<Long, EnergyFootprint.Builder>> getFootprints() {
    HashMap<Instant, HashMap<Long, EnergyFootprint.Builder>> footprints = new HashMap<>();
    for (EnergyFootprint footprint : energyProcessor.process()) {
      if (!footprints.containsKey(footprint.start)) {
        footprints.put(footprint.start, new HashMap<Long, EnergyFootprint.Builder>());
      }
      footprints.get(footprint.start).put(footprint.id, footprint.toBuilder());
      if (!footprints.containsKey(footprint.end)) {
        footprints.put(footprint.end, new HashMap<Long, EnergyFootprint.Builder>());
      }
    }
    return new RangeMap<>(footprints);
  }
}
