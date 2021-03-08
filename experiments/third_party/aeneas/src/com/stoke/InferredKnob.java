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

public class InferredKnob extends Knob {
  int _highLimit;
  int _lowLimit;

  public InferredKnob(String name, int high, int low, int interpolation) {
    super(name, null, 0);  
    _highLimit = high;
    _lowLimit = low;

    int range = high - low;
    int mid = (high + low) / 2; 
    List<Integer> seeds = AeneasMachine.seed(high, mid, low, range, interpolation);

    KnobVal[] settings = new KnobVal[seeds.size()];
    for (int i = 0; i < settings.length; i++) {
      settings[i] = KnobValT.haveInteger(seeds.get(i));
    }

    _settings = settings;
  }

  public InferredKnob(String name, int high, int low, Integer[] seeds) {
    super(name, null, 0);  
    _highLimit = high;
    _lowLimit = low;
    Arrays.sort(seeds, new Comparator<Integer>() {
      @Override
      public int compare(Integer l, Integer r) {
        return Integer.compare(r, l);
      }
    });

    KnobVal[] settings = new KnobVal[seeds.length + 2];
    for (int i = 1; i < settings.length-1; i++) {
      settings[i] = KnobValT.haveInteger(seeds[i-1]);
    }
    settings[0] = KnobValT.haveInteger(high);
    settings[settings.length-1] = KnobValT.haveInteger(low);

    _settings = settings;
  }

}

