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

package com.stoke.types;

public class StringKnobVal implements KnobVal {
  private String _val;

  public StringKnobVal() { _val = ""; }

  public StringKnobVal(String val) { _val = val; }

  public KnobValType type() { return KnobValType.STRING; }

  public Object value() { return _val; }

  public int compareTo(KnobVal kv) {
    if (kv.type() != KnobValType.STRING) {
      throw new RuntimeException(
          "KnobVal type mismatch - Expected StringKnobVal, got " +
          kv.getClass());
    }
    StringKnobVal other = (StringKnobVal)kv;
    return (_val.compareTo(other._val));
  }

  public KnobVal add(KnobVal kv) {
    throw new RuntimeException("Should not be adding a StringKnobVal");
  }

  public KnobVal sub(KnobVal kv) {
    throw new RuntimeException("Should not be subtracting a StringKnobVal");
  }

  public KnobVal abs() { 
    throw new RuntimeException("Should not abs a StringKnobVal");
  } 

  public KnobVal dividedBy(int i) { 
    throw new RuntimeException("Should not be dividing a StringKnobVal");
  }

  public KnobVal dividedBy(KnobVal ot) {
    throw new RuntimeException("Should not be dividing a StringKnobVal");
  }

  public String toString() { return String.format("%b", _val); }
}
