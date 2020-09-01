load("@rules_java//java:defs.bzl", "java_library")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_library(
  name = "attribution",
  visibility = ["//visibility:public"],
  srcs = ["EflectFootprint.java"],
  deps = [
    "//src/eflect/sampling/cpu",
    "//src/eflect/sampling/tasks",
    "@chappie//src/chappie/attribution",
    "@chappie//src/chappie/sampling/energy",
    "@jRAPL",
  ],
)

java_library(
  name = "module",
  visibility = ["//visibility:public"],
  srcs = glob(["*.java"], exclude = ["EflectFootprint.java"]),
  deps = [
    ":attribution",
    "//src/eflect/sampling/cpu",
    "//src/eflect/sampling/tasks",
    "@chappie//src/chappie/attribution",
    "@chappie//src/chappie/sampling",
    "@chappie//src/chappie/sampling/energy",
    "@chappie//src/chappie/utils",
    "@clerk//src/clerk",
    "@jlibc//src/jlibc/proc",
    "@jRAPL",
    ":dagger",
  ],
)
