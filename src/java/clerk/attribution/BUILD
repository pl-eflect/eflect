load("@rules_java//java:defs.bzl", "java_library")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_library(
  name = "attribution",
  visibility = ["//visibility:public"],
  srcs = ["ClerkEnergyAttribution.java"],
  deps = [
    "@chappie//src/java/chappie/attribution",
    "@chappie//src/java/chappie/sampling/energy",
    "@jRAPL",
    "//src/java/clerk/sampling/cpu",
    "//src/java/clerk/sampling/tasks",
  ],
)


java_library(
  name = "module",
  visibility = ["//visibility:public"],
  srcs = glob(["*.java"]),
  deps = [
    "@chappie//src/java/chappie/attribution",
    "@chappie//src/java/chappie/attribution:sampler",
    "@chappie//src/java/chappie/profiling:core",
    "@chappie//src/java/chappie/profiling:samples",
    "@chappie//src/java/chappie/profiling:sampling_rate",
    "@chappie//src/java/chappie/sampling/energy",
    "@chappie//src/java/chappie/util",
    "@jlibc//src/main/java/jlibc/proc",
    "@jRAPL",
    ":dagger",
    "//src/java/clerk/sampling/cpu",
    "//src/java/clerk/sampling/tasks",
  ],
)
