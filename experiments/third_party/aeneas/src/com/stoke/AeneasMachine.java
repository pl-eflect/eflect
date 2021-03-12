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

import com.google.common.base.Stopwatch;
import com.stoke.eval.*;
import com.stoke.types.*;
import com.stoke.util.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class AeneasMachine implements Runnable {
  protected static String REWARD_KNOB = "reward";
  protected static String FEEDBACK_KNOB = "feedback";

  /* Actual Bandit "state": Most other fields are related to experimental
   * book keeping. */
  Map<String, Knob> _inputKnobs = new LinkedHashMap<>();
  Map<String, Recording> _recordings = new LinkedHashMap<>();
  Recording[] _userRecordings = null;
  Recording _rewardKnob = null;

  /* Bandits abstract over a set of configurations of Knobs. We must generate
   * all possible configurations given a set of inputs, and currently have one
   * configuration "selected" at any point. */
  protected Configuration[] _configurations = null;

  protected static int QUALITY_WINDOW = 1;
  protected int[] _configWindow = new int[QUALITY_WINDOW];

  protected int _selfOptimizeTicks = 0;

  protected int _banditStep = 0;
  private int _taskSkip = 0;

  private int _repeatTick = 0;
  private int _repeatTaskReset = 0;
  private int _repeatTotal = 0;
  private int _runCount = 0;

  /* For extra task tracking */
  protected Stopwatch _totalWatch = Stopwatch.createUnstarted();
  protected Stopwatch _taskWatch = Stopwatch.createUnstarted();

  /* Stuff related to our new binary bandit search. */
  protected boolean _isPaused = false;

  protected SamplingPolicy _samplingPolicy;
  protected StochasticPolicyType _stochasticPolicyType;
  protected StochasticPolicyType _originalPolicyType;
  protected ConfigurationSetPolicy _configurationSetPolicy;

  protected MiniMachine _miniMachine = null;

  /* Use for experimental purposes only, and not meant to be part of
   * an exposed API, i.e., this is the centralized hacking for ad-hoc
   * settings. */
  protected Experiment _experiment = Experiment.MACHINE;

  /* Constant scaling to transform recorded energy values into a
   * bernouli distribution */
  protected RewardType _rewardType;
  protected double _rscale = 0.0;

  /* Meter     _meter;
  MeterReading _startReading;
  MeterReading _diffReading; */

  /* Ugly state save for restarting mini bandits */
  protected int _taskDelay;
  protected int _numTaskSamples;

  protected boolean _usingFixedSelfOptimizeInterval = false;
  protected boolean _inFinalSelfOptimizeTicks = false;
  protected int _finalSelfOptimizeTicks = 0;

  public int MIN_STEPS = 15;
  public int MAX_STEPS = 30;
  public int FALLBACK_STEPS = 124;

  // public int FALLBACK_STEPS = 500;

  private InferredKnob _continuousKnob = null;

  List<Double> _lastRoundQStack = new ArrayList<Double>();
  List<Integer> _lastRoundPickStack = new ArrayList<Integer>();

  /* Hacking for Cross-Iteration State Preservation */
  public class StatePreserve {
    public double _q;
    public int _numOn;

    public StatePreserve(double joules, int numOn) {
      _q = joules;
      _numOn = numOn;
    }
  }

  Map<Integer, StatePreserve> _states = new HashMap<>();

  List<SeedRange> _seedStack = new ArrayList<SeedRange>();

  public class SeedRange {
    public int _slow;
    public int _fast;
    public int _mid;
    public KnobVal[] _settings;

    public SeedRange(int slow, int fast, int mid) {
      _slow = slow;
      _fast = fast;
      _mid = mid;
    }

    public SeedRange(KnobVal[] settings) {
      _settings = settings;
    }
  }

  public Reward _reward;

  // User-facing API
  public AeneasMachine(
      StochasticPolicyType stochasticPolicyType, Knob[] inputKnobs, Reward reward) {
    this(
        inputKnobs,
        new Recording[] {},
        null,
        stochasticPolicyType,
        SamplingPolicy.SAMPLE_ALL,
        reward,
        Experiment.MACHINE,
        false);
  }

  public AeneasMachine(
      Knob[] inputKnobs,
      Recording[] recordings,
      Constraint constraint,
      StochasticPolicyType stochasticPolicyType,
      SamplingPolicy samplingPolicy,
      Reward reward,
      Experiment experiment,
      boolean useSelfOptimizer) {

    _experiment = experiment;
    _samplingPolicy = samplingPolicy;
    _stochasticPolicyType = stochasticPolicyType;
    _originalPolicyType = _stochasticPolicyType;
    _constraint = constraint;
    _reward = reward;

    Integer slotsI = (Integer) AndroidUtil.getProperty("STOKE_DIVIDE_SLOTS");
    if (slotsI != null) {
      _slots = slotsI;
    }

    for (int i = 0; i < inputKnobs.length; i++) {
      Knob ik = inputKnobs[i];
      if (ik instanceof InferredKnob) {
        _performContinousLogic = true;
        _continuousKnob = (InferredKnob) ik;
        break;
      }
    }

    for (int i = 0; i < inputKnobs.length; i++) {
      _inputKnobs.put(inputKnobs[i].name(), inputKnobs[i]);
    }
    if (_experiment == Experiment.IGNORE) {
      _samplingPolicy = SamplingPolicy.SAMPLE_NONE;
    }

    _userRecordings = recordings;

    /* Everything from here on inits stuff for experimental evaluation */
    // LogUtil.initLogger();

    initConfigurations();

    // _meter = new Meter();

    if (useSelfOptimizer) {
      initializeSelfOptimizer();
    } else {
      _miniMachine =
          new MiniMachine(
              this,
              _inputKnobs,
              _configurations,
              _samplingPolicy,
              _stochasticPolicyType,
              _rewardType);
    }

    Integer minSteps = (Integer) AndroidUtil.getProperty("STOKE_MIN_STEPS");
    Integer maxSteps = (Integer) AndroidUtil.getProperty("STOKE_MAX_STEPS");
    if (minSteps != null) {
      MIN_STEPS = minSteps;
    }
    if (maxSteps != null) {
      MAX_STEPS = maxSteps;
    }
  }

  protected void resetConfigurations() {
    _banditStep = 0;
    _repeatTick = 0;
    Configuration.resetIdGen();
    initConfigurations();
    _miniMachine =
        new MiniMachine(
            this,
            _inputKnobs,
            _configurations,
            _samplingPolicy,
            _stochasticPolicyType,
            _rewardType);
    _miniMachine.setNumTaskSamples(_numTaskSamples);
    _miniMachine.setTaskDelay(_taskDelay);
  }

  protected boolean performContinuousKnobDivision(
      boolean shouldTighten, KnobVal[] settings, int winId) {
    _miniMachine.dumpConfigurations(_miniMachine._configurations, false);
    _miniMachine.dumpConfigurations(_miniMachine._configurations, true);

    for (int i = 0; i < _miniMachine._configurations.length; i++) {
      Configuration c = _miniMachine._configurations[i];
      int interval = KnobValT.needInteger(c.getSettingForKnob(_continuousKnob));
      double joules = _miniMachine._leftRewards.get(c.getId());
      int numOn = _miniMachine._numOns.get(c.getId());
      _states.put(interval, new StatePreserve(joules, numOn));
    }

    if (winId != -1) {
      _lastRoundQStack.add(_miniMachine._leftRewards.get(winId));
      _lastRoundPickStack.add(
          KnobValT.needInteger(
              _miniMachine._configurations[winId].getSettingForKnob(_continuousKnob)));
    }

    LogUtil.writeLogger("==ROUND-END==\n");
    if (settings == null) {
      populateKnobFromEdges(shouldTighten, winId);
    } else {
      _seedStack.add(
          new SeedRange(
              Arrays.copyOf(_continuousKnob.getSettings(), _continuousKnob.getSettings().length)));
      _continuousKnob.changeSettings(settings);
    }

    if (winId == -1) {
      _lastRoundQStack.remove(_lastRoundQStack.size() - 1);
      _seedStack.remove(_seedStack.size() - 1);
      _lastRoundPickStack.remove(_lastRoundPickStack.size() - 1);
    }

    _continuousStepTick = 0;
    _continuousRoundTick++;

    // Reset the configurations / mini bandit
    Configuration.resetIdGen();
    Knob[] inputKnobs = (Knob[]) _inputKnobs.values().toArray(new Knob[] {});
    Recording[] constraints = (Recording[]) _recordings.values().toArray(new Recording[] {});
    _configurations = createConfigsFromKnobs(inputKnobs, constraints);

    /*
    StochasticPolicyType type = _stochasticPolicyType;
    if (_continuousRounds != 0 && _continuousRoundTick == _continuousRounds) {
      type = _originalPolicyType;
    }
    */

    _miniMachine =
        new MiniMachine(
            this,
            _inputKnobs,
            _configurations,
            _samplingPolicy,
            _stochasticPolicyType,
            _rewardType);
    _miniMachine.setNumTaskSamples(_numTaskSamples);
    _miniMachine.setTaskDelay(_taskDelay);

    _miniMachine.restoreState(_states, _continuousKnob);

    _miniMachine.dumpConfigurations(_miniMachine._configurations, false);
    _miniMachine.dumpConfigurations(_miniMachine._configurations, true);

    LogUtil.writeLogger("==ROUND-START==\n");

    return true;
  }

  protected Configuration[] createConfigsFromKnobs(Knob[] knobs, Recording[] recordings) {
    int numConfigs = 1;
    for (int i = 0; i < knobs.length; i++) {
      numConfigs *= knobs[i].numPos();
    }

    int[] positions = new int[knobs.length];
    Configuration[] configurations = new Configuration[numConfigs];

    for (int i = 0; i < numConfigs; i++) {
      /*
      Knob[] inputKnobs =
        (Knob[]) knobs.values().toArray(new Knob[]{});
      Recording[] constraints =
        (Recording[]) recordings.values().toArray(new Recording[]{});
        */

      Configuration config =
          new Configuration(Arrays.copyOf(positions, positions.length), knobs, recordings);

      configurations[i] = config;

      for (int j = 0; j < knobs.length; j++) {
        positions[j]++;
        if (positions[j] < knobs[j].numPos()) {
          break;
        }
        positions[j] = 0;
      }
    }

    return configurations;
  }

  protected void initConfigurations() {
    _rewardKnob = new Recording(REWARD_KNOB, KnobValType.DOUBLE);
    _recordings.put(_rewardKnob.name(), _rewardKnob);
    _recordings.put(FEEDBACK_KNOB, new Recording(FEEDBACK_KNOB, KnobValType.DOUBLE));

    for (int i = 0; i < _userRecordings.length; i++) {
      _recordings.put(_userRecordings[i].name(), _userRecordings[i]);
    }

    Knob[] inputKnobs = (Knob[]) _inputKnobs.values().toArray(new Knob[] {});
    Recording[] constraints = (Recording[]) _recordings.values().toArray(new Recording[] {});

    _configurations = createConfigsFromKnobs(inputKnobs, constraints);

    for (int i = 0; i < _configurations.length; i++) {
      _configurations[i].updateRecording(FEEDBACK_KNOB, KnobValT.haveDouble(0.5));
    }

    LogUtil.writeLogger(String.format("==RUN %d==\n", _runCount));

    if (_performContinousLogic) {
      LogUtil.writeLogger("==ROUND-START==\n");
    }

    resetKnobRead();
  }

  Knob findHighestPriorityKnob() {
    int mp = -1;
    Knob highest = null;
    Knob[] inputKnobs = _inputKnobs.values().toArray(new Knob[] {});
    for (Knob ik : _inputKnobs.values()) {
      if (ik.getPriority() >= mp) {
        mp = ik.getPriority();
        highest = ik;
      }
    }
    return highest;
  }

  protected Configuration _lowest = null;
  protected Configuration _highest = null;

  protected boolean _runningOptimizer = false;
  protected double _lowOptimizeReward = 0.0;
  protected double _highOptimizeReward = 0.0;

  protected boolean _performSelfOptimization = true;
  Knob _taskInterval = null;

  protected int _selfOptimizeRounds = 0;
  protected int _currentOptimizeRound = 0;

  protected void performSelfOptimizerDivision() {
    if (!_usingFixedSelfOptimizeInterval) {
      populateSelfOptimizerKnob();
    }
    resetSelfOptimizer();
  }

  protected void resetSelfOptimizer() {
    _runningOptimizer = true;
    Recording[] constraints = (Recording[]) _recordings.values().toArray(new Recording[] {});
    Knob[] knobs = new Knob[] {_taskInterval};

    Map<String, Knob> mappedKnobs = new LinkedHashMap<>();
    for (int i = 0; i < knobs.length; i++) {
      mappedKnobs.put(knobs[i].name(), knobs[i]);
    }

    Configuration[] selfOptimizingConfigs = createConfigsFromKnobs(knobs, constraints);

    /*
    for (int i = 0; i < selfOptimizingConfigs.length; i++) {
      System.err.format("Config: %s\n", selfOptimizingConfigs[i]);
    }
    */

    _miniMachine =
        new MiniMachine(
            this,
            mappedKnobs,
            selfOptimizingConfigs,
            SamplingPolicy.SAMPLE_ALL,
            _stochasticPolicyType,
            RewardType.OPTIMIZER);
    _miniMachine.setNumTaskSamples(_numTaskSamples);
    _miniMachine.setTaskDelay(_taskDelay);

    _lowest.select();
  }

  protected void initializeSelfOptimizer() {
    if (_experiment == Experiment.OPTIMIZER) {
      _finalSelfOptimizeTicks =
          (Integer) AndroidUtil.getProperty("STOKE_SELF_OPTIMIZE_FINAL_TICKS");
    }
    Integer fixedInterval = (Integer) AndroidUtil.getProperty("STOKE_SELF_OPTIMIZE_FIXED_INTERVAL");
    if (fixedInterval != null) {
      System.out.format("STOKE: Using fixed interval %d\n", fixedInterval);
      _taskInterval = new DiscreteKnob("task-interval", KnobValT.haveIntegers(fixedInterval), 0);
      _usingFixedSelfOptimizeInterval = true;
    } else {
      _taskInterval =
          new DiscreteKnob("task-interval", KnobValT.haveIntegers(40000, 30000, 20000), 0);
    }

    _lowest = _configurations[0];
    _highest = _configurations[_configurations.length - 1];

    resetSelfOptimizer();
  }

  public void dumpIntervals() {
    LogUtil.writeLogger(
        String.format("Bandit Runs:%d Tasks-in-Run:%d\n", _repeatTotal, _repeatTaskReset));
    LogUtil.writeLogger(
        String.format(
            "Self-Optimizer Rounds:%d Tasks-in-Round:%d\n",
            _selfOptimizeRounds, _selfOptimizeTicks));
    LogUtil.writeLogger(
        String.format("Continuous Knob Rounds:Inf Tasks-in-Round:%d\n", _continuousSteps));

    /*
    System.err.format("Bandit Runs:%d Tasks-in-Run:%d\n", _repeatTotal, _repeatTaskReset);
    System.err.format("Self-Optimizer Rounds:%d Tasks-in-Round:%d\n", _selfOptimizeRounds, _selfOptimizeTicks);
    System.err.format("Continuous Knob Rounds:Inf Tasks-in-Round:%d\n", _continuousSteps);
    */
  }

  /* Programmer API: Programmer may forward YES or NO to the bandit
   * via userFeedback. */
  // public void userFeedback(Feedback feed) {
  //  updateWindow(feed);
  // }

  /* We maintain a window of last configurations that are all updated on a
   * userFeedback trigger. This is done due to the delay associated with a
   * user's perception of quality and the actual configuration(s) selected
   * during that time peroid. */
  // protected void slideWindow(int configId) {
  //  for (int i = QUALITY_WINDOW-1; i > 0; i--) {
  //    _configWindow[i] = _configWindow[i-1];
  //  }
  //  _configWindow[0] = configId;
  //  //System.err.format("STOKE: Window: ");
  //  for (int i = 0; i < QUALITY_WINDOW; i++) {
  //    System.err.format("%d ", _configWindow[i]);
  //  }
  //  System.err.format("\n");
  // }

  // protected void updateWindow(Feedback feed) {
  //  if (_inSampling) {
  //    return;
  //  }
  //  System.err.format("Calling with %s\n", feed);
  //  for (int i = 0; i < QUALITY_WINDOW; i++) {
  //    System.err.format("%d ", _configWindow[i]);
  //  }
  //  System.err.format("\n");
  //
  //  Map<Integer,Boolean> alreadyUpdated = new HashMap<>();
  //  for (int i = 0; i < QUALITY_WINDOW; i++) {
  //    if (_configWindow[i] == -1) continue;
  //
  //    if (alreadyUpdated.get(_configWindow[i]) == null) {
  //      Configuration c = _configurationsMap.get(_configWindow[i]);
  //      _latticePolicy.rateLattice(c, _latDir, feed);
  //      alreadyUpdated.put(_configWindow[i], true);
  //    }
  //
  //    _configWindow[i] = -1;
  //  }
  // }

  // protected double _startDrain = 0;
  // protected double _lastDrain = 0;

  // protected double getDrain() {
  //  return _startDrain - _lastDrain;
  // }

  // public void updateDrain(double drain) {
  //  _lastDrain = drain;
  // }

  /* The following methods expose the programmer API for stepping a task:
   * prestep and poststep trigger the start and stop of an energy recording, along
   * with machine adjustments. */

  /*
  public double scaledEnergy(Configuration config) {
    double totalConsumed =
        KnobValT.needDouble(config.getRecording(_rewardKnob.name()));

    double timeOn = config.getNumOn();
    double consumed = totalConsumed / timeOn;
    if (consumed > _rscale) {
      throw new RuntimeException("We should have properly scaled when reading energy");
    }

    return (consumed / _rscale);
  }
  */

  public double totalReward() {
    return _miniMachine._totalLeftReward;
  }

  private void resetTaskInternals() {
    _miniMachine.clearStep();
    if (_taskWatch.isRunning()) {
      _taskWatch.stop();
    }
    if (_reward.getWatch().isRunning()) {
      _reward.getWatch().stop();
    }
  }

  /* Public API discussed in paper */
  private Thread _aeneasThread = null;
  private boolean _running = false;

  public void start() {
    if (!_running) {
      _running = true;
      preStep();
      System.err.format("STOKE: We have kicked off the first preStep!\n");
      _aeneasThread = new Thread(this);
      _aeneasThread.start();
    }
  }

  public void stop() {
    if (_running) {
      _aeneasThread.interrupt();
      resetKnobRead();
      resetTaskInternals();
      _running = false;
      _didInteract = false;
      _didRun = false;
    }
  }

  public void reset() {
    finishRun();
    _runCount++;
    resetConfigurations();
  }

  public int step() {
    return _banditStep;
  }

  /* public for model right now */
  public KnobVal internalRead(String name) {
    if (name.equals("task-interval")) {
      return _taskInterval.getSetting();
    }
    return _inputKnobs.get(name).getSetting();
  }

  public KnobVal read(String name) {
    return internalRead(name);
  }

  /*
  public KnobVal read(String name) {
    KnobVal n = internalRead(name);
    if (n == null) {
      return n;
    }
    try {
      _lock.lock();

      if (!_knobRead.get(name)) {
        _knobReadCount--;
        _knobRead.put(name, true);
      }

      if (_knobReadCount == 0) {
        _knobsHaveBeenRead.signalAll();
      }

      while (_knobReadCount == 0) {
        _machineHasUpdated.await();
      }
    } catch (InterruptedException e) {
    } catch (Exception e) {
      System.err.format("!!!!!!! Got exception: %s\n", e);
      e.printStackTrace(System.err);
    } finally {
      _lock.unlock();
    }
    return n;
  }
  */

  ReentrantLock _lock = new ReentrantLock();

  Condition _knobsHaveBeenRead = _lock.newCondition();
  Condition _machineHasUpdated = _lock.newCondition();

  Condition _waitOnInteract = _lock.newCondition();
  Condition _waitOnRun = _lock.newCondition();

  boolean _didInteract = false;
  boolean _didRun = false;

  boolean _cleanUp = false;

  int _knobReadCount = 0;
  Map<String, Boolean> _knobRead = new HashMap<>();

  private void resetKnobRead() {
    _knobReadCount = 0;
    for (Map.Entry<String, Knob> e : _inputKnobs.entrySet()) {
      _knobReadCount++;
      _knobRead.put(e.getKey(), false);
    }
  }

  public void interact() {
    try {
      _lock.lock();
      _didInteract = true;
      _waitOnInteract.signal();
      while (!_didRun) {
        _waitOnRun.await();
      }
      _didRun = false;
    } catch (InterruptedException e) {
      System.err.format("STOKE: Thead %d interupted\n", Thread.currentThread().getId());
    } catch (Exception e) {
      System.err.format("STOKE: Got exception: %s\n", e);
      System.exit(1);
    } finally {
      _lock.unlock();
    }
  }

  public void run() {
    System.err.format("STOKE: Thead %d started\n", Thread.currentThread().getId());
    boolean keepRunning = true;
    while (keepRunning) {
      try {
        _lock.lock();
        while (!_didInteract) {
          _waitOnInteract.await();
        }

        // System.err.format("STOKE: Starting postStep!\n");
        postStep();
        // System.err.format("STOKE: Finished postStep!\n");
        resetKnobRead();

        // System.err.format("STOKE: Starting preStep!\n");
        preStep();
        // System.err.format("STOKE: Finished preStep!\n");

        _didInteract = false;
        _didRun = true;
        _waitOnRun.signal();
      } catch (InterruptedException e) {
        keepRunning = false;
        System.err.format("STOKE: Thead %d interupted\n", Thread.currentThread().getId());
      } catch (Exception e) {
        System.err.format("STOKE: Got exception: %s\n", e);
        System.exit(1);
      } finally {
        _lock.unlock();
      }
      // System.err.format("STOKE: Finished loop\n");
    }
    System.err.format("STOKE: Thead %d exited\n", Thread.currentThread().getId());
  }

  /*
  public void run() {
    System.err.format("STOKE: Thead %d started\n", Thread.currentThread().getId());
    boolean keepRunning = true;
    while (keepRunning) {
      try {
        _lock.lock();
        while (_knobReadCount > 0) {
          _knobsHaveBeenRead.await();
        }
        //System.err.format("STOKE: Starting postStep!\n");
        postStep();
        //System.err.format("STOKE: Finished postStep!\n");
        resetKnobRead();

        //System.err.format("STOKE: Starting preStep!\n");
        preStep();
        //System.err.format("STOKE: Finished preStep!\n");

        _machineHasUpdated.signalAll();
      } catch (InterruptedException e) {
        keepRunning = false;
        System.err.format("STOKE: Thead %d interupted\n", Thread.currentThread().getId());
      } catch (Exception e) {
        System.err.format("STOKE: Got exception: %s\n", e);
      } finally {
        _lock.unlock();
      }
      //System.err.format("STOKE: Finished loop\n");
    }
    System.err.format("STOKE: Thead %d exited\n", Thread.currentThread().getId());
  }
  */

  /* Pre-Exhisting Programmer API */
  public void preStep() {
    if (_banditStep < _taskSkip) {
      return;
    } else if (_banditStep == _taskSkip) {
      _reward.getWatch().start();
    }

    _taskWatch.start();
    if (!_totalWatch.isRunning()) {
      _totalWatch.start();
    }

    // _startReading = _meter.readMeter();

    if (_runningOptimizer) {
      if ((_banditStep - _taskSkip) % 2 == 0) {
        _lowest.select();
        _miniMachine.preStep(false);
      } else if ((_banditStep - _taskSkip) % 2 == 1) {
        _highest.select();
      }
      return;
    }

    // _miniMachine.preStep(_inFinalSelfOptimizeTicks || _continuousFreeze);
    _miniMachine.preStep(_inFinalSelfOptimizeTicks);

    // System.out.format("STOKE: Selected configuration %d\n", _miniMachine._selected.getId());

    if (_miniMachine.inSampling() && _performContinousLogic) {
      _hackFreeze = !_hackFreeze;
    }

    if (!_miniMachine.inSampling() && _performContinousLogic) {
      _hackFreeze = false;
      _continuousFreeze = !_continuousFreeze;
    }
  }

  /* Programmer API */
  public void postStep() {
    if (_banditStep < _taskSkip) {
      _banditStep++;
      return;
    }

    _taskWatch.stop();
    long ms = _taskWatch.elapsed(TimeUnit.MILLISECONDS);
    /*
    if (skipTime) {
      ms = 1000;
    }
    */
    double task_seconds = ms / 1000.0;
    _taskWatch.reset();

    // _diffReading = _meter.diffMeter(_startReading);

    _banditStep++;

    /*
    double reward = 0;
    MeterVector vector = null;
    switch(_miniMachine._rewardType) {
      case JOULES:
        vector = _meter.asJoules(_diffReading);
        reward = vector.get("total");
        _miniMachine._rewardType.adjustLeft(reward);
        break;
      case WATTS:
        vector = _meter.asWatts(_diffReading);
        reward = vector.get("total");
        _miniMachine._rewardType.adjustLeft(reward);
        break;
      case BATTERY:
        reward = (externalReward * task_seconds);
        _miniMachine._rewardType.adjustLeft(reward);
        break;
      case CONSTRAINED:
        reward = (externalReward * task_seconds);
        _miniMachine._rewardType.adjustLeft(reward);
        break;
      case CONSTRAINED2:
        reward = (externalReward * task_seconds);
        _miniMachine._rewardType.adjustLeft(reward);
        break;
      case OPTIMIZER:
        //vector = _meter.asJoules(_diffReading);
        //reward = vector.get("total");
        System.err.format("STOKE: External Reward: %.2f\n", externalReward);
        reward = externalReward;
        //reward = (externalReward * task_seconds);
        break;
    }
    */

    /*
    if (_runningOptimizer) {
      if ((_banditStep - _taskSkip) % 2 == 1) {
        _lowOptimizeReward = reward;
      } else if ((_banditStep - _taskSkip) % 2 == 0) {
        _highOptimizeReward = reward;

        double leftReward = Math.abs(_highOptimizeReward - _lowOptimizeReward);
        double rightReward = KnobValT.needInteger(_taskInterval.getSetting());

        _miniMachine._rewardType.adjustLeft(leftReward);
        _miniMachine._rewardType.adjustRight(rightReward);

        System.err.format("STOKE: Unscaled LeftReward: %.2f -- Right Reward: %.2f \n", leftReward, rightReward);
        System.err.format("STOKE: Scaled LeftReward: %.2f -- Right Reward: %.2f \n", leftReward / _miniMachine._rewardType.leftUpperBound(), (1.0 - (rightReward / _miniMachine._rewardType.rightUpperBound())));

        _miniMachine.postStep(leftReward, rightReward, ms);
      }

      System.err.format("STOKE: postConfigStep:%d _selfOptimizeTicks:%d _currentOptimizeRound:%d _selfOptimizeRounds:%d\n", _miniMachine.postConfigStep(), _selfOptimizeTicks, _currentOptimizeRound,_selfOptimizeRounds);
      if (_inFinalSelfOptimizeTicks) {
        if (_miniMachine.postConfigStep() > _selfOptimizeTicks + _finalSelfOptimizeTicks) {
          done();
        }
      }

      if (!_inFinalSelfOptimizeTicks && _miniMachine.postConfigStep() > _selfOptimizeTicks) {
        _currentOptimizeRound++;
        if (_currentOptimizeRound < _selfOptimizeRounds) {
          System.err.format("STOKE: Performing a round of self optimizer division\n");
          performSelfOptimizerDivision();
          LogUtil.writeLogger("Performing self optimizer division");
          _miniMachine.dumpConfigurations(_miniMachine._configurations, true);
        } else {
          _miniMachine.finalize();
          System.err.format("STOKE: Selected %d as optimal task-interval\n", KnobValT.needInteger(_taskInterval.getSetting()));
          LogUtil.writeLogger(String.format("Selected %d as optimal task-interval\n", KnobValT.needInteger(_taskInterval.getSetting())));
          if (_experiment == Experiment.OPTIMIZER) {
            _inFinalSelfOptimizeTicks = true;
          } else {
            _runningOptimizer = false;
            _miniMachine = new MiniMachine(this, _inputKnobs, _configurations, _samplingPolicy,
                _latticePolicyType, _stochasticPolicyType, _rewardType);
            _miniMachine.setNumTaskSamples(_numTaskSamples);
            _miniMachine.setTaskDelay(_taskDelay);
            System.err.format("STOKE: Starting new bandit!\n");
          }
        }
      }

      return;
    }
    */

    double rawReward = _reward.valuate();
    double rawJoules = _reward.cached();

    double r = _reward.SLA() - rawReward;
    _miniMachine.postStep(r, 0.0, ms, rawReward, rawJoules);

    // Offline collect
    if (!_miniMachine.inSampling() && _experiment == Experiment.OFFLINE_COLLECT) {
      done();
    }

    // Continuous knob logic
    if (_performContinousLogic) {
      if (_banditStep > FALLBACK_STEPS) {
        LogUtil.writeLogger("Hit safety!\n");
        finishRun();
        done();
      }

      _continuousStepTick++;

      // Continuous Interval Logic
      if (_continuousStepTick >= MIN_STEPS) {
        double minQ = Double.MAX_VALUE;
        int minRewardIndex = -1;

        int maxConvergeIndex = -1;
        double maxConverge = 0;
        double[] convergenceRates = new double[_miniMachine._configurations.length];
        for (int i = 0; i < _miniMachine._configurations.length; i++) {
          Configuration c = _miniMachine._configurations[i];
          double c_reward = Math.abs(_miniMachine.qvalue(c));

          if (Double.compare(c_reward, minQ) < 0) {
            minQ = c_reward;
            minRewardIndex = i;
          }

          int stateNumOns =
              _miniMachine._numOns.get(c.getId()) - _miniMachine._restoredNumOns.get(c.getId());
          convergenceRates[i] = (double) stateNumOns / (double) _miniMachine.step();
          if (convergenceRates[i] > maxConverge) {
            maxConverge = convergenceRates[i];
            maxConvergeIndex = i;
          }
          System.err.format("STOKE: %d convergence rate %.2f\n", c.getId(), convergenceRates[i]);
          LogUtil.writeLogger(
              String.format("STOKE: %d convergence rate %.2f\n", c.getId(), convergenceRates[i]));
        }

        System.err.format("STOKE: minQ:%.2f minInd:%d\n", minQ, minRewardIndex);
        LogUtil.writeLogger(String.format("STOKE: minQ:%.2f minInd:%d\n", minQ, minRewardIndex));

        int minId = _miniMachine._configurations[minRewardIndex].getId();
        LogUtil.writeLogger(String.format("STOKE: minId:%d\n", minId));

        int pick =
            KnobValT.needInteger(
                _miniMachine._configurations[minRewardIndex].getSettingForKnob(_continuousKnob));

        int slowpick =
            KnobValT.needInteger(
                _miniMachine._configurations[0].getSettingForKnob(_continuousKnob));
        int fastpick =
            KnobValT.needInteger(
                _miniMachine._configurations[_miniMachine._configurations.length - 1]
                    .getSettingForKnob(_continuousKnob));

        boolean shouldConverge = Double.compare(maxConverge, CONVERGE_THRESHOLD) > 0;
        boolean tickUp = _continuousStepTick >= MAX_STEPS;

        if (shouldConverge || tickUp) {
          LogUtil.writeLogger(
              String.format("STOKE: shouldConverge:%b tickUp:%b\n", shouldConverge, tickUp));

          if (_reward.equate(minQ, 0)) {
            System.err.format("STOKE: At threshold!\n");
            LogUtil.writeLogger(String.format("STOKE: At threshold %.2f\n", minQ));
            return;
          }

          boolean emptyRound = _lastRoundQStack.size() == 0;
          int lastPick = 0;
          double lastQ = 0.0;
          if (!emptyRound) {
            lastQ = _lastRoundQStack.get(_lastRoundQStack.size() - 1);
            lastPick = _lastRoundPickStack.get(_lastRoundPickStack.size() - 1);
            System.err.format("STOKE: Last/Min -- %.4f -- %.4f\n", lastQ, minQ);
            LogUtil.writeLogger(String.format("STOKE: Last/Min -- %.4f -- %.4f\n", lastQ, minQ));
          }

          if (emptyRound) {
            System.err.format("STOKE: Tightening intervals (empty round)\n");
            LogUtil.writeLogger("STOKE: Tightening intervals (empty round)\n");
            performContinuousKnobDivision(true, null, minId);
            return;
          }

          if (lastPick == pick) {
            if (slowpick - fastpick < 50) {
              System.err.format(
                  "STOKE: Too close, staying! slow %d = fast %d)\n", slowpick, fastpick);
              LogUtil.writeLogger(
                  String.format(
                      "STOKE: Too close, staying! slow %d = fast %d)\n", slowpick, fastpick));
            }

            System.err.format(
                "STOKE: Tightening intervals (last pick %d = pick %d)\n", lastPick, pick);
            LogUtil.writeLogger(
                String.format(
                    "STOKE: Tightening intervals (last pick %d = pick %d)\n", lastPick, pick));
            performContinuousKnobDivision(true, null, minId);
            return;
          }

          if (Double.compare(lastQ * minQ, 0) < 0) {
            if (slowpick - fastpick < 50) {
              System.err.format(
                  "STOKE: Too close, staying! slow %d = fast %d)\n", slowpick, fastpick);
              LogUtil.writeLogger(
                  String.format(
                      "STOKE: Too close, staying! slow %d = fast %d)\n", slowpick, fastpick));
              return;
            }

            int slowInterval = (lastPick > pick) ? lastPick : pick;
            int fastInterval = (lastPick < pick) ? lastPick : pick;

            System.err.format("STOKE: Crossing boundary Last:%d This:%d\n", lastPick, pick);
            LogUtil.writeLogger(
                String.format("STOKE: Crossing boundary Last:%d This:%d\n", lastPick, pick));

            performContinuousKnobDivision(
                true, AeneasMachine.seedMinMax(slowInterval, fastInterval, _slots), minId);
            return;
          }

          double diff = Math.abs(lastQ) - Math.abs(minQ);
          System.err.format("STOKE: Round diff: %.4f\n", diff);
          LogUtil.writeLogger(String.format("STOKE: Round diff: %.4f\n", diff));

          if (_reward.equate(diff, 0) && Double.compare(lastQ, minQ) > 0) {
            if (slowpick - fastpick < 50) {
              System.err.format(
                  "STOKE: Too close, staying! slow %d = fast %d)\n", slowpick, fastpick);
              LogUtil.writeLogger(
                  String.format(
                      "STOKE: Too close, staying! slow %d = fast %d)\n", slowpick, fastpick));
              return;
            }

            System.err.format("STOKE: Tightening intervals\n");
            LogUtil.writeLogger("STOKE: Tightening intervals\n");
            performContinuousKnobDivision(true, null, minId);
            return;

          } else {
            System.err.format("STOKE: Widening intervals\n");
            LogUtil.writeLogger("STOKE: Widening intervals\n");
            performContinuousKnobDivision(false, null, -1);
            return;
          }
        }
      }
      return;
    }

    // Repeat logic
    if (!_miniMachine.inSampling()) {
      if (_repeatTotal != 0) {
        if (_repeatTick < _repeatTaskReset) {
          _repeatTick++;
        } else {
          finishRun();
          _runCount++;
          if (_runCount < _repeatTotal) {
            resetConfigurations();
          } else {
            done();
          }
        }
      }
    }
  }

  public void finishRun() {
    if (_totalWatch.isRunning()) {
      _totalWatch.stop();
      LogUtil.writeLogger(
          String.format("ERun: Time:%d\n", _totalWatch.elapsed(TimeUnit.MILLISECONDS)));
      LogUtil.writeLogger(String.format("==END-RUN %d==\n", _runCount));
      LogUtil.writeLogger(String.format("==ROUND-END==\n", _runCount));
    }
    if (_reward.getWatch().isRunning()) {
      _reward.getWatch().stop();
    }
    if (_taskWatch.isRunning()) {
      _taskWatch.stop();
    }

    _miniMachine.dumpConfigurations(_miniMachine._configurations, false);
    _miniMachine.dumpConfigurations(_miniMachine._configurations, true);
  }

  /* Programmer API */
  public void done() {
    LogUtil.closeLogger();
    System.out.format("\n");
    System.exit(0);
  }

  // Stuff relating to binary search bandit. Not sure where to put it yet
  public void pauseSelection() {
    _isPaused = true;
  }

  public void resumeSelection() {
    _isPaused = false;
  }

  public boolean isPaused() {
    return _isPaused;
  }

  public int currentConfiguration() {
    return _miniMachine.currentConfiguration();
  }

  public void setTaskSkip(int taskSkip) {
    _taskSkip = taskSkip;
  }

  public int getTaskSkip() {
    return _taskSkip;
  }

  public void setRepeatTaskReset(int repeatTaskReset) {
    _repeatTaskReset = repeatTaskReset;
  }

  public void setRepeatTotal(int repeatTotal) {
    _repeatTotal = repeatTotal;
  }

  public void setSelfOptimizeTicks(int ticks) {
    _selfOptimizeTicks = ticks;
  }

  public void setSelfOptimizeRounds(int ticks) {
    _selfOptimizeRounds = ticks;
  }

  /* Mini Bandit Forwards */
  public void setNumTaskSamples(int samples) {
    _numTaskSamples = samples;
    _miniMachine.setNumTaskSamples(samples);
  }

  public void setTaskDelay(int taskDelay) {
    _taskDelay = taskDelay;
    _miniMachine.setTaskDelay(taskDelay);
  }

  public void updateRecording(String name, KnobVal val) {
    if (name.equals("location_accuracy")) {
      System.out.format(
          "STOKE: Configuration %d getting %s\n", _miniMachine._selected.getId(), val);
    }
    Recording cons = _recordings.get(name);
    _miniMachine._selected.updateRecording(name, val);
  }

  /* Continuous Knob Logic:
   *  Right now this is a bit of a jumbled mess, and the logic itself
   *  should be attached to a Knob as opposed to the bandit, but
   *  consolidating it here for now.
   */

  private int _continuousSteps = 0;
  private int _continuousRounds = 0;
  private String _continuousKnobName = null;
  private Constraint _constraint = null;

  private int _continuousStepTick = 0;
  private int _continuousRoundTick = 0;
  protected boolean _performContinousLogic = false;
  protected boolean _continuousFreeze = false;
  protected boolean _hackFreeze = false;

  protected int _numSplits = 5;

  // This is done specifically for the self optimizing mini bandit
  private Configuration[] orderByReward(final boolean increasing) {
    System.out.println("STOKE: orderByReward  " + increasing);
    Configuration[] ordered =
        Arrays.copyOf(_miniMachine._configurations, _miniMachine._configurations.length);
    Arrays.sort(
        ordered,
        new Comparator<Configuration>() {
          public int compare(Configuration c1, Configuration c2) {
            double e1 = _miniMachine.qvalue(c1);
            double e2 = _miniMachine.qvalue(c2);
            if (increasing) {
              return Double.compare(e1, e2);
            } else {
              return Double.compare(e2, e1);
            }
          }
        });
    return ordered;
  }

  public Configuration[] orderByRecording(final Recording recording, final boolean increasing) {
    Configuration[] ordered = Arrays.copyOf(_configurations, _configurations.length);
    Arrays.sort(
        ordered,
        new Comparator<Configuration>() {
          public int compare(Configuration c1, Configuration c2) {
            double e1 = KnobValT.needDouble(c1.getRecordingDivd(recording.name()));
            double e2 = KnobValT.needDouble(c2.getRecordingDivd(recording.name()));
            if (increasing) {
              return Double.compare(e1, e2);
            } else {
              return Double.compare(e2, e1);
            }
          }
        });
    return ordered;
  }

  public Configuration[] filterForConstraint(Constraint constraint) {
    Configuration[] ordered = orderByReward(true);
    List<Configuration> filtered = new ArrayList<>();
    for (int i = 0; i < ordered.length; i++) {
      KnobVal val = ordered[i].getRecordingDivd(constraint.getRecording().name());
      if (val.compareTo(constraint.getRequired()) > 0) {
        filtered.add(ordered[i]);
      }
    }
    return filtered.toArray(new Configuration[] {});
  }

  static class ConfigurationPair {
    Configuration _lower = null;
    Configuration _higher = null;
    boolean _lowerFake = false;
    boolean _higherFake = false;

    public ConfigurationPair() {}

    public ConfigurationPair(Configuration lower, Configuration higher) {
      _lower = lower;
      _higher = higher;
    }
  }

  private List<ConfigurationPair> isolateSelfOptimizeCases() {
    double maxReward = 0.0;
    int maxi = -1;
    for (int i = 0; i < _miniMachine._configurations.length; i++) {
      Configuration c = _miniMachine._configurations[i];
      double e = _miniMachine.qvalue(c);
      if (e > maxReward) {
        maxReward = e;
        maxi = i;
      }
    }
    System.out.println(
        "STOKE: Select maxi : " + maxi + " length " + _miniMachine._configurations.length);

    List<ConfigurationPair> edges = new ArrayList<>();
    if (maxi == 0) {
      edges.add(
          new ConfigurationPair(_miniMachine._configurations[0], _miniMachine._configurations[1]));
    } else if (maxi == _miniMachine._configurations.length - 1) {
      edges.add(
          new ConfigurationPair(
              _miniMachine._configurations[_miniMachine._configurations.length - 2],
              _miniMachine._configurations[_miniMachine._configurations.length - 1]));
    } else {
      edges.add(
          new ConfigurationPair(
              _miniMachine._configurations[maxi - 1], _miniMachine._configurations[maxi]));
      edges.add(
          new ConfigurationPair(
              _miniMachine._configurations[maxi], _miniMachine._configurations[maxi + 1]));
    }

    return edges;
    /*
    Configuration[] ordered = orderByReward();

    System.err.format("STOKE: Ordering\n");
    _miniMachine.dumpConfigurations(ordered, false);
    System.err.format("STOKE:\n");

    ConfigurationPair edge = new ConfigurationPair(null, null);
    edge._lower = ordered[ordered.length-2];
    edge._higher = ordered[ordered.length-1];
    */
    /*
    double range = 0;

    for (int i = 0; i < ordered.length-1; i++) {
      double lv = _miniMachine.qvalue(ordered[i]);
      double rv = _miniMachine.qvalue(ordered[i+1]);
      if (rv - lv > range) {
        range = rv - lv;
        edge._lower = ordered[i];
        edge._higher = ordered[i+1];
      }
    }

    List<ConfigurationPair> edges = new ArrayList<>();
    edges.add(edge);

    return edges;
    */
  }

  /*
  public List<ConfigurationPair> isolateEdgeCases(Constraint constraint) {
    int maxi = -1;
    double maxr = 0;
    for (int i = 0; i < _configurations.length; i++) {
      Configuration c = _configurations[i];
      double scaled = _miniMachine.qvalue(c);
      if (scaled > maxr) {
        maxr = scaled;
        maxi = i;
      }
    }

    Configuration low = _configurations[0];
    Configuration high = _configurations[_configurations.length-1];

    Knob gpsInterval = _inputKnobs.get("gps-interval");

    int originalRange = KnobValT.needInteger(high.getSettingForKnob(gpsInterval)) - KnobVal.needInteger(low.getSettingForKnob(gpsInterval));
    int newRange = originalRange / 2;
    System.out.format("STOKE: Original Range:%d New Range: %d", originalRange, newRange);

    int slots = 3;
    int partition = newRange / slots;


    Configuration[] ordered = null;
  }
  */

  /*
  public List<ConfigurationPair> isolateEdgeCases(Constraint constraint) {
    Configuration[] ordered = null;
    if (constraint.getRecording().name().equals("joule")) {
      ordered = orderByReward(!constraint.isMin());
    } else {
      ordered = orderByRecording(constraint.getRecording(), constraint.isMin());
    }

    System.out.format("STOKE: Dumping orded configurations\n");
    _miniMachine.dumpConfigurations(ordered, false);
    _miniMachine.dumpConfigurations(ordered, true);

    boolean allMeetRequirement = true;
    boolean noneMeetRequirement = true;

    List<ConfigurationPair> edges = new ArrayList<>();
    for (int i = 0; i < ordered.length-1; i++) {
      KnobVal lv = null;
      KnobVal rv = null;
      if (constraint.getRecording().name().equals("joule")) {
        lv = KnobValT.haveDouble(_miniMachine._leftRewards.get(ordered[i].getId()) / _miniMachine._numOns.get(ordered[i].getId()));
        rv = KnobValT.haveDouble(_miniMachine._leftRewards.get(ordered[i+1].getId()) / _miniMachine._numOns.get(ordered[i+1].getId()));
      } else {
        lv = ordered[i].getRecordingDivd(constraint.getRecording().name());
        rv = ordered[i+1].getRecordingDivd(constraint.getRecording().name());
      }

      if (constraint.isMin()) {
        System.out.format("STOKE: In min!\n");

        if (lv.compareTo(constraint.getRequired()) < 0 &&
            rv.compareTo(constraint.getRequired()) > 0) {
          edges.add(new ConfigurationPair(ordered[i], ordered[i+1]));
        }

        System.out.format("STOKE: Checking %s with %s\n", lv.toString(), constraint.getRequired().toString());

        if (lv.compareTo(constraint.getRequired()) > 0) {
          noneMeetRequirement = false;
        }
        if (lv.compareTo(constraint.getRequired()) < 0) {
          allMeetRequirement = false;
        }
      } else {
        if (lv.compareTo(constraint.getRequired()) > 0 &&
            rv.compareTo(constraint.getRequired()) < 0) {
          edges.add(new ConfigurationPair(ordered[i], ordered[i+1]));
        }

        if (lv.compareTo(constraint.getRequired()) < 0) {
          noneMeetRequirement = false;
        }
        if (lv.compareTo(constraint.getRequired()) > 0) {
          allMeetRequirement = false;
        }
      }
    }
    KnobVal last = null;
    if (constraint.getRecording().name().equals("joule")) {
      Configuration c = ordered[ordered.length-1];
      last = KnobValT.haveDouble(_miniMachine._leftRewards.get(c.getId()) / _miniMachine._numOns.get(c.getId()));
    } else {
      last = ordered[ordered.length-1].getRecordingDivd(constraint.getRecording().name());
    }
    if (constraint.isMin()) {
      if (last.compareTo(constraint.getRequired()) > 0) {
        noneMeetRequirement = false;
      }
      if (last.compareTo(constraint.getRequired()) < 0) {
        allMeetRequirement = false;
      }
    } else {
      if (last.compareTo(constraint.getRequired()) < 0) {
        noneMeetRequirement = false;
      }
      if (last.compareTo(constraint.getRequired()) > 0) {
        allMeetRequirement = false;
      }
    }

    if (allMeetRequirement) {
      System.out.format("STOKE: All configurations meet requirement\n");
      ConfigurationPair fake = new ConfigurationPair(null, ordered[0]);
      fake._lowerFake = true;
      edges.add(fake);
    }
    if (noneMeetRequirement) {
      System.out.format("STOKE: No configurations meet requirement\n");
      ConfigurationPair fake = new ConfigurationPair(ordered[ordered.length-1], null);
      fake._higherFake = true;
      edges.add(fake);
    }

    return edges;
  }
  */

  public static final double JOULE_THRESHOLD = 2.0;
  // public static final double STAY_THRESHOLD = 2.0;
  public static final double STAY_THRESHOLD = 2.0;
  public static final double NOT_MUCH_DIFF_THRESHOLD = 2.0;

  public static final double CONVERGE_THRESHOLD = 0.60;
  public static final int SLOTS = 5;

  public int _slots = SLOTS;

  public static KnobVal[] seedMinMax(int slow, int fast, int slots) {
    int range = slow - fast;
    int parts = range / (slots - 1);
    System.err.format("STOKE: Parts:%d \n", parts);

    KnobVal[] settings = new KnobVal[slots];
    settings[0] = KnobValT.haveInteger(slow);
    settings[settings.length - 1] = KnobValT.haveInteger(fast);

    int p = slots - 2;
    for (int i = 0; i < p; i++) {
      settings[i + 1] = KnobValT.haveInteger(slow - ((i + 1) * parts));
    }

    return settings;
  }

  public static List<Integer> seed(int slow, int middle, int fast, int range, int slots) {
    int partition = range / (slots - 1);

    System.err.format("STOKE: Range:%d Partition:%d Middle:%d\n", range, partition, middle);
    String fmt =
        String.format("STOKE: Range:%d Partition:%d Middle:%d\n", range, partition, middle);
    System.err.println(fmt);
    LogUtil.writeLogger(fmt);

    List<Integer> seeds = new ArrayList<>();

    int distributed = 1;

    boolean hitSlowBoundary = false;
    for (int i = 0; i < slots / 2; i++) {
      int off = (slots / 2) - i;
      if (middle + (partition * off) <= slow) {
        seeds.add((middle + (partition * off)));
        distributed++;
      } else {
        hitSlowBoundary = true;
      }
    }
    if (hitSlowBoundary && middle != slow) {
      distributed++;
      seeds.add(slow);
    }

    seeds.add(middle);

    boolean hitFastBoundary = false;
    for (int i = 0; i < slots / 2; i++) {
      int off = i + 1;
      if (middle - (partition * off) >= fast) {
        seeds.add(middle - (partition * off));
        distributed++;
      } else {
        hitFastBoundary = true;
      }
    }
    if (hitFastBoundary && middle != fast) {
      distributed++;
      seeds.add(fast);
    }

    Collections.sort(
        seeds,
        new Comparator<Integer>() {
          @Override
          public int compare(Integer l, Integer r) {
            return Integer.compare(r, l);
          }
        });

    int remaining = slots - distributed;
    if (remaining > 0) {
      System.out.format("STOKE: Generated seeds before boundary adjust: %s\n", seeds);
      LogUtil.writeLogger(
          String.format("STOKE: Generated seeds before boundary adjust: %s\n", seeds));

      int smallerPartition = partition / (remaining + 1);
      int[] extraPartition = new int[remaining];
      for (int i = 0; i < remaining; i++) {
        if (hitSlowBoundary) {
          extraPartition[i] = seeds.get(0) - (smallerPartition * (i + 1));
        } else {
          extraPartition[i] = seeds.get(seeds.size() - 1) + (smallerPartition * (i + 1));
        }
      }
      for (int i = 0; i < remaining; i++) {
        seeds.add(extraPartition[i]);
      }

      Collections.sort(
          seeds,
          new Comparator<Integer>() {
            @Override
            public int compare(Integer l, Integer r) {
              return Integer.compare(r, l);
            }
          });
    }

    System.out.format("STOKE: Generated seeds: %s\n", seeds);
    LogUtil.writeLogger(String.format("STOKE: Generated seeds: %s\n", seeds));

    return seeds;
  }

  public boolean populateKnobFromEdges(boolean shouldTighten, int minId) {
    if (!shouldTighten) {
      SeedRange sr = _seedStack.get(_seedStack.size() - 1);

      /*
      List<Integer> seeds = AeneasMachine.seed(sr._slow,sr._mid,sr._fast,sr._slow - sr._fast);
      List<KnobVal> newSettings = new ArrayList<>();
      for (int i = 0; i < seeds.size(); i++) {
        newSettings.add(KnobValT.haveInteger(seeds.get(i)));
      }
      */
      _continuousKnob.changeSettings(sr._settings);
      return true;
    }

    Configuration slowc = _configurations[0];
    Configuration fastc = _configurations[_configurations.length - 1];

    int slow = KnobValT.needInteger(slowc.getSettingForKnob(_continuousKnob));
    int middle = KnobValT.needInteger(_configurations[minId].getSettingForKnob(_continuousKnob));
    int fast = KnobValT.needInteger(fastc.getSettingForKnob(_continuousKnob));

    int originalRange = slow - fast;
    int newRange = originalRange / 2;

    List<Integer> seeds =
        AeneasMachine.seed(
            _continuousKnob._highLimit, middle, _continuousKnob._lowLimit, newRange, _slots);

    _seedStack.add(
        new SeedRange(
            Arrays.copyOf(_continuousKnob.getSettings(), _continuousKnob.getSettings().length)));

    List<KnobVal> newSettings = new ArrayList<>();
    for (int i = 0; i < seeds.size(); i++) {
      newSettings.add(KnobValT.haveInteger(seeds.get(i)));
    }

    _continuousKnob.changeSettings(newSettings.toArray(new KnobVal[] {}));
    return true;
  }

  public boolean populateSelfOptimizerKnob() {
    List<ConfigurationPair> edges = isolateSelfOptimizeCases();
    return populateKnobFromEdges("task-interval", edges);
  }

  public boolean populateKnobFromEdges(String name, List<ConfigurationPair> edges) {
    if (edges.size() == 0) {
      return false;
    }
    Knob needsPopulating = null;
    if (name.equals("task-interval")) {
      needsPopulating = _taskInterval;
    } else {
      needsPopulating = _inputKnobs.get(name);
    }
    // Hacking
    Map<Integer, Boolean> isAdded = new HashMap<>();

    List<KnobVal> newSettings = new ArrayList<>();
    for (int i = 0; i < edges.size(); i++) {
      KnobVal lv;
      KnobVal rv;
      ConfigurationPair edge = edges.get(i);
      if (!edge._lowerFake) {
        lv = edge._lower.getSettingForKnob(needsPopulating);
      } else {
        lv =
            edge._higher
                .getSettingForKnob(needsPopulating)
                .add(edge._higher.getSettingForKnob(needsPopulating));
      }
      if (!edge._higherFake) {
        rv = edge._higher.getSettingForKnob(needsPopulating);
      } else {
        rv = edge._lower.getSettingForKnob(needsPopulating).dividedBy(2);
      }

      KnobVal inBetween = lv.add(rv).dividedBy(2);
      if (isAdded.get(KnobValT.needInteger(lv)) == null) {
        newSettings.add(lv);
        isAdded.put(KnobValT.needInteger(lv), true);
      }
      if (isAdded.get(KnobValT.needInteger(inBetween)) == null) {
        newSettings.add(inBetween);
        isAdded.put(KnobValT.needInteger(inBetween), true);
      }
      if (isAdded.get(KnobValT.needInteger(rv)) == null) {
        newSettings.add(rv);
        isAdded.put(KnobValT.needInteger(rv), true);
      }
    }

    System.out.format("STOKE: New Settings: ");
    for (int i = 0; i < newSettings.size(); i++) {
      System.out.format("%s ", newSettings.get(i));
    }
    System.out.format("\n");

    needsPopulating.changeSettings(newSettings.toArray(new KnobVal[] {}));
    return true;
  }

  // Perhaps the edge cases are the only cases, we want to explore configurations
  // that just barely meet specification (to save the most energy)

  public void divideForKnob(String name, Constraint constraint) {
    Configuration[] filtered = filterForConstraint(constraint);
    Knob knob = _inputKnobs.get(name);
    List<KnobVal> newSettings = new ArrayList<>();
    for (int i = 0; i < filtered.length - 1; i++) {
      KnobVal lv = filtered[i].getSettingForKnob(knob);
      KnobVal rv = filtered[i + 1].getSettingForKnob(knob);
      KnobVal inBetween = lv.add(rv).dividedBy(2);
      newSettings.add(lv);
      newSettings.add(inBetween);
    }
    newSettings.add(filtered[filtered.length - 1].getSettingForKnob(knob));
    // TODO: Missing a first and last edge case, will come back
    knob.changeSettings(newSettings.toArray(new KnobVal[] {}));
    // resetConfigurations();
  }

  public void divideForKnob() {
    divideForKnob(_continuousKnobName, _constraint);
  }

  public void setContinuousRounds(int continuousRounds) {
    _continuousRounds = continuousRounds;
  }

  public void setContinuousSteps(int continuousSteps) {
    _continuousSteps = continuousSteps;
  }

  public void setContinuousKnobName(String continuousKnobName) {
    _continuousKnobName = continuousKnobName;
  }

  public void setConstraint(Constraint constraint) {
    _constraint = constraint;
  }

  public boolean isFrozen() {
    return _continuousFreeze || _hackFreeze;
  }
}
