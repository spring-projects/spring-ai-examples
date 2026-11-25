#!/bin/bash

set -eu

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UPDATE_SCRIPT="$(cd "${SCRIPT_DIR}/.." && pwd)/update-spring-ai-version.sh"

fail() {
    echo "FAIL: $1" >&2
    exit 1
}

assert_exit_code() {
    expected="$1"
    shift

    set +e
    "$@" >/tmp/update-version-test.out 2>&1
    actual=$?
    set -e

    if [ "${actual}" -ne "${expected}" ]; then
        cat /tmp/update-version-test.out >&2
        fail "Expected exit code ${expected}, got ${actual}"
    fi
}

echo "Test: rejects missing version"
assert_exit_code 2 "${UPDATE_SCRIPT}"

echo "Test: rejects extra arguments"
assert_exit_code 2 "${UPDATE_SCRIPT}" 2.0.0 extra

echo "Test: rejects invalid version"
assert_exit_code 1 "${UPDATE_SCRIPT}" latest

echo "PASS: update version argument validation"