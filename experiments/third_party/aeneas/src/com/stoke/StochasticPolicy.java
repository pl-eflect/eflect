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

public abstract class StochasticPolicy { 

  protected Configuration[]   _configurations = null;

  protected MiniMachine _miniBandit = null;

  public StochasticPolicy(MiniMachine miniBandit, Configuration[] configurations) {
    _miniBandit = miniBandit;
    _configurations = configurations;
  }

  public abstract boolean shouldRandomize();

  public abstract Configuration stochasticSelect(int step);

  public Configuration argMaxSelect(int step) {
    double minScore = Double.MAX_VALUE;
    int minInd = 0;
    for (int i = 0; i < _miniBandit._configurations.length; i++) {
      Configuration c = _miniBandit._configurations[i];
      //double escore = _bandit.scaledReward(c);
      double reward = Math.abs(_miniBandit.qvalue(c));
      if (Double.compare(reward, minScore) < 0) {
        minScore = reward;
        minInd = i;
      }
    }
    Configuration ot = _miniBandit._configurations[minInd];
    return ot;
  }

  public abstract void learn();

  public abstract StochasticPolicyType type();

}

