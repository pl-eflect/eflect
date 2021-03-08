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

public enum StochasticPolicyType {

  NO_STOCHASTIC,
  EPSILON_GREEDY_0,
  EPSILON_GREEDY_10,
  EPSILON_GREEDY_50,
  SOFTMAX ,
  VBDE_05 ,
  VBDE_10 ,
  VBDE_50 ,
  VBDE_200 ,
  UCB ;

  public static StochasticPolicyType toStochasticPolicy(String s) {
    switch (s) {
      case "NO_STOCHASTIC":
        return StochasticPolicyType.NO_STOCHASTIC;
      case "EPSILON_GREEDY_0":
        return StochasticPolicyType.EPSILON_GREEDY_0;
      case "EPSILON_GREEDY_10":
        return StochasticPolicyType.EPSILON_GREEDY_10;
      case "EPSILON_GREEDY_50":
        return StochasticPolicyType.EPSILON_GREEDY_50;
      case "SOFTMAX":
        return StochasticPolicyType.SOFTMAX;
      case "VBDE_05":
        return StochasticPolicyType.VBDE_05;
      case "VBDE_10":
        return StochasticPolicyType.VBDE_10;
      case "VBDE_50":
        return StochasticPolicyType.VBDE_50;
      case "VBDE_200":
        return StochasticPolicyType.VBDE_200;
      case "UCB":
        return StochasticPolicyType.UCB;
      default:
        throw new RuntimeException("Error: Trying to select stochastic policy " + s + ". Does not exist.");
    }
  }
}
