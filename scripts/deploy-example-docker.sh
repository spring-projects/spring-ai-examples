#!/usr/bin/env bash

set -euo pipefail

usage() {
    cat <<EOF
Usage: $0 [<module-directory> | --select] [--image name:tag] [--name container]
       [--port host:container|none] [--env-file path]
       [--anthropic-url URL] [--ollama-url URL] [--ollama-model NAME]
       [--anthropic-api-key KEY]

Build a Maven example, create a Java 25 Docker image, and run it in the background.
If the module directory is omitted, choose an example from an interactive menu.
Defaults: image spring-ai-<module>:latest, container spring-ai-<module>,
          port 8080:8080
Use --port none for applications that do not expose an HTTP port.
Ollama URL defaults to http://host.docker.internal:11434 (override with --ollama-url).
Use --env-file for secrets when possible; --anthropic-api-key is provided directly
to Docker and may be visible in process listings and container metadata.
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit $(( $# == 1 ? 0 : 1 ))
fi

SELECT_EXAMPLE=0
MODULE_PATH=""
if [[ $# -eq 0 ]]; then
    SELECT_EXAMPLE=1
elif [[ "$1" == "--select" ]]; then
    SELECT_EXAMPLE=1
    shift
elif [[ "$1" == -* ]]; then
    SELECT_EXAMPLE=1
else
    MODULE_PATH="$1"
    shift
fi

IMAGE=""
CONTAINER_NAME=""
PORT_MAPPING="8080:8080"
ENV_FILE=""
ANTHROPIC_URL=""
OLLAMA_URL=""
OLLAMA_MODEL=""
ANTHROPIC_API_KEY=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --image|--name|--port|--env-file|--anthropic-url|--ollama-url|--ollama-model|--anthropic-api-key)
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
                --anthropic-url) ANTHROPIC_URL="$2" ;;
                --ollama-url) OLLAMA_URL="$2" ;;
                --ollama-model) OLLAMA_MODEL="$2" ;;
                --anthropic-api-key) ANTHROPIC_API_KEY="$2" ;;
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

if [[ "${SELECT_EXAMPLE}" -eq 1 ]]; then
    MODULE_CANDIDATES=()
    while IFS= read -r pom; do
        [[ "${pom}" == "${PROJECT_ROOT}/pom.xml" ]] && continue
        if grep -q '<artifactId>spring-boot-maven-plugin</artifactId>' "${pom}"; then
            MODULE_CANDIDATES+=("${pom#"${PROJECT_ROOT}"/}")
        fi
    done < <(find "${PROJECT_ROOT}" -type f -name pom.xml \
        -not -path "${PROJECT_ROOT}/.git/*" | LC_ALL=C sort)

    if [[ ${#MODULE_CANDIDATES[@]} -eq 0 ]]; then
        echo "Error: no Spring Boot Maven examples were found." >&2
        exit 1
    fi

    printf 'Available Spring Boot examples:\n'
    for index in "${!MODULE_CANDIDATES[@]}"; do
        printf '  %d) %s\n' "$((index + 1))" "${MODULE_CANDIDATES[index]%/pom.xml}"
    done

    if ! read -r -p "Select an example by number (or q to cancel): " selection; then
        echo "Error: no example selection was provided." >&2
        exit 1
    fi
    case "${selection}" in
        q|Q)
            echo "Example selection cancelled." >&2
            exit 1
            ;;
    esac

    MODULE_PATH=""
    for index in "${!MODULE_CANDIDATES[@]}"; do
        if [[ "${selection}" == "$((index + 1))" ]]; then
            MODULE_PATH="${MODULE_CANDIDATES[index]%/pom.xml}"
            break
        fi
    done
    if [[ -z "${MODULE_PATH}" ]]; then
        echo "Error: invalid example selection: ${selection}" >&2
        exit 1
    fi
fi

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
if [[ -n "${ANTHROPIC_URL}" ]]; then
    DOCKER_RUN_ARGS+=(--env "SPRING_AI_ANTHROPIC_BASE_URL=${ANTHROPIC_URL}")
fi
OLLAMA_URL="${OLLAMA_URL:-http://host.docker.internal:11434}"
DOCKER_RUN_ARGS+=(--env "OLLAMA_BASE_URL=${OLLAMA_URL}")
if [[ -n "${OLLAMA_MODEL}" ]]; then
    DOCKER_RUN_ARGS+=(--env "OLLAMA_MODEL=${OLLAMA_MODEL}")
fi
if [[ -n "${ANTHROPIC_API_KEY}" ]]; then
    DOCKER_RUN_ARGS+=(--env "ANTHROPIC_API_KEY=${ANTHROPIC_API_KEY}")
fi

CONTAINER_ID="$(docker run "${DOCKER_RUN_ARGS[@]}" "${IMAGE}")"
echo "Started container ${CONTAINER_NAME}: ${CONTAINER_ID}"
