#!/usr/bin/env bash

set -euo pipefail

usage() {
    cat <<EOF
Usage: $0 <module-directory> [--image name:tag] [--name container]
       [--port host:container|none] [--env-file path]

Build a Maven example, create a Java 25 Docker image, and run it in the background.
Defaults: image spring-ai-<module>:latest, container spring-ai-<module>,
          port 8080:8080
Use --port none for applications that do not expose an HTTP port.
EOF
}

if [[ $# -lt 1 || "$1" == "-h" || "$1" == "--help" ]]; then
    usage
    exit $(( $# == 1 ? 0 : 1 ))
fi

MODULE_PATH="$1"
shift
IMAGE=""
CONTAINER_NAME=""
PORT_MAPPING="8080:8080"
ENV_FILE=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --image|--name|--port|--env-file)
            if [[ $# -lt 2 ]]; then
                echo "Error: missing value for $1" >&2
                usage >&2
                exit 1
            fi
            case "$1" in
                --image) IMAGE="$2" ;;
                --name) CONTAINER_NAME="$2" ;;
                --port) PORT_MAPPING="$2" ;;
                --env-file) ENV_FILE="$2" ;;
            esac
            shift 2
            ;;
        *)
            echo "Error: unknown option: $1" >&2
            usage >&2
            exit 1
            ;;
    esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
MODULE_DIR="$(cd "${PROJECT_ROOT}/${MODULE_PATH}" 2>/dev/null && pwd)" || {
    echo "Error: module directory does not exist: ${MODULE_PATH}" >&2
    exit 1
}

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

if ! command -v docker >/dev/null 2>&1; then
    echo "Error: Docker CLI not found; install Docker and ensure its daemon is running." >&2
    exit 1
fi
if ! docker info >/dev/null 2>&1; then
    echo "Error: Docker daemon is unavailable; start Docker and try again." >&2
    exit 1
fi

if [[ -n "${ENV_FILE}" && ! -f "${ENV_FILE}" ]]; then
    echo "Error: environment file not found: ${ENV_FILE}" >&2
    exit 1
fi

MODULE_SLUG="$(printf '%s' "${MODULE_PATH}" | tr '/[:upper:]' '-[:lower:]')"
IMAGE="${IMAGE:-spring-ai-${MODULE_SLUG}:latest}"
CONTAINER_NAME="${CONTAINER_NAME:-spring-ai-${MODULE_SLUG}}"

"${SCRIPT_DIR}/build-example.sh" "${MODULE_PATH}"

shopt -s nullglob
JAR_CANDIDATES=()
for jar in "${MODULE_DIR}"/target/*.jar; do
    case "${jar}" in
        *.original.jar|*-sources.jar|*-javadoc.jar|*-plain.jar) continue ;;
        *) JAR_CANDIDATES+=("${jar}") ;;
    esac
done

if [[ ${#JAR_CANDIDATES[@]} -ne 1 ]]; then
    echo "Error: expected one runnable JAR in ${MODULE_DIR}/target, found ${#JAR_CANDIDATES[@]}." >&2
    echo "Select a Spring Boot application module that produces one executable JAR." >&2
    exit 1
fi

CONTEXT_DIR="$(mktemp -d "${TMPDIR:-/tmp}/spring-ai-docker.XXXXXX")"
cleanup() {
    rm -rf "${CONTEXT_DIR}"
}
trap cleanup EXIT
cp "${JAR_CANDIDATES[0]}" "${CONTEXT_DIR}/app.jar"

echo "Building Docker image ${IMAGE}"
docker build -t "${IMAGE}" -f "${SCRIPT_DIR}/Dockerfile.java25" "${CONTEXT_DIR}"

DOCKER_RUN_ARGS=(--detach --name "${CONTAINER_NAME}" --restart unless-stopped)
if [[ "${PORT_MAPPING}" != "none" ]]; then
    DOCKER_RUN_ARGS+=(--publish "${PORT_MAPPING}")
fi
if [[ -n "${ENV_FILE}" ]]; then
    DOCKER_RUN_ARGS+=(--env-file "${ENV_FILE}")
fi

CONTAINER_ID="$(docker run "${DOCKER_RUN_ARGS[@]}" "${IMAGE}")"
echo "Started container ${CONTAINER_NAME}: ${CONTAINER_ID}"
