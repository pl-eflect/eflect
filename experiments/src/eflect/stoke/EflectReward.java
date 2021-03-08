package eflect.stoke;

import com.stoke.Reward;
import eflect.Eflect;
import eflect.data.EnergyFootprint;
import java.util.concurrent.atomic.AtomicInteger;

public final class EflectReward extends Reward {
  private final double SLA;

  private AtomicInteger interactionCount = new AtomicInteger(0);

  public EflectReward(double SLA) {
    this.SLA = SLA;
  }

  @Override
  public double valuate() {
    return perInteractionEnergy();
  }

  @Override
  public double SLA() {
    return SLA;
  }

  @Override
  public boolean equate(double r1, double r2) {
    double d = Math.abs(r1 - r2);
    return Double.compare(d, 1.0) <= 0;
  }

  @Override
  public double perInteractionEnergy() {
    double energy = 0;
    for (EnergyFootprint footprint : Eflect.getInstance().read()) {
      energy += footprint.energy;
    }
    Eflect.getInstance().dump(Integer.toString(interactionCount.getAndIncrement()));
    return energy;
  }
}
