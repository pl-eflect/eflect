package eflect.data;

/** Class that provides the activity of a thread on an energy domain. */
public final class ThreadActivity {
  public final long id;
  public final String name;
  public final int domain;
  public final double activity;

  public ThreadActivity(long id, String name, int domain, double activity) {
    this.id = id;
    this.name = name;
    this.domain = domain;
    this.activity = activity;
  }

  @Override
  public String toString() {
    return String.join(
        ",", Long.toString(id), name, Integer.toString(domain), String.format("%.2f", activity));
  }
}
