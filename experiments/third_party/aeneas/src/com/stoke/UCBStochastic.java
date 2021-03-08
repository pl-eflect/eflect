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

import java.lang.Math;
import java.util.Random;

import com.stoke.types.KnobValT;

public class UCBStochastic extends StochasticPolicy {

  public UCBStochastic(MiniMachine machine, Configuration[] configurations) {
    super(machine, configurations);
  }

  public boolean shouldRandomize() {
    return true;
  }

  public void learn() { }

  private double ucb(double exploit, int n, int t) {
    return exploit + (double)Math.sqrt((2 * Math.log(t)) / n);
  }

  public Configuration stochasticSelect(int curIter) {
    double ucbAbsolute = -Double.MAX_VALUE;
    int ucbI = -1;

    for (int i = 0; i < _configurations.length; i++) {
      Configuration c = _configurations[i];
      double qt = Math.abs(_miniBandit._leftRewards.get(c.getId()));
      qt = 1 - (qt / 200.0);
      double ucbCur = ucb(qt, _miniBandit._numOns.get(c.getId()), curIter);
      if (ucbCur > ucbAbsolute) {
          ucbAbsolute = ucbCur;
          ucbI = i;
      }
    }
    return _configurations[ucbI];
  }

  public StochasticPolicyType type() {
    return StochasticPolicyType.UCB;
  }
}
