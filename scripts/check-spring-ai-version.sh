#!/bin/bash

# Check Spring AI versions across all pom.xml files.
# Usage: ./scripts/check-spring-ai-version.sh

set -e

echo "Checking Spring AI versions in all pom.xml files..."
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${PROJECT_ROOT}"

POM_LIST_FILE="$(mktemp "${TMPDIR:-/tmp}/spring-ai-poms.XXXXXX")"
RESULTS_FILE="$(mktemp "${TMPDIR:-/tmp}/spring-ai-versions.XXXXXX")"
POM_VERSIONS_FILE="$(mktemp "${TMPDIR:-/tmp}/spring-ai-pom-versions.XXXXXX")"

cleanup() {
    rm -f \
        "${POM_LIST_FILE}" \
        "${RESULTS_FILE}" \
        "${POM_VERSIONS_FILE}"
}

trap cleanup EXIT HUP INT TERM

find . \
    -name "pom.xml" \
    -type f \
    -not -path "./.version-backups/*" \
    | sort > "${POM_LIST_FILE}"

TOTAL_WITH_VERSION=0
TOTAL_WITHOUT_VERSION=0

echo "Files with Spring AI version declarations:"
echo "-----------------------------------------"

while IFS= read -r pom_file; do
    perl -0777 -ne '
            my $xml = $_;

            # Ignore declarations inside XML comments.
            $xml =~ s/<!--.*?-->//sg;

            while (
                $xml =~
                /<spring-ai\.version>\s*([^<]+?)\s*<\/spring-ai\.version>/sg
            ) {
                print "$1\n";
            }

            while ($xml =~ /<dependency>\s*(.*?)\s*<\/dependency>/sg) {
                my $dependency = $1;

                next unless $dependency =~
                    /<groupId>\s*org\.springframework\.ai\s*<\/groupId>/s;

                next unless $dependency =~
                    /<artifactId>\s*spring-ai-bom\s*<\/artifactId>/s;

                if (
                    $dependency =~
                    /<version>\s*([^<]+?)\s*<\/version>/s
                ) {
                    my $version = $1;

                    # The concrete value is already obtained from the property.
                    next if $version eq q(${spring-ai.version});

                    print "$version\n";
                }
            }
        ' "${pom_file}" \
        | sed '/^[[:space:]]*$/d' \
        | sort -u > "${POM_VERSIONS_FILE}"

    if [ -s "${POM_VERSIONS_FILE}" ]; then
        TOTAL_WITH_VERSION=$((TOTAL_WITH_VERSION + 1))
        relative_path="${pom_file#./}"

        while IFS= read -r version; do
            printf "  %s - %s\n" "${version}" "${relative_path}"
            printf "%s\t%s\n" \
                "${version}" \
                "${relative_path}" >> "${RESULTS_FILE}"
        done < "${POM_VERSIONS_FILE}"
    else
        TOTAL_WITHOUT_VERSION=$((TOTAL_WITHOUT_VERSION + 1))
    fi
done < "${POM_LIST_FILE}"

echo ""
echo "Version Summary:"
echo "----------------"

if [ -s "${RESULTS_FILE}" ]; then
    cut -f1 "${RESULTS_FILE}" \
        | sort \
        | uniq -c \
        | while read -r count version; do
            echo "  ${version}: ${count} files"
        done
else
    echo "  No Spring AI versions detected"
fi

echo ""
echo "Statistics:"
echo "-----------"
echo "  Total pom.xml files: $((TOTAL_WITH_VERSION + TOTAL_WITHOUT_VERSION))"
echo "  Files with Spring AI version declarations: ${TOTAL_WITH_VERSION}"
echo "  Files without Spring AI version declarations: ${TOTAL_WITHOUT_VERSION}"

UNIQUE_VERSION_COUNT=0

if [ -s "${RESULTS_FILE}" ]; then
    UNIQUE_VERSION_COUNT="$(
        cut -f1 "${RESULTS_FILE}" \
            | sort -u \
            | wc -l \
            | tr -d '[:space:]'
    )"
fi

case "${UNIQUE_VERSION_COUNT}" in
    0)
        echo ""
        echo "Error: no Spring AI version declarations were detected." >&2
        exit 2
        ;;
    1)
        CONSISTENT_VERSION="$(
            cut -f1 "${RESULTS_FILE}" | sort -u
        )"

        echo ""
        echo "All detected declarations use Spring AI version ${CONSISTENT_VERSION}"
        ;;
    *)
        echo ""
        echo "Error: multiple Spring AI versions were detected." >&2
        echo \
            "Consider running: ./scripts/update-spring-ai-version.sh <version>" \
            >&2
        exit 1
        ;;
esac