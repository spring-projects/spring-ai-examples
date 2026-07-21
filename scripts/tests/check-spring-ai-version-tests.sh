#!/bin/bash

set -eu

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECKER="$(cd "${SCRIPT_DIR}/.." && pwd)/check-spring-ai-version.sh"

TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/spring-ai-version-test.XXXXXX")"

cleanup() {
    rm -rf "${TEST_ROOT}"
}

trap cleanup EXIT HUP INT TERM

fail() {
    echo "FAIL: $1" >&2
    exit 1
}

assert_contains() {
    output="$1"
    expected="$2"

    echo "${output}" | grep -Fq "${expected}" ||
        fail "Expected output to contain: ${expected}"
}

assert_not_contains() {
    output="$1"
    unexpected="$2"

    if echo "${output}" | grep -Fq "${unexpected}"; then
        fail "Expected output not to contain: ${unexpected}"
    fi
}

echo "Test: ignores commented Spring AI version declarations"

mkdir -p "${TEST_ROOT}/scripts"
mkdir -p "${TEST_ROOT}/example"

cp "${CHECKER}" "${TEST_ROOT}/scripts/check-spring-ai-version.sh"
chmod +x "${TEST_ROOT}/scripts/check-spring-ai-version.sh"

cat > "${TEST_ROOT}/example/pom.xml" <<'EOF'
<project>
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>commented-version-test</artifactId>
    <version>1.0.0</version>

    <properties>
        <!-- <spring-ai.version>1.1.3-SNAPSHOT</spring-ai.version> -->
        <spring-ai.version>2.0.0-SNAPSHOT</spring-ai.version>
    </properties>
</project>
EOF

set +e
output="$("${TEST_ROOT}/scripts/check-spring-ai-version.sh" 2>&1)"
exit_code=$?
set -e

if [ "${exit_code}" -ne 0 ]; then
    echo "${output}" >&2
    fail "Expected exit code 0, but received ${exit_code}"
fi

assert_contains "${output}" "2.0.0-SNAPSHOT"
assert_not_contains "${output}" "1.1.3-SNAPSHOT"
assert_contains \
    "${output}" \
    "All detected declarations use Spring AI version 2.0.0-SNAPSHOT"

echo "PASS: commented Spring AI version declaration was ignored"