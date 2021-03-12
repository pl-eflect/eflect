package eflect.data;

import java.time.Instant;
import java.util.ArrayList;

/** Report for energy consumed by a thread. */
public final class EnergyFootprint {
  public final long id;
  public final String name;
  public final Instant start;
  public final Instant end;
  public final int domain;
  public final double energy;
  public final double totalEnergy;
  public final String stackTrace;

  private EnergyFootprint(
      long id,
      String name,
      Instant start,
      Instant end,
      int domain,
      double energy,
      double totalEnergy,
      String stackTrace) {
    this.id = id;
    this.name = name;
    this.start = start;
    this.end = end;
    this.domain = domain;
    this.energy = energy;
    this.totalEnergy = totalEnergy;
    this.stackTrace = stackTrace;
  }

  @Override
  public String toString() {
    if (stackTrace == "") {
      return String.join(
          ",",
          Long.toString(id),
          name,
          start.toString(),
          end.toString(),
          Integer.toString(domain),
          Double.toString(energy),
          Double.toString(totalEnergy),
          "");
    }
    String[] traces = stackTrace.split("@");
    String[] footprints = new String[traces.length];
    for (int i = 0; i < traces.length; i++) {
      if (!traces[i].isEmpty()) {
        footprints[i] =
            String.join(
                ",",
                Long.toString(id),
                name,
                start.toString(),
                end.toString(),
                Integer.toString(domain),
                Double.toString(energy / traces.length),
                Double.toString(totalEnergy),
                traces[i]);
      }
    }
    return String.join(System.lineSeparator(), footprints);
  }

  public Builder toBuilder() {
    Builder builder =
        new Builder()
            .setId(id)
            .setName(name)
            .setStart(start)
            .setEnd(end)
            .setDomain(domain)
            .setEnergy(energy)
            .setTotalEnergy(totalEnergy);
    if (!stackTrace.isEmpty()) {
      for (String trace : stackTrace.split("@")) {
        builder.addStackTrace(trace);
      }
    }
    return builder;
  }

  public static final class Builder {
    private long id;
    private String name = "";
    private Instant start = Instant.EPOCH;
    private Instant end = Instant.EPOCH;
    private int domain = 0;
    private double energy = 0;
    private double totalEnergy = 0;
    private ArrayList<String> stackTrace = new ArrayList<>();;

    public Builder() {}

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setId(long id) {
      this.id = id;
      return this;
    }

    public Builder setStart(Instant start) {
      this.start = start;
      return this;
    }

    public Builder setEnd(Instant end) {
      this.end = end;
      return this;
    }

    public Builder setDomain(int domain) {
      this.domain = domain;
      return this;
    }

    public Builder setEnergy(double energy) {
      this.energy = energy;
      return this;
    }

    public Builder setTotalEnergy(double energy) {
      this.totalEnergy = energy;
      return this;
    }

    public Builder addStackTrace(String stackTrace) {
      this.stackTrace.add(stackTrace);
      return this;
    }

    public EnergyFootprint build() {
      if (stackTrace.isEmpty()) {
        return new EnergyFootprint(id, name, start, end, domain, energy, totalEnergy, "");
      } else {
        return new EnergyFootprint(
            id, name, start, end, domain, energy, totalEnergy, String.join("@", stackTrace));
      }
    }
  }
}
