/* ************************************************************************************************ 
 * Copyright 2016 SUNY Binghamton
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit 
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 * ***********************************************************************************************/

package com.stoke.util;

public class OsUtil {

  public static OsType getOsType() {
    String osName = System.getProperty("os.name");

    if (isWindowsVariant(osName)) {
      return OsType.WINDOWS;
    }

    if (isUnixVariant(osName)) {
      if (osName.equals("Mac OS X")) {
        return OsType.MACOS;
      }

      String vendor = System.getProperty("java.vendor");

      if (vendor.equals("The Android Project")) {
        return OsType.ANDROID;
      }

      return OsType.LINUX;
    }

    return OsType.NONE;
  }

  private static boolean isWindowsVariant(String osName) {
    return (osName.equals("Windows 7") || osName.equals("Windows XP") ||
            osName.equals("Windows 2003") || osName.equals("Windows NT"));
  }

  private static boolean isUnixVariant(String osName) {
    return (osName.equals("Linux") || osName.equals("Mac OS X"));
  }
}
