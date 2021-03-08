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

public class SoftmaxStochastic extends StochasticPolicy {

  protected static double TEMPERATURE = 0.05;
  protected Random _random = new Random();

  public SoftmaxStochastic(MiniMachine bandit, Configuration[] configurations) {
    super(bandit, configurations);
    System.err.format("STOKE: Loaded softmax!\n");
  }

  public boolean shouldRandomize() {
    return true;
  }

  public void learn() { }

  public Configuration stochasticSelect(int step) {
    throw new RuntimeException("TODO : Need to re-implement Softmax");
    /*
    double[] rewards = new double[_configurations.length];
    double[] ratings = new double[_configurations.length];

    double totalReward = 0;
    double totalRating = 0;

    for(int i = 0; i < _configurations.length; i++) {
      rewards[i] = Math.exp(-_configurations[i].getRewardMean() / TEMPERATURE);
      ratings[i] = Math.exp(KnobValT.needDouble(_configurations[i].getRecording(AeneasMachine.FEEDBACK_KNOB)) / TEMPERATURE) / 1000;

      totalReward += rewards[i];
      totalRating += ratings[i]; 
    }

    double guess = _random.nextDouble();
    double prob = 0;
    for(int i = 0; i < _configurations.length; i++) {
      prob += (rewards[i] / totalReward + ratings[i] / totalRating) / 2;
      if(prob < guess) {
        return _configurations[i];
      }
    }

    return _configurations[_configurations.length - 1];
    */
  }

  public StochasticPolicyType type() {
    return StochasticPolicyType.SOFTMAX;
  }
}
