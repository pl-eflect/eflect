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

import com.stoke.AeneasMachine;

import java.util.*;

/* KnobValT is the projection into and out of our DSL type system. 
 *
 * Without a stronger (dependent) type system we cannot implement heterogenous collections
 * in a trivial way. Rather than abusing the generic type system (and still running into
 * issues) we define our own type system for values into and out of the bandits.  
 *
 * To maintain our 'DSL', users should only interact with values of our languages
 * through KnobValT. If the user touches 'KnobVal' directly then we need to adjust. */
public class KnobValT {

  /* need methods project out of KnobVals */

  public static Integer needInteger(KnobVal kv) {
    if (kv.type() != KnobValType.INTEGER) {
      throw new KnobValTypeError("expected integer");
    }
    return (Integer) kv.value();
  }

  public static Double needDouble(KnobVal kv) {
    if (kv.type() != KnobValType.DOUBLE) {
      throw new KnobValTypeError("expected double");
    }
    return (Double) kv.value();
  }

  public static Boolean needBoolean(KnobVal kv) {
    if (kv.type() != KnobValType.BOOLEAN) {
      throw new KnobValTypeError("expected boolean");
    }
    return (Boolean) kv.value();
  }

  public static String needString(KnobVal kv) {
    if (kv.type() != KnobValType.STRING) {
      throw new KnobValTypeError("expected string");
    }
    return (String) kv.value();
  }


  /* convience methods, but these need to be removed and worked in as DSLs
   * i.e, we can expect every KnobType to conver to double */

  public static double forceDouble(KnobVal kv) {
    switch (kv.type()) {
      case INTEGER:
        return (double) needInteger(kv).intValue();
      case DOUBLE:
        return needDouble(kv);
      default:
        throw new RuntimeException("unexpected type");
    }
  }

  public static KnobVal haveInteger(Integer val) {
    return new IntegerKnobVal(val);
  }

  public static KnobVal[] haveIntegers(Integer ...vals) {
    KnobVal[] kvs = new KnobVal[vals.length];
    for (int i = 0; i < vals.length; i++) {
      kvs[i] = haveInteger(vals[i]);
    }
    return kvs;
  }

  public static KnobVal haveDouble(Double val) {
    return new DoubleKnobVal(val);
  }

  public static KnobVal[] haveDoubles(Double ...vals) {
    KnobVal[] kvs = new KnobVal[vals.length];
    for (int i = 0; i < vals.length; i++) {
      kvs[i] = haveDouble(vals[i]);
    }
    return kvs;
  }

  public static KnobVal haveBoolean(Boolean val) {
    return new BooleanKnobVal(val);
  }

  public static KnobVal[] haveBooleans(Boolean ...vals) {
    KnobVal[] kvs = new KnobVal[vals.length];
    for (int i = 0; i < vals.length; i++) {
      kvs[i] = haveBoolean(vals[i]);
    }
    return kvs;
  }

  public static KnobVal haveString(String val) {
    return new StringKnobVal(val);
  }

  public static KnobVal[] haveStrings(String ...vals) {
    KnobVal[] kvs = new KnobVal[vals.length];
    for (int i = 0; i < vals.length; i++) {
      kvs[i] = haveString(vals[i]);
    }
    return kvs;
  }



}
