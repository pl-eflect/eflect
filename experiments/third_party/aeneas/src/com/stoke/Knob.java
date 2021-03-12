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

public abstract class Knob {
  private int _curPos = 0;

  private final String    _name;
  protected KnobVal[] _settings;
  private final int _priority;

  public Knob(String name, KnobVal[] settings, int priority) {
    _name     = name;
    _settings = settings;
    _priority = priority;
  }

  public String name() {
    return _name;
  }

  public int numPos() {
    return _settings.length;
  }

  public void changeSettings(KnobVal[] settings) {
    _settings = settings;
    _curPos = 0;
  }

  public void setPos(int pos) { _curPos = pos; }

  public int getPos() { return _curPos; }

  public KnobVal getSetting() { return _settings[_curPos]; }

  public KnobVal getSettingAtPos(int pos) { return _settings[pos]; }

  public KnobVal[] getSettings() { return _settings; }

  public int getPriority() { return _priority; } 

  public String toString() {
    return String.format("%s (Current Pos: %d)", _name, _curPos);
  }
}
