# Project Title

## Overview

This project serves as an example of how to leverage the Spring AI framework to build applications that utilize generative AI capabilities, specifically focusing on vector embeddings and similarity search.

**Key Features & Technologies:**

*   **Spring AI:** Utilizes the Spring AI library to simplify the integration of AI functionalities into a Spring Boot application.
*   **Google Cloud Vertex AI Embeddings:** Demonstrates how to generate text embeddings using Google Cloud's Vertex AI PaLM 2 embedding models. These embeddings convert textual data into numerical vectors, capturing semantic meaning.
*   **MariaDB as a Vector Database:** Shows how MariaDB, with its vector storage and search capabilities (e.g., using its `VECTOR` data type and functions like `VECTOR_COSINE_DISTANCE`), can be used to store and perform similarity searches on the generated Vertex AI embeddings.
*   **Vector Search/Similarity Search:** The core functionality likely involves ingesting data, generating embeddings for it, storing these embeddings in MariaDB, and then performing queries to find items semantically similar to a given input query.

**Purpose:**

The primary goal of this example is to provide developers with a practical, hands-on demonstration of:
1.  Generating high-quality text embeddings with Vertex AI.
2.  Storing these vector embeddings efficiently in MariaDB.
3.  Performing semantic similarity searches against the stored vectors using Spring AI abstractions.
4.  Integrating these components within a standard Spring Boot application.

## Prerequisites

Before you begin, ensure you have the following dependencies and prerequisites met:

*   You have installed [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) (which includes `gcloud`).
*   You have a Google Cloud Project.
*   You have appropriate permissions to authenticate and access resources in your Google Cloud Project.
*   [Optional: Add any other project-specific prerequisites, e.g., Node.js, Python, Docker, etc.]

## Setup

Follow these steps to get your development environment set up:

### 1. Clone the Repository (if applicable)

```bash
git clone <your-repository-url>
cd <your-project-directory>
```

### 2. Set Up Environment Variables

This project requires certain environment variables to be set. The most common one is `PROJECT_ID`.

**Option A: Using a `.env` file (Recommended for local development)**

Create a file named `.env` in the root of your project directory. Add your environment variables there:

```env
# .env
PROJECT_ID="your-gcp-project-id"
# Add other environment variables as needed
# ANOTHER_VAR="some-value"
```

**Important:** Add `.env` to your `.gitignore` file to prevent committing sensitive information.

```gitignore
# .gitignore
.env
```

**Option B: Exporting directly in your shell**

You can set environment variables directly in your terminal session. These will be lost when the session ends.

```bash
export PROJECT_ID="your-gcp-project-id"
# export ANOTHER_VAR="some-value"
```

For persistent shell-specific environment variables, add these export commands to your shell's configuration file (e.g., `~/.bashrc`, `~/.zshrc`).

### 3. Configure Google Cloud Application Default Credentials (ADC)

Application Default Credentials (ADC) provide a way for your application to authenticate to Google Cloud services.

1.  **Log in to gcloud:**
    If you haven't already, authenticate `gcloud` with your Google Cloud account:
    ```bash
    gcloud auth login
    ```
    Follow the on-screen instructions to authorize `gcloud`.

2.  **Set up Application Default Credentials:**
    This command will store your user credentials in a well-known location on your local machine, which Google Cloud client libraries can automatically find.
    ```bash
    gcloud auth application-default login
    ```
    This will also open a browser window for you to authenticate.

    Your application will now be able to use these credentials to authenticate to Google Cloud services when running locally, provided the authenticated user has the necessary IAM permissions for the resources your application needs to access.

### 4. [Optional: Add other setup steps]

*   Install dependencies: `npm install`, `pip install -r requirements.txt`, etc.
*   Database setup instructions.
*   ...

## Running the Application

[Instructions on how to run your application, e.g., `npm start`, `python app.py`]

## Debugging

If you are using VS Code, ensure your `.vscode/launch.json` is configured to pass environment variables. For example:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "name": "Launch Program",
            "type": "node", // or "python", etc.
            "request": "launch",
            "program": "${workspaceFolder}/your-main-file.js",
            "env": {
                "PROJECT_ID": "your-gcp-project-id"
                // You can also use "${env:PROJECT_ID}" if it's set in your shell
            }
        }
    ]
}
```

Replace `"your-gcp-project-id"` with your actual Project ID or use environment variable substitution if you prefer.

---

Remember to replace placeholders like `<your-repository-url>`, `<your-project-directory>`, `"your-gcp-project-id"`, and any other project-specific details with your actual information.

This README provides a good starting point. You can expand it further with more specific details about your project, such as deployment instructions, contribution guidelines, or licensing information.