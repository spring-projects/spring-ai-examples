# spring-ai-examples

[![Integration Tests](https://github.com/spring-projects/spring-ai-examples/actions/workflows/integration-tests.yml/badge.svg)](https://github.com/spring-projects/spring-ai-examples/actions/workflows/integration-tests.yml)

## Baseline Maintenance

### Environment

- Put runtime credentials in environment variables.
- Use `.env.example` as the configuration template.

### CI

- `baseline-ci.yml` provides a unified pipeline with `lint + build + test + secret scan`.

### Repo Hygiene

- Keep generated files (`dist/`, `build/`, `__pycache__/`, `.idea/`, `.DS_Store`) out of version control.

## Audit Baseline Notes

### Requirements

- Environment requirements are defined by this module and parent project documentation.
- Configure credentials via environment variables before startup.
- Use `.env.example` (or equivalent sample config) for local setup.

### Run

- Install dependencies for this module before execution.
- Use the standard project command to build and run (for example Maven, Gradle, npm, or Python entrypoint scripts in this repository).


## Contributor Quick Verification
<!-- codex-batch30-readme -->

Before opening a PR, run these quick checks locally:

- Build: `git status`
- Tests: `(no dedicated test command documented)`
- Keep changes focused: one topic per PR with a clear title

