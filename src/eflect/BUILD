load("@rules_java//java:defs.bzl", "java_library")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_library(
  name = "profiler",
  visibility = ["//visibility:public"],
  srcs = ["EflectProfiler.java"],
  deps = [
    "//src/eflect/attribution",
    "//src/eflect/attribution:module",
    "@chappie//src/chappie/attribution:module",
    "@chappie//src/chappie/sampling",
    "@clerk//src/clerk:profiler",
    "@clerk//src/clerk/concurrent",
    ":dagger",
  ],
)
