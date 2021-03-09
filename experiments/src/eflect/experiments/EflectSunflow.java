package eflect.experiments;

import com.stoke.AeneasMachine;
import com.stoke.DiscreteKnob;
import com.stoke.Knob;
import com.stoke.StochasticPolicyType;
import com.stoke.types.KnobValT;
import eflect.Eflect;
import eflect.stoke.EflectReward;
import java.util.HashMap;
import org.sunflow.SunflowAPI;
import org.sunflow.core.Display;
import org.sunflow.image.Color;
import org.sunflow.math.Matrix4;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;
import org.sunflow.system.BenchmarkTest;

public final class EflectSunflow implements BenchmarkTest {
  private int resolution;
  private int threads;

  private static StochasticPolicyType getPolicy() {
    return StochasticPolicyType.toStochasticPolicy(
        System.getProperty("aeneas.policy", "EPSILON_GREEDY_10"));
  }

  private static Knob[] getKnobs() {
    return new Knob[] {
      new DiscreteKnob("aa.min", KnobValT.haveIntegers(-1, -2, -3)),
      new DiscreteKnob("aa.max", KnobValT.haveIntegers(1, 2, 3))
    };
  }

  public static void main(String[] args) {
    double SLA = Integer.parseInt(args[0]);

    int iterations = 50;
    HashMap<String, Integer> properties =
        new HashMap<>() {
          {
            put("aa.min", -1);
            put("aa.max", 1);
          }
        };
    AeneasMachine machine = new AeneasMachine(getPolicy(), getKnobs(), new EflectReward(SLA));

    Eflect.getInstance().start();
    machine.start();
    for (int i = 0; i < iterations; i++) {
      new EflectSunflow(64, Runtime.getRuntime().availableProcessors()).kernelMain();
      machine.interact();
      for (String property : properties.keySet()) {
        int value = KnobValT.needInteger(machine.read(property));
        if (value != properties.get(property)) {
          System.setProperty(property, Integer.toString(value));
          properties.put(property, value);
        }
      }
    }
    machine.stop();
    Eflect.getInstance().stop();
  }

  public EflectSunflow(int resolution, int threads) {
    this.resolution = resolution;
    this.threads = threads;
  }

  @Override
  public void kernelMain() {
    new BenchmarkScene();
  }

  private class BenchmarkScene extends SunflowAPI {
    public BenchmarkScene() {
      build();

      Display dummyDisplay =
          new Display() {
            public void imageBegin(int w, int h, int bucketSize) {}

            public void imagePrepare(int x, int y, int w, int h, int id) {}

            public void imageUpdate(int x, int y, int w, int h, Color[] data, float[] alpha) {}

            public void imageFill(int x, int y, int w, int h, Color c, float alpha) {}

            public void imageEnd() {}
          };
      render(SunflowAPI.DEFAULT_OPTIONS, dummyDisplay);
    }

    @Override
    public void build() {
      // settings
      parameter("threads", threads);
      // spawn regular priority threads
      parameter("threads.lowPriority", false);
      parameter("resolutionX", resolution);
      parameter("resolutionY", resolution);
      parameter("aa.min", Integer.parseInt(System.getProperty("aa.min", "-1")));
      parameter("aa.max", Integer.parseInt(System.getProperty("aa.max", "1")));
      parameter("filter", "triangle");
      parameter("depths.diffuse", 2);
      parameter("depths.reflection", 2);
      parameter("depths.refraction", 2);
      parameter("bucket.order", "hilbert");
      parameter("bucket.size", 32);
      // gi options
      parameter("gi.engine", "igi");
      parameter("gi.igi.samples", 90);
      parameter("gi.igi.c", 0.000008f);
      options(SunflowAPI.DEFAULT_OPTIONS);
      buildCornellBox();
    }

    private void buildCornellBox() {
      // camera
      parameter(
          "transform",
          Matrix4.lookAt(new Point3(0, 0, -600), new Point3(0, 0, 0), new Vector3(0, 1, 0)));
      parameter("fov", 45.0f);
      camera("main_camera", "pinhole");
      parameter("camera", "main_camera");
      options(SunflowAPI.DEFAULT_OPTIONS);
      // cornell box
      float minX = -200;
      float maxX = 200;
      float minY = -160;
      float maxY = minY + 400;
      float minZ = -250;
      float maxZ = 200;

      float[] verts =
          new float[] {
            minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY,
            minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ,
          };
      int[] indices =
          new int[] {
            0, 1, 2, 2, 3, 0, 4, 5, 6, 6, 7, 4, 1, 2, 5, 5, 6, 2, 2, 3, 6, 6, 7, 3, 0, 3, 4, 4, 7, 3
          };

      parameter("diffuse", null, 0.70f, 0.70f, 0.70f);
      shader("gray_shader", "diffuse");
      parameter("diffuse", null, 0.80f, 0.25f, 0.25f);
      shader("red_shader", "diffuse");
      parameter("diffuse", null, 0.25f, 0.25f, 0.80f);
      shader("blue_shader", "diffuse");

      // build walls
      parameter("triangles", indices);
      parameter("points", "point", "vertex", verts);
      parameter("faceshaders", new int[] {0, 0, 0, 0, 1, 1, 0, 0, 2, 2});
      geometry("walls", "triangle_mesh");

      // instance walls
      parameter("shaders", new String[] {"gray_shader", "red_shader", "blue_shader"});
      instance("walls.instance", "walls");

      // create mesh light
      parameter(
          "points",
          "point",
          "vertex",
          new float[] {-50, maxY - 1, -50, 50, maxY - 1, -50, 50, maxY - 1, 50, -50, maxY - 1, 50});
      parameter("triangles", new int[] {0, 1, 2, 2, 3, 0});
      parameter("radiance", null, 15, 15, 15);
      parameter("samples", 8);
      light("light", "triangle_mesh");

      // spheres
      parameter("eta", 1.6f);
      shader("Glass", "glass");
      sphere("glass_sphere", "Glass", -120, minY + 55, -150, 50);
      parameter("color", null, 0.70f, 0.70f, 0.70f);
      shader("Mirror", "mirror");
      sphere("mirror_sphere", "Mirror", 100, minY + 60, -50, 50);

      // scanned model
      geometry("teapot", "teapot");
      parameter(
          "transform",
          Matrix4.translation(80, -50, 100)
              .multiply(Matrix4.rotateX((float) -Math.PI / 6))
              .multiply(Matrix4.rotateY((float) Math.PI / 4))
              .multiply(Matrix4.rotateX((float) -Math.PI / 2).multiply(Matrix4.scale(1.2f))));
      parameter("shaders", "gray_shader");
      instance("teapot.instance1", "teapot");
      parameter(
          "transform",
          Matrix4.translation(-80, -160, 50)
              .multiply(Matrix4.rotateY((float) Math.PI / 4))
              .multiply(Matrix4.rotateX((float) -Math.PI / 2).multiply(Matrix4.scale(1.2f))));
      parameter("shaders", "gray_shader");
      instance("teapot.instance2", "teapot");
    }

    private void sphere(String name, String shaderName, float x, float y, float z, float radius) {
      geometry(name, "sphere");
      parameter("transform", Matrix4.translation(x, y, z).multiply(Matrix4.scale(radius)));
      parameter("shaders", shaderName);
      instance(name + ".instance", name);
    }
  }

  @Override
  public void kernelBegin() {}

  @Override
  public void kernelEnd() {}
}
