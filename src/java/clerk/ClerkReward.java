package clerk;

import chappie.attribution.EnergyAttribution;
import chappie.profiling.Profile;
import com.stoke.Reward;
import java.util.ArrayList;

public final class ClerkReward implements Reward {
  private final double SLA;

  private int lastProfileIndex = 0;
  private double cached = 0;

  public ClerkReward() {
    this.SLA = Double.parseDouble(System.getProperty("SLA", "50"));
  }

  @Override
  public double valuate() {
    return getEnergy();
  }

  @Override
  public double SLA() {
    return SLA;
  }

  @Override
  public boolean equate(double r1, double r2) {
   double d = Math.abs(r1 - r2);
   return (Double.compare(d, 1.0) <= 0);
  }

  @Override
  public double cached() {
    return cached;
  }

  private double getEnergy() {
    double energy = 0;
    try {
      int count = 0;
      for (Profile profile: Clerk.getProfiles()) {
        if (count++ > lastProfileIndex) {
          energy += ((EnergyAttribution) profile).getApplicationEnergy();
        }
      }
      lastProfileIndex = count;
    } catch (Exception e) {
      e.printStackTrace();
    }

    cached = energy;

    return energy;
  }
}
