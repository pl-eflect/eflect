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

public class Constraint {
  protected Recording _recording;
  protected KnobVal _required;
  protected boolean _isMin;

  public Constraint(Recording recording, KnobVal required, boolean isMin) {
    if (recording.knobValType() != required.type()) {
      throw new RuntimeException(
          String.format("Recording %s expected type %s, got %s", 
            recording, recording.knobValType(), required.type()));
    }
    _recording = recording;
    _required  = required;
    _isMin = isMin;
  }

  public Recording getRecording() {
    return _recording;
  }

  public KnobVal getRequired() {
    return _required;
  }

  public boolean isMin() {
   return _isMin;
  }
}
