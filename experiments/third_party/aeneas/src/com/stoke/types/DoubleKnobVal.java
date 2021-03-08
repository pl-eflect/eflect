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

import java.lang.Math;

public class DoubleKnobVal implements KnobVal {
  private Double _val;

  public DoubleKnobVal() { _val = 0.0; }

  public DoubleKnobVal(Double val) { _val = val; }

  public Object value() { return _val; }

  public KnobValType type() { return KnobValType.DOUBLE; }

  public int compareTo(KnobVal kv) {
    if (kv.type() != KnobValType.DOUBLE) {
      throw new RuntimeException(
          "KnobVal type mismatch - Expected DoubleKnobVal, got " +
          kv.getClass());
    }
    DoubleKnobVal other = (DoubleKnobVal)kv;
    return (_val.compareTo(other._val));
  }

  public KnobVal add(KnobVal kv) {
    if (kv.type() != KnobValType.DOUBLE) {
      throw new RuntimeException(
          "KnobVal type mismatch - Expected DoubleKnobVal, got " +
          kv.getClass());
    }
    DoubleKnobVal other = (DoubleKnobVal)kv;
    return new DoubleKnobVal(_val + other._val);
  }

  public KnobVal sub(KnobVal kv) {
    if (kv.type() != KnobValType.DOUBLE) {
      throw new RuntimeException(
          "KnobVal type mismatch - Expected DoubleKnobVal, got " +
          kv.getClass());
    }
    DoubleKnobVal other = (DoubleKnobVal)kv;
    return new DoubleKnobVal(_val - other._val);
  }

  public KnobVal abs() { return new DoubleKnobVal(Math.abs(_val)); }

  public KnobVal dividedBy(int i) {
    return new DoubleKnobVal(_val / (double)i);
  }

  public KnobVal dividedBy(KnobVal ot) {
    if (ot instanceof IntegerKnobVal) {
      return new DoubleKnobVal(_val / KnobValT.needInteger(ot));
    } else if (ot instanceof DoubleKnobVal) {
      return new DoubleKnobVal(_val / ((DoubleKnobVal)ot)._val);
    } else {
      throw new RuntimeException("should not reach");
    }
  }

  public String toString() { return String.format("%.2f", _val); }
}
