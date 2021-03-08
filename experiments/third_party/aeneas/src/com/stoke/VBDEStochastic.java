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

public class VBDEStochastic extends StochasticPolicy {

  final protected static double SIGMA = 1;

  protected Random _random = new Random();
  protected double _sigma = SIGMA;

  protected double _ep = 1.0;

  public VBDEStochastic(MiniMachine miniBandit, Configuration[] configurations, double sigma) {
    super(miniBandit, configurations);
    _sigma = sigma;
  }

  public boolean shouldRandomize() {
    //double ep = calculateEpsilon();
    return (_random.nextDouble() < _ep);
  }

  public void learn() {
    calculateEpsilon();
  }

  protected void calculateEpsilon() {
    double q_t = _miniBandit._leftRewards.get(_miniBandit._selected.getId());
    double q_tm1 = _miniBandit._lastRewards.get(_miniBandit._selected.getId());


    double qdiff = (-Math.abs(q_t - q_tm1));
    double ex = Math.pow(Math.E, (qdiff / _sigma));
    double delta = 1.0 / _configurations.length;
    double f = (1.0-ex)/(1.0+ex);
    _ep = 
        (delta * f) +
        ((1-delta) * _ep);

    // Roll some stuff
    System.err.format("STOKE: Config:%d  Q_t:%f  Q_tm1:%f  qdiff:%f  f:%f  ep_t:%f\n", 
        _miniBandit._selected.getId(), q_t, q_tm1, qdiff, f, _ep);
    //LogUtil.writeLogger(String.format("STOKE: Config:%d  Q_t:%f  Q_tm1:%f  qdiff:%f  f:%f  ep_t:%f\n", _miniBandit._selected.getId(), q_t, q_tm1, qdiff, f, ep_t));
  }

  public Configuration stochasticSelect(int step) {
    return _configurations[_random.nextInt(_configurations.length)];
  }

  public StochasticPolicyType type() {
    return StochasticPolicyType.VBDE_05;
  }
}
