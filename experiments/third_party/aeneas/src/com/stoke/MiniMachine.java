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
import com.stoke.util.*;
import com.stoke.eval.*;

import com.google.common.base.Stopwatch;

import java.util.*;
import java.util.concurrent.*;

import java.io.*;

import java.lang.StringBuilder;


/* Class is meant to represent a mini bandit machine. Essentiall, a self contained bandit that
 * we can run over and over, and *swap* other mini bandits with it. */
public class MiniMachine {

  Configuration[] _configurations    = null;
  Configuration   _selected          = null; 
  Map<String,Knob>  _inputKnobs = null;

  protected Map<Integer, Configuration> _configurationsMap = new HashMap<>();

  /* Running Q functions */
  Map<Integer, Double>  _leftRewards = new HashMap<>();
  Map<Integer, Double>  _lastRewards = new HashMap<>();
  Map<Integer, Integer> _numOns = new HashMap<>();

  Map<Integer, Double>  _rightRewards = new HashMap<>();


  double _totalLeftReward = 0.0;

  RewardType _rewardType;

  protected SamplingPolicy _samplingPolicy;

  /* Main Bandit logic: The combination of these two policies creates the 
   * individual bandit machines. */
  protected StochasticPolicy _stochasticPolicy = null;

  protected int       _internalStep = 0;
  protected int       _postConfigStep = 0;

  protected static int _totalStep = 0;

  protected boolean   _inSampling = true;

  private int         _taskDelay  = 0; 

  /* Actual energy measurement: We need a small layer so we can measure on both
   * linux and android */ 
  protected boolean _doneWithExperiment = false;
  protected boolean _tableLoaded = false;

  /* We will remove this (mostly) for a revised version on the bandit
   * machine, however it's useful for simulating the table based experiments
   * where we actually need to sample tasks */
  protected int     _numTaskSamples = 2;

  protected AeneasMachine _theBandit;

  Map<Integer, Integer>  _restoredNumOns = new HashMap<>();

  public MiniMachine(AeneasMachine theBandit,
      Map<String,Knob> inputKnobs,
      Configuration[] configurations,
      SamplingPolicy samplingPolicy,
      StochasticPolicyType stochasticPolicyType,
      RewardType rewardType) 
  {
    //System.err.format("GOT %s\n", stochasticPolicyType);

    _theBandit = theBandit;
    _inputKnobs = inputKnobs;
    _samplingPolicy = samplingPolicy;
    _configurations = configurations;
    _rewardType = rewardType;

    initPolicies(stochasticPolicyType);
    if (_samplingPolicy == SamplingPolicy.SAMPLE_NONE) {
      _inSampling = false;
    }


    // Build 
    for (int i = 0; i < _configurations.length; i++) {
      Configuration config = _configurations[i];
      _configurationsMap.put(config.getId(), config);
      _leftRewards.put(config.getId(), 0.0);
      _rightRewards.put(config.getId(), 0.0);
      _lastRewards.put(config.getId(), 0.0);
      _numOns.put(config.getId(), 0);
      _restoredNumOns.put(config.getId(), 0);
    }

    selectConfigurationByPos(0); 

    if (_theBandit._experiment == Experiment.OVERHEAD) {
      selectConfigurationByPos(_configurations.length-1);
    }
    if (_theBandit._experiment == Experiment.OVERHEAD2) {
      selectConfigurationByPos(_configurations.length-3);
    }

  }

  public void restoreState(Map<Integer, AeneasMachine.StatePreserve> states, Knob contKnob) {
    for (int i = 0; i < _configurations.length; i++) {
      Configuration c = _configurations[i];
      Integer interval = KnobValT.needInteger(c.getSettingForKnob(contKnob));
      if (states.get(interval) != null) {
        AeneasMachine.StatePreserve sp = states.get(interval);
        _leftRewards.put(c.getId(), sp._q);
        _numOns.put(c.getId(), sp._numOn);
        _lastRewards.put(c.getId(), sp._q);
        _restoredNumOns.put(c.getId(), sp._numOn);
      }
    }
  }

  protected boolean isPerformingHotRun() {
    return (_theBandit._experiment == Experiment.OFFLINE_COLLECT || _theBandit._experiment == Experiment.HARDWARE_SOFTWARE);
  } 

  public void initPolicies(StochasticPolicyType stochasticPolicyType) {
    switch (stochasticPolicyType) {
      case NO_STOCHASTIC:
        _stochasticPolicy = new NoStochastic(this, _configurations);
        break;
      case EPSILON_GREEDY_0:
        _stochasticPolicy = new EpsilonGreedyStochastic(this, _configurations, 0.0);
        break;
      case EPSILON_GREEDY_10:
        _stochasticPolicy = new EpsilonGreedyStochastic(this, _configurations, EpsilonGreedyStochastic.EPSILON);
        break;
      case EPSILON_GREEDY_50:
        _stochasticPolicy = new EpsilonGreedyStochastic(this, _configurations, 0.50);
        break;
      case SOFTMAX:
        _stochasticPolicy = new SoftmaxStochastic(this, _configurations);
        break;
      case VBDE_05:
        _stochasticPolicy = new VBDEStochastic(this, _configurations, 0.5); 
        break;
      case VBDE_10:
        _stochasticPolicy = new VBDEStochastic(this, _configurations, 1.0); 
        break;
      case VBDE_50:
        _stochasticPolicy = new VBDEStochastic(this, _configurations, 5.0); 
        break;
      case VBDE_200:
        _stochasticPolicy = new VBDEStochastic(this, _configurations, 20.0); 
        break;
      case UCB:
        // FIX : Rework the UCB policy (normalization) 
        _stochasticPolicy = new UCBStochastic(this, _configurations); 
        break;
    }
  }

  public void clearStep() {
  }

  public int step() {
    return _internalStep;
  }

  public int postConfigStep() {
    return _postConfigStep;
  }

  public void preStep(boolean skipFromOpt) { 
    if (_theBandit._experiment != Experiment.IGNORE && _theBandit._experiment != Experiment.DRAIN) {
      if (!skipFromOpt) {
        adjustKnob(_internalStep);
      }
    } /* else {
      //selectConfigurationById(0);
    }
    */
    if (_theBandit._experiment == Experiment.OVERHEAD) {
      selectConfigurationByPos(_configurations.length-1);
    }
    if (_theBandit._experiment == Experiment.OVERHEAD2) {
      selectConfigurationByPos(_configurations.length-3);
    }
  }

  public void postStep(double leftReward, double rightReward, long ms, double rawReward, double rawJoules) { 
    _internalStep++;
    _totalStep++;

    if (!_inDelay && !_doneWithExperiment) { 
      if (_theBandit._experiment != Experiment.IGNORE) {
        learn(leftReward, rightReward); 
        if (!_inSampling) {
          _stochasticPolicy.learn();
        }
      }

      _totalLeftReward += Math.abs(rawJoules); 

      // Logging
      System.err.format(
          "STOKE: ETask %d: Configuration:%d Energy:%.2f Power:%.2f Reward:%.2f Time:%d\n",
          _internalStep,
          _selected.getId(),
          0.0,
          0.0,
          leftReward,
          ms
          );

      StringBuilder sb = new StringBuilder("ETask ");
      sb.append(MiniMachine._totalStep);
      sb.append("-");
      sb.append(_internalStep);
      sb.append(": Configuration:");
      sb.append(_selected.getId());
      sb.append(" Energy:");
      sb.append(rawJoules);
      sb.append(" Reward:");
      sb.append(leftReward);
      sb.append(" Raw:");
      sb.append(rawReward);
      sb.append(" Time:");
      sb.append(ms);
      sb.append(" TotalReward:");
      sb.append(_totalLeftReward);
      sb.append("\n");

      LogUtil.writeLogger(sb.toString());

      if (!_inSampling) {
        _postConfigStep++;
      }

      dumpConfigurations(_configurations, false);
    }
  }

  public boolean doneWithExperiment() {
    return _doneWithExperiment;
  }

  protected void learn(double leftReward, double rightReward) {
    if (_inSampling || _stochasticPolicy.type() != StochasticPolicyType.NO_STOCHASTIC) {
      int selectedId = _selected.getId();
      _numOns.put(selectedId, _numOns.get(selectedId) + 1);

      double Qa = _leftRewards.get(selectedId);
      double alpha = 1.0 / (double) _numOns.get(selectedId);

      System.err.format("Alpha: %.2f LeftReward: %.2f\n", alpha, leftReward);

      double temporalDiff = alpha * (leftReward - Qa);
      double Qap1 = Qa + temporalDiff;

      _lastRewards.put(selectedId, Qa);
      _leftRewards.put(selectedId, Qap1);

      System.err.format("STOKE: Qa:%.2f Delta:%.2f Qa+1:%.2f\n", Qa, temporalDiff, Qap1);

      _rightRewards.put(selectedId, _rightRewards.get(selectedId) + rightReward);
    } 
  } 

  public double qvalue(Configuration c) {
    return qvalue(c.getId());
  }

  public double qvalue(int id) {
    return _leftRewards.get(id);
  }

  /*
  protected double scaledReward(double left, double right, int numOn) {
    //double leftReward = _rewardType.scaleLeft(left, numOn);
    double leftReward = left / numOn;
    double rightReward = _rewardType.scaleRight(right, numOn);

    switch (_rewardType) {
      case JOULES:
      case WATTS:
      case BATTERY:
        leftReward = -leftReward;
        break;
      case OPTIMIZER:
        rightReward = 1.0 - rightReward;
        break;
      case CONSTRAINED:
        leftReward = 1.0 - (Math.abs(leftReward - (_rewardType.distance() / _rewardType.leftUpperBound())));
        break;
      case CONSTRAINED2:
        double distance = (_rewardType.distance() / _rewardType.leftUpperBound()) - leftReward;
        boolean halve = false;
        if (distance < 0) {
          halve = true;
          distance *= -1;
        }
        leftReward = 1.0 - distance;
        if (halve) {
          leftReward *= 0.5;
        }
        break;
    }

    if (_rewardType == RewardType.OPTIMIZER) {
      return leftReward * rightReward;
    } else {
      return leftReward;
    }
  }

  protected double scaledReward(Configuration c) {
    int id = c.getId();
    double leftReward = _leftRewards.get(id);
    //double leftReward = _rewardType.scaleLeft(_leftRewards.get(id), _numOns.get(id));
    double rightReward = _rewardType.scaleRight(_rightRewards.get(id), _numOns.get(id));

    switch (_rewardType) {
      case JOULES:
      case WATTS:
      case BATTERY:
        leftReward = -leftReward;
        break;
      case OPTIMIZER:
        rightReward = 1.0 - rightReward;
        break;
      case CONSTRAINED:
        leftReward = 1.0 - (Math.abs(leftReward - (_rewardType.distance() / _rewardType.leftUpperBound())));
        break;
      case CONSTRAINED2:
        double distance = (_rewardType.distance() / _rewardType.leftUpperBound()) - leftReward;
        boolean halve = false;
        if (distance < 0) {
          halve = true;
          distance *= -1;
        }
        leftReward = 1.0 - distance;
        if (halve) {
          leftReward *= 0.5;
        }
        break;
    }

    if (_rewardType == RewardType.OPTIMIZER) {
      return leftReward * rightReward;
    } else {
      return leftReward;
    }

    double reward = 
      ((1.0 - _rewardType.alpha()) * leftReward) +
      (_rewardType.alpha() * rightReward);
    return reward;
  }
  */

  /* Functions and globals related to Bandit sampling 
   * */
  private boolean _skipFirst = true; 

  protected void finalize() {
    _stochasticPolicy.argMaxSelect(0);
  }

  protected void adjustKnob(int step) {
    handleSampling(step);
    if (_inSampling) {
      return;
    }

    Configuration next = null;
    if (_stochasticPolicy.shouldRandomize()) {
      next = _stochasticPolicy.stochasticSelect(step);
    } else {
      next = _stochasticPolicy.argMaxSelect(step);
    }

    /* TODO: This needs to be moved into the specific lattice policy */
    /*
    if (!_skipFirst) {
      if (_latticePolicy.lessThan(next, _selected)) {
        _latDir = LatticeDirection.DOWN;
      }
      if (_latticePolicy.greaterThan(next, _selected)) {
        _latDir = LatticeDirection.UP;
      }
    } else {
      _skipFirst = false;
    }
    */

    selectConfigurationById(next.getId()); 
  } 

  protected boolean _inDelay = false;

  public boolean inSampling() {
    return _inSampling;
  }

  private void loadOfflineTable() {
    try {
      BufferedReader br = new BufferedReader(new FileReader("/sdcard/com.stoke/com.stoke.table"));
      String l = br.readLine();
      System.err.format("Read:%s\n", l);
      String[] tokens = l.split(" ");
      for (int i = 0; i < _configurations.length; i++) {
        Configuration c = _configurations[i];
        l = br.readLine();
        System.err.format("Read:%s\n", l);
        tokens = l.split(" ");
        int id = Integer.parseInt(tokens[0].split(":")[1]);
        double reward = Double.parseDouble(tokens[1].split(":")[1]);
        int numOn = Integer.parseInt(tokens[2].split(":")[1]);
        if (c.getId() != id) {
          throw new RuntimeException("Error when reading offline table!");
        }
        // TODO : left reward hack
        _leftRewards.put(c.getId(), reward);
        _numOns.put(c.getId(), numOn);
      }

    } catch (Exception e) {
      throw new RuntimeException("Unable to load table!" + e);
    }

  }


  protected void handleSampling(int step) {
    if (_samplingPolicy == SamplingPolicy.OFFLINE_PROFILE && !_tableLoaded) {
      loadOfflineTable();
      _tableLoaded = true;
      _inSampling = false;
    } else {
      sampleConfigurations(step);
    }
  } 

  protected boolean sampleAll(int step) {
    if (step < (_configurations.length * _numTaskSamples)) {
      if (step % _numTaskSamples != 0) {
        return true;
      }
      int next = step / _numTaskSamples;
      if (_samplingPolicy == SamplingPolicy.SAMPLE_ALL) {
        selectConfigurationByPos(next);
      } else if (_samplingPolicy == SamplingPolicy.SAMPLE_REVERSE) {
        selectConfigurationByPos(_configurations.length - 1 - next);
      } else {
        throw new RuntimeException("Should never reach!");
      }

      String configstr = "";
      for (int i = 0; i < _inputKnobs.size(); i++) {
        configstr += String.format("%d ", _selected.getPosition(i));
      }

      return true;
    } 
    _inSampling = false;
    return false;
  } 

  // FIX : This doesn't work anymore. No more meter readings. Need to pull out.
  protected boolean sampleOfflineCollection(int step) {
    int cycleSteps = _numTaskSamples + _taskDelay;

    if (step < ((_configurations.length * cycleSteps)-_taskDelay)) {
      int currentStep = step % cycleSteps;

      // Start of cycle reading, -- active samples
      if (currentStep == 0) {
        _inDelay = false; 
        //_startDrain = _lastDrain; 
        //LogUtil.writeLogger(String.format("ECycle-Start %d: Start-Drain:%.2f\n", _selected.getId(),_startDrain));


      // Start of cycle delay -- configuration is switched
      } else if (step % cycleSteps >= _numTaskSamples) {
        if (!_inDelay) {

          //LogUtil.writeLogger(String.format("ECycle-Start %d: End-Drain:%.2f\n", _selected.getId(),_lastDrain));

          // Logging
          //System.err.format(
           // "ECycle %d: Energy:%.2f Power:%.2f\n",
            //_selected.getId(),
            //_theBandit._meter.asJoules(diffReading).get("total"),
            //_theBandit._meter.asWatts(diffReading).get("total")
            //); 

          LogUtil.writeLogger(
              String.format(
                "ECycle %d: Energy:%.2f Power:%.2f\n",
                _selected.getId(),
                0,
                0
                )); 

          int cyclesPassed = step / cycleSteps;
          int next = (step - (cyclesPassed * _taskDelay)) / _numTaskSamples;
          selectConfigurationByPos(next);
        }
        _inDelay = true;
      }

      return true;
    } else {
      if (_inSampling) {
        _inSampling = false;
        _doneWithExperiment = true;

        //System.err.format(
         // "ECycle %d: Energy:%.2f Power:%.2f\n",
         // _selected.getId(),
         // _theBandit._meter.asJoules(diffReading).get("total"),
         // _theBandit._meter.asWatts(diffReading).get("total")
         // ); 

        LogUtil.writeLogger(
              String.format(
                "ECycle %d: Energy:%.2f Power:%.2f\n",
                _selected.getId(),
                0,
                0
                )); 
      }
    }
    return false;
  }

  private Knob _highPrioKnob = null;

  /*
  protected boolean samplePriority(int step) {
    if (_highPrioKnob == null) {
      _highPrioKnob = _theBandit.findHighestPriorityKnob();
      //System.err.format("STOKE: Selecting %s as the high priority knob\n", _highPrioKnob);
    }

    if (step % _numTaskSamples != 0) {
      return false;
    }

    int nextSetting = step / _numTaskSamples;

    if (nextSetting != 0) {
      Configuration[] configs = 
        _latticePolicy.getFromSettings(_highPrioKnob.name(), nextSetting-1);
      for (int i = 0; i < configs.length; i++) {
        Configuration c = configs[i]; 
        _leftRewards.put(c.getId(), _leftRewards.get(_selected.getId()));
        _rightRewards.put(c.getId(), _rightRewards.get(_selected.getId()));
        _numOns.put(c.getId(), _numOns.get(_selected.getId()));
      }
    }
    
    if (nextSetting >= _highPrioKnob.numPos()) {
      _inSampling = false;
      return true;
    } 

    Configuration nextSample = 
      _latticePolicy.getFromSettings(_highPrioKnob.name(), nextSetting)[0];

    selectConfigurationById(nextSample.getId()); 

    String configstr = "";
    for (int i = 0; i < _inputKnobs.size(); i++) {
      configstr += String.format("%d ", _selected.getPosition(i));
    }

    //System.err.format("Exercising configuration %s\n", configstr);
    
    return true;
  }
  */

  protected boolean sampleConfigurations(int step) {
    if (_inSampling) {
      switch (_samplingPolicy) {
        case SAMPLE_ALL:
        case SAMPLE_REVERSE:
          if (isPerformingHotRun()) {
            sampleOfflineCollection(step);
          } else {
            sampleAll(step);
          }
          break;
        case SAMPLE_PRIORITY:
          //samplePriority(step);
          break;
      }
      return true;
    }
    
    return false;
  }
  
  /* Various selection methods that select an actual configuration for the 
   * machine to use. */
  protected void selectConfigurationByPos(int i) {
    selectConfiguration(_configurations[i]);
  }

  protected void selectConfigurationById(int id) {
    selectConfiguration(_configurationsMap.get(id));
  }

  protected void selectConfiguration(Configuration config) {
    config.select();
    _selected = config;
  } 

  public int currentConfiguration() {
    return _selected.getId();
  }

  public void setNumTaskSamples(int samples) {
    _numTaskSamples = samples;
  }

  public void setTaskDelay(int taskDelay) {
    _taskDelay = taskDelay;
  } 

  /* Having to dump configurations based on a specific order is useful even
   * externally to the bandit. */
  public void dumpConfigurations(Configuration[] configs, boolean dumpToLog) {
    if (dumpToLog) {
      for (int i = 0; i < configs.length; i++) {
        Configuration c = configs[i];
        int numOn = _numOns.get(c.getId());
        double reward = qvalue(c);
        LogUtil.writeLogger(String.format("STOKE: Config: %s | -- on:%d reward:%.4f\n", c.toString(), numOn, reward));
      }
    } else {
      for (int i = 0; i < configs.length; i++) {
        Configuration c = configs[i];
        int numOn = _numOns.get(c.getId());
        double reward = qvalue(c);
        System.err.format("STOKE: Config: %s | -- on:%d reward:%.4f\n", c.toString(), numOn, reward);
      }
    }
  }
}
