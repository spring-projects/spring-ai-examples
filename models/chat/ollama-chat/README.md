# Spring AI Hello World Chat Application using Ollama chat model

A simple command-line chat application demonstrating Spring AI's ChatClient capabilities with Ollama AI models.

## Prerequisites
- Java 17 or higher
- Maven
- Postman
- Access to a running Ollama instance
  - eg: Download and install Ollama on your local machine from https://ollama.com/download and follow instructions to run the instance.

## Running the Application
1. Clone the repository
2. Navigate to the project directory
3. Run the application using Maven wrapper:
   `./mvnw spring-boot:run`
4. Use postman to invoke prompts
   eg: http://localhost:8080/ai/chat?prompt=which model are you using?
