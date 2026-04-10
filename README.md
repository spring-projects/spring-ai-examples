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

