package com.stoke.util;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.io.FileReader;
import java.io.BufferedReader;

import java.util.Map;
import java.util.HashMap;

public class AndroidUtil {
  private static Map<String,Object> properties = new HashMap<String,Object>();

  static {
    File f = new File("/data/local/tmp/stoke/stoke_properties.txt");
    BufferedReader rd = null;
    try {
      rd = new BufferedReader(new FileReader(f));
      while (true) {
        String l = rd.readLine();
        if (l == null) {
          break;
        }
        String[] parsed = l.split("=");
        String key = parsed[0];
        String val = parsed[1];
        String typ = parsed[2];
        Object oval = null;
        switch (typ) {
          case "int":
            oval = Integer.valueOf(Integer.parseInt(val));
            break;
          case "float":
            oval = Float.valueOf(Float.parseFloat(val));
            break;
          case "double":
            oval = Double.valueOf(Double.parseDouble(val));
            break;
          case "string":
            oval = val;
            break;
          case "bool":
            oval = Boolean.valueOf(Boolean.parseBoolean(val));
            break;
        }
        properties.put(key,oval);
        System.err.format("STOKE: AndroidUtil: key:%s value:%s\n", key, oval);
      }
    } catch (Exception e) {
      
    }
  }

  public static Object getProperty(String key) {
    return AndroidUtil.properties.get(key);
  }

}

