package clerk.experiments;

import static java.util.concurrent.TimeUnit.SECONDS;

import clerk.Clerk;
import chappie.profiling.Profile;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public final class NOP {

  public static void nop() {
    return;
  }

  public static int fibonaci(int n) {
    if (n <= 1) {
      return n;
    } return fibonaci(n - 1) + fibonaci(n - 2);
  }

  public static void writeHashes() {
    File file = new File(Thread.currentThread().getName());
    try { file.createNewFile(); } catch (Exception e) {}
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.getPath()))) {
      for (int j = 0; j < 500000; j++) {
        String message = UUID.randomUUID().toString();
        writer.write(message, 0, message.length());
        writer.newLine();
      }
    } catch (Exception e) { System.out.println("whoops!"); e.printStackTrace(); }
    file.delete();
    System.out.println("all done!");
  }

  public static void main(String[] args) throws Exception {
    int threads = Runtime.getRuntime().availableProcessors();

    String workloadName = System.getProperty("clerk.nop.workload", "nop");
    System.out.println(workloadName);
    Runnable workload = () -> { try { Thread.sleep(60000); } catch (Exception e) {} };
    Runnable workload2 = null;
    if (workloadName.equals("cpu")) {
      System.out.println("running cpu test");
      workload = () -> {
        fibonaci(48);
        System.out.println("all done!");
      };
    } else if (workloadName.equals("io")) {
      System.out.println("running i/o test");
      workload = NOP::writeHashes;
    } else if (workloadName.equals("mixed")) {
      System.out.println("running mixed test");
      workload = NOP::writeHashes;
      workload2 = () -> {
        fibonaci(48);
        System.out.println("all done!");
      };
    } else {
      System.out.println("running nop");
      threads = 1;
    }

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    Clerk.start();
    for (int i = 0; i < threads; i++) {
      if (workloadName.equals("mixed") && i > 3 * threads / 4) {
        executor.execute(workload2);
      } else {
        executor.execute(workload);
      }
    }
    executor.shutdown();
    while(!executor.awaitTermination(5, SECONDS)) { }
    Clerk.stop();

    String path = System.getProperty("chappie.out", "chappie-log.txt");

    try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
      writer.println("start,end,socket,attributed,system,total");
      for (Profile profile: Clerk.getProfiles()) {
        writer.println(profile);
      }
    } catch (Exception e) {
      System.out.println("unable to write profiles");
      e.printStackTrace();
    }
  }
}
