package eflect.experiments;

import static eflect.util.LoggerUtil.getLogger;

import eflect.Eflect;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.tensorflow.Graph;
import org.tensorflow.Output;
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
    try (Tensor<?> data = normalizeImage(readBytesOrDie(Paths.get(args[1]))); ) {
      executeGraph(readBytesOrDie(Paths.get(args[0])), data);
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
          getLogger().info("completed iteration " + Integer.toString(i / batches + 1));
          Eflect.getInstance().stop();
          Eflect.getInstance().dump(Integer.toString(i / batches));
          Eflect.getInstance().start();
        }
      }
      Eflect.getInstance().stop();
    }
  }

  public static byte[] readBytesOrDie(Path path) {
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      System.err.println("Failed to read [" + path + "]: " + e.getMessage());
      System.exit(1);
    }
    return null;
  }

  public static Tensor<Float> normalizeImage(byte[] imageBytes) {
    try (Graph g = new Graph()) {
      GraphBuilder b = new GraphBuilder(g);
      // Some constants specific to the pre-trained model
      final int H = 224;
      final int W = 224;
      final float mean = 117f;
      final float scale = 1f;

      final Output<String> input = b.constant("input", imageBytes);
      final Output<Float> output =
          b.div(
              b.sub(
                  b.resizeBilinear(
                      b.expandDims(
                          b.cast(b.decodeJpeg(input, 3), Float.class), b.constant("make_batch", 0)),
                      b.constant("size", new int[] {H, W})),
                  b.constant("mean", mean)),
              b.constant("scale", scale));
      try (Session s = new Session(g)) {
        return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class);
      }
    }
  }

  private InceptionDriver() {}
}
