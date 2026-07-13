# Spring AI RAG with PGVector Example

This is an example project demonstrating **Retrieval-Augmented Generation (RAG)** using **Spring AI**, with **PGVector** as the vector database and **Ollama** for running LLM and embedding models. This setup uses **Docker** for running the PGVector database locally.

---

## 🧠 What This Project Does

- Upload a PDF file with paragraphs to the application.
- Extracts content and embeds it using a local embedding model via Ollama.
- Stores the embeddings in a PostgreSQL + PGVector database.
- Enables retrieval and interaction with a local chat LLM (via Ollama).
- Provides a simple RAG flow powered by Spring AI.

---

## 🛠 Prerequisites

Before running the application, make sure you have the following installed and configured:

- **Java 17**
- **Docker** (to run PGVector via `compose.yaml`)
- **Ollama** installed locally with:
    - `gemma:3b` (chat model)
    - `nomic-embed-text:latest` (embedding model)
- **Postman** or any API testing tool (for uploading PDF files)
- **DBeaver** or another database UI client (optional, for inspecting the database)

---

## ⚙️ Setup Instructions

### 1. Start the PGVector Database

Ensure Docker is running and start the PGVector service using the provided `compose.yaml`:

```bash
docker compose up -d
