load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository", "new_git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:maven_rules.bzl", "maven_jar")

RULES_JVM_EXTERNAL_TAG = "2.8"
RULES_JVM_EXTERNAL_SHA = "79c9850690d7614ecdb72d68394f994fef7534b292c4867ce5e7dec0aa7bdfad"
http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

DAGGER_TAG = "2.28.1"
DAGGER_SHA = "9e69ab2f9a47e0f74e71fe49098bea908c528aa02fa0c5995334447b310d0cdd"
http_archive(
    name = "dagger",
    strip_prefix = "dagger-dagger-%s" % DAGGER_TAG,
    sha256 = DAGGER_SHA,
    urls = ["https://github.com/google/dagger/archive/dagger-%s.zip" % DAGGER_TAG],
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")

maven_install(
    artifacts = DAGGER_ARTIFACTS,
    repositories = DAGGER_REPOSITORIES,
)

maven_jar(
    name = "net_java_dev_jna_jna",
    artifact = "net.java.dev.jna:jna:5.4.0",
    repository = "https://repo1.maven.org/maven2",
)

git_repository(
    name = "clerk",
    commit = "a126080ed391a00ff5714cd2f5c2a096b2dff3a2",
    shallow_since = "1598887079 -0400",
    remote = "https://github.com/pl-eflect/clerk.git",
)

git_repository(
    name = "chappie",
    commit = "ac5100310afe403b6201a5e9564bca4ec03babfe",
    shallow_since = "1598918318 -0400",
    remote = "https://github.com/pl-eflect/chappie.git",
)

git_repository(
    name = "jRAPL",
    commit = "be6f46bccc52f5439947de1aba9bdca52401e471",
    shallow_since = "1598918413 -0400",
    remote = "https://github.com/pl-eflect/jRAPL.git",
)

git_repository(
    name = "jlibc",
    commit = "93bf8471c474edec3661391ca2216ca630aeb809",
    remote = "https://github.com/pl-eflect/jlibc.git",
)

# new_local_repository(
#   name = "async_profiler",
#   path = "/home/timur/projects/async-profiler",
#   build_file_content = """
# load("@rules_java//java:defs.bzl", "java_library")
#
# cc_library(
#     name = "async-profiler-lib",
#     srcs = glob(["src/*.c"]),
#     hdrs = glob(["src/*.h"]),
#     deps = ["//tools:copy_jni_hdr_lib"],
#     alwayslink = True
# )
#
# java_library(
#     name = "async_profiler",
#     visibility = ["//visibility:public"],
#     srcs = glob(["src/java/one/profiler/*.java"])
# )
#
# java_binary(
#     name = "test",
#     visibility = ["//visibility:public"],
#     main_class = "one.profiler.Test",
#     resources = [":libasyncProfiler.so"],
#     runtime_deps = [
#       ":async_profiler",
#     ],
# )
# """
# )
