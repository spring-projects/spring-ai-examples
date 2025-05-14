/* 
* Copyright 2024 - 2024 the original author or authors.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* https://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example.agentic;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

/**
 * Workflow: <b>Evaluator-optimizer</b>
 * <p/>
 * Implements the Evaluator-Optimizer workflow pattern for Large Language Model
 * (LLM) interactions. This workflow orchestrates a dual-LLM process where one
 * model
 * generates responses while another provides evaluation and feedback in an
 * iterative loop,
 * similar to a human writer's iterative refinement process.
 *
 * <p>
 * The workflow consists of two main components:
 * <ul>
 * <li>A generator LLM that produces initial responses and refines them based on
 * feedback</li>
 * <li>An evaluator LLM that analyzes responses and provides detailed feedback
 * for improvement</li>
 * </ul>
 *
 * <b>Usage Criteria</b>
 * This workflow is particularly effective in scenarios that meet the following
 * conditions:
 * <ul>
 * <li>Clear evaluation criteria exist for assessing response quality</li>
 * <li>Iterative refinement provides measurable value to the output</li>
 * <li>The task benefits from multiple rounds of critique and improvement</li>
 * </ul>
 *
 * <b>Fitness Indicators</b>
 * Two key indicators suggest this workflow is appropriate:
 * <ul>
 * <li>LLM responses can be demonstrably improved when feedback is
 * articulated</li>
 * <li>The evaluator LLM can provide substantive and actionable feedback</li>
 * </ul>
 *
 * <b>Example Applications</b>
 * <ul>
 * <li>Literary translation requiring capture of subtle nuances through
 * iterative refinement</li>
 * <li>Complex search tasks needing multiple rounds of searching and
 * analysis</li>
 * <li>Code generation where quality can be improved through systematic
 * review</li>
 * <li>Content creation requiring multiple drafts and specific improvements</li>
 * </ul>
 * 
 * @author Christian Tzolov
 * @see <a href=
 *      "https://www.anthropic.com/research/building-effective-agents">Building
 *      effective agents</a>
 */
@SuppressWarnings("null")
public class EvaluatorOptimizer {

	public static final String DEFAULT_GENERATOR_PROMPT = """
			Your goal is to complete the task based on the input. If there are feedback
			from your previous generations, you should reflect on them to improve your solution.

			CRITICAL: Your response must be a SINGLE LINE of valid JSON with NO LINE BREAKS except those explicitly escaped with \\n.
			Here is the exact format to follow, including all quotes and braces:

			{"thoughts":"Brief description here","response":"public class Example {\\n    // Code here\\n}"}

			Rules for the response field:
			1. ALL line breaks must use \\n
			2. ALL quotes must use \\"
			3. ALL backslashes must be doubled: \\
			4. NO actual line breaks or formatting - everything on one line
			5. NO tabs or special characters
			6. Java code must be complete and properly escaped

			Example of properly formatted response:
			{"thoughts":"Implementing counter","response":"public class Counter {\\n    private int count;\\n    public Counter() {\\n        count = 0;\\n    }\\n    public void increment() {\\n        count++;\\n    }\\n}"}

			Follow this format EXACTLY - your response must be valid JSON on a single line.
			""";

	public static final String DEFAULT_EVALUATOR_PROMPT = """
			Evaluate this code implementation for correctness, time complexity, and best practices.
			Ensure the code have proper javadoc documentation.
			Respond with EXACTLY this JSON format on a single line:

			{"evaluation":"PASS, NEEDS_IMPROVEMENT, or FAIL", "feedback":"Your feedback here"}

			The evaluation field must be one of: "PASS", "NEEDS_IMPROVEMENT", "FAIL"
			Use "PASS" only if all criteria are met with no improvements needed.
			""";

	/**
	 * Represents a solution generation step. Contains the model's thoughts and the
	 * proposed solution.
	 * 
	 * @param thoughts The model's understanding of the task and feedback
	 * @param response The model's proposed solution
	 */
	public static record Generation(String thoughts, String response) {
	}

	/**
	 * Represents an evaluation response. Contains the evaluation result and
	 * detailed feedback.
	 * 
	 * @param evaluation The evaluation result (PASS, NEEDS_IMPROVEMENT, or FAIL)
	 * @param feedback   Detailed feedback for improvement
	 */
	public static record EvaluationResponse(Evaluation evaluation, String feedback) {

		public enum Evaluation {
			PASS, NEEDS_IMPROVEMENT, FAIL
		}
	}

	/**
	 * Represents the final refined response. Contains the final solution and the
	 * chain of thought showing the evolution of the solution.
	 * 
	 * @param solution       The final solution
	 * @param chainOfThought The chain of thought showing the evolution of the
	 *                       solution
	 */
	public static record RefinedResponse(String solution, List<Generation> chainOfThought) {
	}

	private final ChatClient chatClient;

	private final String generatorPrompt;

	private final String evaluatorPrompt;

	public EvaluatorOptimizer(ChatClient chatClient) {
		this(chatClient, DEFAULT_GENERATOR_PROMPT, DEFAULT_EVALUATOR_PROMPT);
	}

	public EvaluatorOptimizer(ChatClient chatClient, String generatorPrompt, String evaluatorPrompt) {
		Assert.notNull(chatClient, "ChatClient must not be null");
		Assert.hasText(generatorPrompt, "Generator prompt must not be empty");
		Assert.hasText(evaluatorPrompt, "Evaluator prompt must not be empty");

		this.chatClient = chatClient;
		this.generatorPrompt = generatorPrompt;
		this.evaluatorPrompt = evaluatorPrompt;
	}

	/**
	 * Initiates the evaluator-optimizer workflow for a given task. This method
	 * orchestrates the iterative process of generation and evaluation until a
	 * satisfactory solution is reached.
	 * 
	 * <p>
	 * The workflow follows these steps:
	 * </p>
	 * <ol>
	 * <li>Generate an initial solution</li>
	 * <li>Evaluate the solution against quality criteria</li>
	 * <li>If evaluation passes, return the solution</li>
	 * <li>If evaluation indicates need for improvement, incorporate feedback and
	 * generate new solution</li>
	 * <li>Repeat steps 2-4 until a satisfactory solution is achieved</li>
	 * </ol>
	 * 
	 * @param task The task or problem to be solved through iterative refinement
	 * @return A RefinedResponse containing the final solution and the chain of
	 *         thought
	 *         showing the evolution of the solution
	 */
	public RefinedResponse loop(String task) {
		List<String> memory = new ArrayList<>();
		List<Generation> chainOfThought = new ArrayList<>();

		return loop(task, "", memory, chainOfThought);
	}

	/**
	 * Internal recursive implementation of the evaluator-optimizer loop. This
	 * method
	 * maintains the state of previous attempts and feedback while recursively
	 * refining
	 * the solution until it meets the evaluation criteria.
	 * 
	 * @param task           The original task to be solved
	 * @param context        Accumulated context including previous attempts and
	 *                       feedback
	 * @param memory         List of previous solution attempts for reference
	 * @param chainOfThought List tracking the evolution of solutions and reasoning
	 * @return A RefinedResponse containing the final solution and complete solution
	 *         history
	 */
	private RefinedResponse loop(String task, String context, List<String> memory,
			List<Generation> chainOfThought) {

		Generation generation = generate(task, context);
		memory.add(generation.response());
		chainOfThought.add(generation);

		EvaluationResponse evaluationResponse = evalute(generation.response(), task);

		if (evaluationResponse.evaluation().equals(EvaluationResponse.Evaluation.PASS)) {
			// Solution is accepted!
			return new RefinedResponse(generation.response(), chainOfThought);
		}

		// Accumulated new context including the last and the previous attempts and
		// feedbacks.
		StringBuilder newContext = new StringBuilder();
		newContext.append("Previous attempts:");
		for (String m : memory) {
			newContext.append("\n- ").append(m);
		}
		newContext.append("\nFeedback: ").append(evaluationResponse.feedback());

		return loop(task, newContext.toString(), memory, chainOfThought);
	}

	/**
	 * Generates or refines a solution based on the given task and feedback context.
	 * This method represents the generator component of the workflow, producing
	 * responses that can be iteratively improved through evaluation feedback.
	 * 
	 * @param task    The primary task or problem to be solved
	 * @param context Previous attempts and feedback for iterative improvement
	 * @return A Generation containing the model's thoughts and proposed solution
	 */
	private Generation generate(String task, String context) {
		Generation generationResponse = chatClient.prompt()
				.user(u -> u.text("{prompt}\n{context}\nTask: {task}")
						.param("prompt", this.generatorPrompt)
						.param("context", context)
						.param("task", task))
				.call()
				.entity(Generation.class);

		System.out.println(String.format("\n=== GENERATOR OUTPUT ===\nTHOUGHTS: %s\n\nRESPONSE:\n %s\n",
				generationResponse.thoughts(), generationResponse.response()));
		return generationResponse;
	}

	/**
	 * Evaluates if a solution meets the specified requirements and quality
	 * criteria.
	 * This method represents the evaluator component of the workflow, analyzing
	 * solutions
	 * and providing detailed feedback for further refinement until the desired
	 * quality
	 * level is reached.
	 * 
	 * @param content The solution content to be evaluated
	 * @param task    The original task against which to evaluate the solution
	 * @return An EvaluationResponse containing the evaluation result
	 *         (PASS/NEEDS_IMPROVEMENT/FAIL)
	 *         and detailed feedback for improvement
	 */
	private EvaluationResponse evaluate(String content, String task) {

		EvaluationResponse evaluationResponse = chatClient.prompt()
				.user(u -> u.text("{prompt}\nOriginal task: {task}\nContent to evaluate: {content}")
						.param("prompt", this.evaluatorPrompt)
						.param("task", task)
						.param("content", content))
				.call()
				.entity(EvaluationResponse.class);

		System.out.println(String.format("\n=== EVALUATOR OUTPUT ===\nEVALUATION: %s\n\nFEEDBACK: %s\n",
				evaluationResponse.evaluation(), evaluationResponse.feedback()));
		return evaluationResponse;
	}

}
