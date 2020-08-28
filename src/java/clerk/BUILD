load("@rules_java//java:defs.bzl", "java_library")
load("@dagger//:workspace_defs.bzl", "dagger_rules")
load("@build_bazel_rules_android//android:rules.bzl", "android_library")

dagger_rules()

java_library(
  name = "clerk",
  visibility = ["//visibility:public"],
  srcs = ["Clerk.java"],
  deps = [
    "@chappie//src/java/chappie/concurrent:module",
    "@chappie//src/java/chappie/naive:module",
    "@chappie//src/java/chappie/processing:alignment",
    "@chappie//src/java/chappie/processing:attribution",
    "@chappie//src/java/chappie/processing/trace",
    "@chappie//src/java/chappie/profiling:core",
    "@chappie//src/java/chappie/profiling:profiler",
    ":dagger",
    "//src/java/clerk/attribution:module",
  ],
)

java_library(
  name = "reward",
  visibility = ["//visibility:public"],
  srcs = ["ClerkReward.java"],
  deps = [
    "@aeneas",
    "@chappie//src/java/chappie/attribution",
    "@chappie//src/java/chappie/concurrent:module",
    "@chappie//src/java/chappie/processing:attribution",
    "@chappie//src/java/chappie/profiling:core",
    "@chappie//src/java/chappie/profiling:profiler",
    ":dagger",
    ":clerk",
    "//src/java/clerk/attribution",
  ],
)
