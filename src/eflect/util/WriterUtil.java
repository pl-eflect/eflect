package eflect.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Utility to write csv data. */
public final class WriterUtil {
  private static final Logger logger = LoggerUtil.getLogger();

  public static void writeCsv(String directory, String fileName, String header, Iterable<?> data) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(new File(directory, fileName)))) {
      writer.println(header);
      for (Object d : data) {
        writer.println(d.toString());
      }
    } catch (IOException e) {
      logger.log(Level.INFO, "unable to write " + new File(directory, fileName), e);
    }
  }
}
