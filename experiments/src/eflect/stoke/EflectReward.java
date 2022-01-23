package eflect.stoke;

import com.stoke.Reward;
import eflect.Eflect;
import eflect.data.EnergyFootprint;
import eflect.util.TimeUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Reward that uses {@link Eflect} for valuation. */
public final class EflectReward extends Reward {

  private final double SLA;

  private final ArrayList<EnergyFootprint> footprints = new ArrayList<>();
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
    Eflect.getInstance().stop();
    EnergyFootprint.Builder builder = new EnergyFootprint.Builder();
    double energy = 0;
    Instant start = Instant.MAX;
    Instant end = Instant.MIN;
    for (EnergyFootprint footprint : Eflect.getInstance().read()) {
      energy += footprint.energy;
      start = TimeUtil.min(start, footprint.start);
      end = TimeUtil.max(end, footprint.end);
    }
    footprints.add(new EnergyFootprint.Builder()
      .setEnergy(energy)
      .setStart(start)
      .setEnd(end)
      .build());
    Eflect.getInstance().start();
    return energy;
  }

  public ArrayList<EnergyFootprint> getData() {
    return new ArrayList(footprints);
  }
}
