package eflect.experiments;

import static eflect.experiments.util.TensorFlowUtil.normalizeImage;
import static eflect.experiments.util.TensorFlowUtil.readBytes;
import static eflect.util.LoggerUtil.getLogger;

import eflect.Eflect;
import java.nio.file.Paths;
import java.util.List;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

/** Runs a frozen tensorflow graph on image data. */
public class InceptionDriver {
  private static void checkArgs(String[] args) {
    if (args.length < 1) {
      System.out.println("Expected 2 args; got " + (args.length - 1) + ": no model graph provided");
      System.exit(1);
    } else if (args.length < 2) {
      System.out.println(
          "Expected 2 args; got " + (args.length - 1) + ": no evaluation image provided");
      System.exit(1);
    }
  }

  public static void main(String[] args) {
    checkArgs(args);
    // hack to load the stripped .so
    System.load(System.getProperty("tf.lib"));
    try (Tensor<?> data = normalizeImage(readBytes(Paths.get(args[1]))); ) {
      executeGraph(readBytes(Paths.get(args[0])), data);
    }
  }

  private static void executeGraph(byte[] graphDef, Tensor<?> data) {
    try (Graph g = new Graph()) {
      g.importGraphDef(graphDef);
      int iters = 10;
      int batches = 250;
      Eflect.getInstance().start();
      for (int i = 0; i < iters * batches; i++) {
        try (Session s = new Session(g)) {
          List<Tensor<?>> result =
              s.runner().feed("input", data).fetch("InceptionV3/Predictions/Reshape/shape").run();
        }
        if ((i + 1) % (batches) == 0) {
          getLogger().info("completed iteration " + Integer.toString(batches / i));
          Eflect.getInstance().stop();
          Eflect.getInstance().dump(Integer.toString(batches / i));
          Eflect.getInstance().start();
        }
      }
      Eflect.getInstance().stop();
    }
  }

  private InceptionDriver() {}
}
