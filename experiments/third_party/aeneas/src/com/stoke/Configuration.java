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

package com.stoke;

import com.stoke.types.*;

import java.util.*;

// A Configuration is a mapping from a system configuration (knob settings)
// and recorded recordings and reward consumed

public class Configuration {
  public enum Relation {
    BELOW,
    EQUAL,
    ABOVE,
    UNORDERED,
  }; 

  private static int idGen = 0;

  private int[] _positions;
  private Knob[] _knobs;

  private Map<String, KnobVal> _records;
  private Map<String, Integer> _numOns;


  private Recording[] _recordings;
  private int _id;

  // HACK for reward (for now)
  private double rewardMean = 0;
  private double rewardVariance = 0;
  private int k = 0;

  public Configuration(int[] positions, Knob[] knobs, Recording[] recordings) {
    _positions = positions;
    _knobs = knobs;
    _recordings = recordings;

    _records = new HashMap<>();
    _numOns = new HashMap<>();

    for (int i = 0; i < _recordings.length; i++) {
      _records.put(_recordings[i].name(), 
          _recordings[i].defaultKnobVal());
      _numOns.put(_recordings[i].name(), 0);
    }

    _id = Configuration.idGen;
    Configuration.idGen++;
  }

  public int getId() { return _id; }

  public static void resetIdGen() {
    Configuration.idGen = 0;
  }

  public void select() {
    for (int i = 0; i < _positions.length; i++) {
      _knobs[i].setPos(_positions[i]);
    }
  }

  public int getNumOn() { return _numOns.get("reward"); }

  public void setNumOn(int numOn) { _numOns.put("reward", numOn); }

  public int getPosition(int i) { return _positions[i]; }

  public Knob getKnob(int i) { return _knobs[i]; }

  public int getNumPositions() { return _positions.length; }

  public KnobVal getSettingForKnob(Knob knob) {
    for (int i = 0; i < _knobs.length; i++) {
      if (_knobs[i].name().equals(knob.name())) {
        return knob.getSettingAtPos(_positions[i]);
      }
    }
    return null;
  }

  public void updateRecording(String name, KnobVal val) {
    if (name.equals("reward")) {
      this.k++;
      double x = KnobValT.needDouble(val);
      // HACK for reward (for now)
      if (this.k == 1) {
        this.rewardMean = x;
        this.rewardVariance = 0;
      } else {
        double lastMean = this.rewardMean;
        double lastVariance = this.rewardVariance;
        this.rewardMean = lastMean + ((x - lastMean) / this.k);

        this.rewardVariance =
            (lastVariance + ((x - lastMean) * (x - this.rewardMean)));
      }
    }

    KnobVal cur = _records.get(name);
    _records.put(name, cur.add(val));
    _numOns.put(name, _numOns.get(name)+1);
  }

  public double getRewardMean() { return this.rewardMean; }

  public double getRewardVariance() { 
    return (k > 1) ? this.rewardVariance / (this.k-1) : 0.0; 
  }

  public KnobVal getRecording(String name) {
    return _records.get(name);
  }

  public Map<String,KnobVal> getRecords() {
    return _records;
  }

  public KnobVal getRecordingDivd(String name) {
    if (_numOns.get(name) == 0) {
      return new DoubleKnobVal(0.0);
    }
    return _records.get(name).dividedBy(_numOns.get(name));
  }

  public void setRecording(String name, KnobVal val) {
    _records.put(name, val);
  }

  public String toString() {
    String s = String.format("%d - Knobs: {", _id);
    for (int i = 0; i < _positions.length; i++) {
      s += String.format("%s:%s ", _knobs[i].name(), _knobs[i].getSettingAtPos(_positions[i]));
    }
    //s += "} Records:{";
    s += "} ";
    /*
    for (Map.Entry<String, KnobVal> e : _records.entrySet()) {
      if (e.getKey().equals("reward") || e.getKey().equals("feedback")) {
        continue;
      }
      s += String.format("%s:%s", e.getKey(), e.getValue().dividedBy(_numOns.get(e.getKey())));
    }
    s += "}";
    */
    return s;
  }

  public String tupleString() {
    String configstr = "(";
    for (int i = 0; i < _knobs.length; i++) {
      configstr += String.format("%d ", getPosition(i));
    }
    configstr += ")";
    return configstr;
  }

  public static Relation compareConfigurations(Configuration c1, Configuration c2) {
    if (c1 == c2) return Relation.EQUAL;
    boolean allAbove = true;
    boolean allBelow = true;
    for (int i = 0; i < c1._knobs.length; i++) {
      if (c1._positions[i] < c2._positions[i]) {
        allAbove = false;
      } else if (c1._positions[i] > c2._positions[i]) {
        allBelow = false;
      }
    }
    if (allAbove) {
      return Relation.ABOVE;
    } else if (allBelow) {
      return Relation.BELOW;
    } else {
      return Relation.UNORDERED;
    } 
  }
}
