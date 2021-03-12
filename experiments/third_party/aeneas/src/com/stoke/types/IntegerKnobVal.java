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

public class IntegerKnobVal implements KnobVal {
  private Integer _val;

  public IntegerKnobVal() { _val = 0; }

  public IntegerKnobVal(Integer val) { _val = val; }

  public KnobValType type() { return KnobValType.INTEGER; }

  public Object value() { return _val; }

  public int compareTo(KnobVal kv) {
    if (kv.type() != KnobValType.INTEGER) {
      throw new RuntimeException(
          "KnobVal type mismatch - Expected IntegerKnobVal, got " +
          kv.getClass());
    }
    IntegerKnobVal other = (IntegerKnobVal)kv;
    return (_val.compareTo(other._val));
  }

  public KnobVal add(KnobVal kv) {
    if (kv.type() != KnobValType.INTEGER) {
      throw new RuntimeException(
          "KnobVal type mismatch - Expected IntegerKnobVal, got " +
          kv.getClass());
    }
    IntegerKnobVal other = (IntegerKnobVal)kv;
    return new IntegerKnobVal(_val + other._val);
  }

  public KnobVal sub(KnobVal kv) {
    if (kv.type() != KnobValType.INTEGER) {
      throw new RuntimeException(
          "KnobVal type mismatch - Expected IntegerKnobVal, got " +
          kv.getClass());
    }
    IntegerKnobVal other = (IntegerKnobVal)kv;
    return new IntegerKnobVal(_val - other._val);
  }

  public KnobVal abs() { return new IntegerKnobVal(Math.abs(_val)); }

  public KnobVal dividedBy(int i) { return new IntegerKnobVal(_val / i); }

  public KnobVal dividedBy(KnobVal ot) {
    if (ot instanceof IntegerKnobVal) {
      return new IntegerKnobVal(_val / ((IntegerKnobVal)ot)._val);
    } else if (ot instanceof DoubleKnobVal) {
      return new DoubleKnobVal((double)_val / KnobValT.needDouble(ot));
    } else {
      throw new RuntimeException("should not reach");
    }
  }

  public String toString() { return String.format("%d", _val); }
}
