package eflect.data.jiffies;

import eflect.data.ThreadActivity;

/** Builder for {@link ThreadActivity} from /proc/ data. */
// TODO: this is sketchy
final class ProcThreadActivityBuilder {
  private long id;
  private String name;
  private int domain;
  private long taskJiffies;
  private long totalJiffies;

  ProcThreadActivityBuilder setId(long id) {
    this.id = id;
    return this;
  }

  ProcThreadActivityBuilder setName(String name) {
    this.name = name;
    return this;
  }

  ProcThreadActivityBuilder setDomain(int domain) {
    this.domain = domain;
    return this;
  }

  ProcThreadActivityBuilder setTaskJiffies(long taskJiffies) {
    this.taskJiffies = taskJiffies;
    return this;
  }

  ProcThreadActivityBuilder setTotalJiffies(long totalJiffies) {
    this.totalJiffies = totalJiffies;
    return this;
  }

  int getDomain() {
    return domain;
  }

  double getActivity() {
    return (double) taskJiffies / totalJiffies;
  }

  ThreadActivity build() {
    return new ThreadActivity(id, name, domain, getActivity());
  }
}
