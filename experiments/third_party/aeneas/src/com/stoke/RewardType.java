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

public enum RewardType {
  JOULES(5.0, 1.0, 0.0, 0.0),
  WATTS(50.0, 1.0, 0.0, 0.0),
  BATTERY(5.0, 1.0, 0.0, 0.0),
  CONSTRAINED(5.0, 1.0, 0.0, 0.0),
  CONSTRAINED2(5.0, 1.0, 0.0, 0.0),
  OPTIMIZER(1.0, 10000.0, 0.25, 0.0);

  private double _leftUpperBound;
  private double _rightUpperBound;
  private double _alpha;
  private double _distance;

  RewardType(double leftUpperBound, double rightUpperBound, double alpha, double distance) {
    _leftUpperBound = leftUpperBound;
    _rightUpperBound = rightUpperBound;
    _alpha = alpha;
    _distance = distance;
  }

  public double leftUpperBound() {
    return _leftUpperBound;
  }

  public double rightUpperBound() {
    return _rightUpperBound;
  }

  public double alpha() {
    return _alpha;
  }
  
  public void adjustLeft(double reward) {
    if (this == RewardType.CONSTRAINED2 && _distance > reward) {
      reward = _distance;
    }
    if (reward >= _leftUpperBound) {
      _leftUpperBound = reward * 1.5;
    }
  }

  public void adjustRight(double reward) {
    if (reward >= _rightUpperBound) {
      _rightUpperBound = reward * 1.5;
    }
  }

  public void setDistance(double distance) {
    _distance = distance;
  }

  public double distance() {
    return _distance;
  }


  public double scaleLeft(double reward, int numOn) {
    return (reward / numOn) / _leftUpperBound;
  }

  public double scaleRight(double reward, int numOn) {
    return (reward / numOn) / _rightUpperBound;
  }

  public static RewardType toRewardType(String s) {
    switch (s) {
      case "JOULES":
        return RewardType.JOULES;
      case "WATTS":
        return RewardType.WATTS;
      case "BATTERY":
        return RewardType.BATTERY;
      case "OPTIMIZER":
        return RewardType.OPTIMIZER;
      default:
        throw new RuntimeException("Error: Trying to load RewardType " + s + ". Does not exist.");
    }
  }

  public String toString() {
    switch (this) {
      case JOULES:
        return "JOULES";
      case WATTS:
        return "WATTS";
      case BATTERY:
        return "BATTERY";
      case OPTIMIZER:
        return "OPTIMIZER";
      default:
        return "UNEXPECTED";
    }
  }
}
