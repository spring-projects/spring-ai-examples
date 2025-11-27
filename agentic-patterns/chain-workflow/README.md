# Prompt Chaining Workflow Example

This project demonstrates the Prompt Chaining workflow pattern for Large Language Models (LLMs) using Spring AI. The pattern decomposes complex tasks into a sequence of steps, where each LLM call processes the output of the previous one.

![Prompt Chaining Workflow](https://www.anthropic.com/_next/image?url=https%3A%2F%2Fwww-cdn.anthropic.com%2Fimages%2F4zrzovbb%2Fwebsite%2F7418719e3dab222dccb379b8879e1dc08ad34c78-2401x1000.png&w=3840&q=75)

## Overview

The prompt chaining pattern is particularly useful when:
- Complex tasks can be broken down into simpler, sequential steps
- Each step's output needs to be validated or transformed
- The process requires maintaining a clear chain of transformations

This implementation shows a four-step workflow for processing numerical data in text:
1. Extract numerical values and metrics
2. Standardize to percentage format
3. Sort in descending order
4. Format as markdown table

## Technical Requirements

- Java 17 or higher
- Spring Boot 4.0.0
- Spring AI 2.0.0-SNAPSHOT
- Ollama (for LLM integration)

## Getting Started

1. Install and start Ollama following the instructions at [ollama.ai](https://ollama.ai)

2. Build the project:
   ```bash
   ./mvnw clean install
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Example Usage

The example processes a Q3 performance report through the chain of prompts. Here's the sample input:

```text
Q3 Performance Summary:
Our customer satisfaction score rose to 92 points this quarter.
Revenue grew by 45% compared to last year.
Market share is now at 23% in our primary market.
Customer churn decreased to 5% from 8%.
New user acquisition cost is $43 per user.
Product adoption rate increased to 78%.
Employee satisfaction is at 87 points.
Operating margin improved to 34%.
```

The workflow processes this through four steps:

1. **Extract Values**: Pulls out numerical values and their metrics
   ```
   92: customer satisfaction
   45%: revenue growth
   23%: market share
   5%: customer churn
   43: user acquisition cost
   78%: product adoption
   87: employee satisfaction
   34%: operating margin
   ```

2. **Standardize Format**: Converts values to percentages where applicable
   ```
   92%: customer satisfaction
   45%: revenue growth
   23%: market share
   5%: customer churn
   78%: product adoption
   87%: employee satisfaction
   34%: operating margin
   ```

3. **Sort**: Orders values in descending order
   ```
   92%: customer satisfaction
   87%: employee satisfaction
   78%: product adoption
   45%: revenue growth
   34%: operating margin
   23%: market share
   5%: customer churn
   ```

4. **Format**: Creates a markdown table
   ```markdown
   | Metric | Value |
   |:--|--:|
   | Customer Satisfaction | 92% |
   | Employee Satisfaction | 87% |
   | Product Adoption | 78% |
   | Revenue Growth | 45% |
   | Operating Margin | 34% |
   | Market Share | 23% |
   | Customer Churn | 5% |
   ```

## Implementation Details

The workflow is implemented in two main classes:

1. `ChainWorkflow.java`: Contains the core logic for the prompt chaining pattern, including:
   - System prompts for each transformation step
   - Chain execution logic
   - Gate validation between steps

2. `Application.java`: Provides the Spring Boot setup and example usage:
   - Sample input data
   - Spring AI configuration
   - Command-line runner for demonstration

Each step in the chain acts as a gate that validates and transforms the output before proceeding to the next step, ensuring the process stays on track.

## References

This implementation is based on the prompt chaining pattern described in Anthropic's research paper [Building Effective Agents](https://www.anthropic.com/research/building-effective-agents).
