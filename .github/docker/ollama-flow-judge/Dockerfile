# ============================================================================
# Ollama with Flow Judge Model Pre-loaded
# ============================================================================
#
# This image contains Ollama with the flow-judge model pre-downloaded
# for use in CI/CD pipelines where downloading large models is impractical.
#
# Model: avcodes/flowaicom-flow-judge:q4 (~2.5GB)
# Base: ollama/ollama:latest
#
# Build:
#   docker build -t ollama-flow-judge .
#
# Run:
#   docker run -d -p 11434:11434 ollama-flow-judge
#
# ============================================================================

FROM ollama/ollama:latest

# Set environment variables
ENV OLLAMA_HOST=0.0.0.0
ENV OLLAMA_MODELS=/root/.ollama/models

# Create models directory
RUN mkdir -p /root/.ollama/models

# Download the flow-judge model during build
# This runs ollama in the background, pulls the model, then stops
RUN ollama serve & \
    sleep 5 && \
    ollama pull avcodes/flowaicom-flow-judge:q4 && \
    pkill ollama

# Expose the Ollama API port
EXPOSE 11434

# Default command - start Ollama server
ENTRYPOINT ["/bin/ollama"]
CMD ["serve"]
