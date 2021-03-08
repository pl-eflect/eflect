package com.stoke;

import com.google.common.base.Stopwatch;

public abstract class Reward {
  private final Stopwatch taskWatch = Stopwatch.createUnstarted();

  public final Stopwatch getWatch() {
    return taskWatch;
  }

  private double _cachedJoules;

  public final double cached() {
    return _cachedJoules;
  }

  // Programmer API
  public abstract double valuate();

  public abstract double SLA();

  public abstract boolean equate(double r1, double r2);

  public abstract double perInteractionEnergy();
}
