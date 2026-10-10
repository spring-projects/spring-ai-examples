#!/usr/bin/env bash

set -euo pipefail

usage() {
    echo "Usage: $0 <module-directory> [maven-arguments...]"
    echo "Build and test a Maven example module."
    echo "Example: $0 agentic-patterns/chain-workflow -DskipTests"
}

if [[ $# -lt 1 || "$1" == "-h" || "$1" == "--help" ]]; then
    usage
    exit $(( $# == 1 ? 0 : 1 ))
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
MODULE_PATH="$1"
shift

if [[ -z "${JAVA_HOME:-}" ]]; then
    if [[ -x /usr/libexec/java_home ]]; then
        if detected_java_home="$(/usr/libexec/java_home -v 25 2>/dev/null)"; then
            if detected_java_version="$("${detected_java_home}/bin/java" -version 2>&1 | sed -n '1s/.*version "\([0-9][0-9]*\).*/\1/p')" \
                && [[ -n "${detected_java_version}" && "${detected_java_version}" -ge 25 ]]; then
                JAVA_HOME="${detected_java_home}"
            fi
        fi
    fi

    if [[ -z "${JAVA_HOME:-}" ]]; then
        for candidate in \
            "${HOME}"/.jdk/jdk-25*/jdk-25*/Contents/Home \
            "${HOME}"/.jdk/jdk-25*/Contents/Home \
            "${HOME}"/.jdk/jdk-25*/jdk-25*/; do
            if [[ -x "${candidate}/bin/java" ]]; then
                JAVA_HOME="$(cd "${candidate}" && pwd)"
                break
            fi
        done
    fi
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
    export JAVA_HOME
    export PATH="${JAVA_HOME}/bin:${PATH}"
fi

if [[ "$MODULE_PATH" = /* ]]; then
    echo "Error: module directory must be relative to the repository root." >&2
    exit 1
fi

if ! MODULE_DIR="$(cd "${PROJECT_ROOT}/${MODULE_PATH}" 2>/dev/null && pwd)"; then
    echo "Error: module directory does not exist: ${MODULE_PATH}" >&2
    exit 1
fi

case "${MODULE_DIR}/" in
    "${PROJECT_ROOT}/"*) ;;
    *)
        echo "Error: module directory must be inside the repository." >&2
        exit 1
        ;;
esac

if [[ ! -f "${MODULE_DIR}/pom.xml" ]]; then
    echo "Error: no pom.xml found in module directory: ${MODULE_PATH}" >&2
    exit 1
fi

MVN="${MAVEN:-mvn}"
if ! command -v "${MVN}" >/dev/null 2>&1; then
    echo "Error: Maven executable not found: ${MVN}" >&2
    exit 1
fi

JAVA_BIN="java"
if [[ -n "${JAVA_HOME:-}" ]]; then
    JAVA_BIN="${JAVA_HOME}/bin/java"
fi
if ! command -v "${JAVA_BIN}" >/dev/null 2>&1; then
    echo "Error: Java executable not found: ${JAVA_BIN}" >&2
    exit 1
fi

JAVA_VERSION="$("${JAVA_BIN}" -version 2>&1 | sed -n '1s/.*version "\([0-9][0-9]*\).*/\1/p')"
if [[ -z "${JAVA_VERSION}" || "${JAVA_VERSION}" -lt 25 ]]; then
    echo "Error: Java 25 or newer is required; found ${JAVA_VERSION:-an unrecognized Java version}." >&2
    exit 1
fi

echo "Building ${MODULE_PATH}"
"${MVN}" -f "${MODULE_DIR}/pom.xml" clean package "$@"
