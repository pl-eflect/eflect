package clerk.experiments;

import clerk.Clerk;
import chappie.attribution.AttributionProfile;
import chappie.profiling.Profile;
import chappie.sampling.energy.EnergySample;
import chappie.sampling.energy.EnergySampler;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class DaCapoNOP extends Callback {
  private int iteration = 0;
  private EnergySampler sampler;
  private ArrayList<EnergySample> samples = new ArrayList<>();
  private ArrayList<Instant> timestamps = new ArrayList<>();

  public DaCapoNOP(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    // Clerk.start();
    sampler = new EnergySampler();
    timestamps.add(Instant.now());
    super.start(benchmark);
  }

  @Override
  public void stop(long duration) {
    super.stop(duration);
    // Clerk.stop();
    samples.add(sampler.sample());
    timestamps.add(Instant.now());

    String path = System.getProperty("chappie.out", "chappie-log.txt");

    // try (PrintWriter writer = new PrintWriter(new FileWriter(path + "." + iteration))) {
    //   writer.println("start,end,socket,app_jiff,total_jiff,attributed,total");
    //   for (Profile profile: Clerk.getProfiles()) {
    //     writer.println(profile);
    //   }
    // } catch (Exception e) {
    //   System.out.println("unable to write profiles");
    //   e.printStackTrace();
    // }

    // try (PrintWriter writer = new PrintWriter(new FileWriter(path + "." + iteration))) {
    //   writer.println("start,end,socket,total,attributed");
    //   for (Profile profile: Clerk.getProfiles()) {
    //     writer.println(((AttributionProfile) profile).getAttribution());
    //   }
    // } catch (Exception e) {
    //   System.out.println("unable to write profiles");
    //   e.printStackTrace();
    // }
    //
    // try (PrintWriter writer = new PrintWriter(new FileWriter(path + "." + iteration + ".ranking"))) {
    //   writer.println("stacktrace,energy");
    //   for (Profile profile: Clerk.getProfiles()) {
    //     writer.println(((AttributionProfile) profile).getRanking().dump());
    //   }
    // } catch (Exception e) {
    //   System.out.println("unable to write profiles");
    //   e.printStackTrace();
    // }

    try (PrintWriter writer = new PrintWriter(new FileWriter(path + "." + iteration))) {
      writer.println("timestamp,socket1,socket2");
      for (EnergySample sample: samples) {
        writer.println(sample);
      }
    } catch (Exception e) {
      System.out.println("unable to write energy");
      e.printStackTrace();
    }

    try (PrintWriter writer = new PrintWriter(new FileWriter(path + "." + iteration + ".stamp"))) {
      writer.println("timestamp");
      for (Instant stamp: timestamps) {
        writer.println(stamp.toEpochMilli());
      }
    } catch (Exception e) {
      System.out.println("unable to write stamps");
      e.printStackTrace();
    }

    samples.clear();
    timestamps.clear();
    iteration++;
  }
}
