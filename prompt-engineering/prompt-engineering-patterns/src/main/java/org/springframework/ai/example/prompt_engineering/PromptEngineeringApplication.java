package org.springframework.ai.example.prompt_engineering;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author Christian Tzolov
 */
@SpringBootApplication
public class PromptEngineeringApplication {

	JsonMapper jsonMapper = new JsonMapper();

	public static void main(String[] args) {
		SpringApplication.run(PromptEngineeringApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder,
			ConfigurableApplicationContext context) {
		return args -> {
			try {
				ChatClient chatClient = chatClientBuilder.build();

				// Check if specific pattern is requested via command line argument
				String pattern = args.length > 0 ? args[0] : "all";

				switch (pattern.toLowerCase()) {
					case "basic":
						runBasicPatterns(chatClient);
						break;
					case "system":
						runSystemPatterns(chatClient);
						break;
					case "advanced":
						runAdvancedPatterns(chatClient);
						break;
					case "all":
					default:
						runAllPatterns(chatClient);
						break;
				}

				System.out.println("Prompt engineering patterns demonstration completed!");
				context.close();
			}
			catch (Exception e) {
				System.err.println("Error during prompt engineering execution: " + e.getMessage());
				e.printStackTrace();
				context.close();
			}
		};
	}

	private void runBasicPatterns(ChatClient chatClient) {
		System.out.println("=== Running Basic Patterns ===");
		// 2.1 General prompting / zero shot
		pt_zero_shot(chatClient);
		// 2.2 One-shot & few-shot
		pt_ones_shot_few_shots(chatClient);
	}

	private void runSystemPatterns(ChatClient chatClient) {
		System.out.println("=== Running System/Role Patterns ===");
		// 2.3.1 System prompting
		pt_system_prompting_1(chatClient);
		pt_system_prompting_2_springai_style(chatClient);
		// 2.3.2 Role prompting
		pt_role_prompting_1(chatClient);
	}

	private void runAdvancedPatterns(ChatClient chatClient) {
		System.out.println("=== Running Advanced Patterns ===");
		// 2.6 Self-consistency (simplified - just one iteration)
		pt_self_consistency_simple(chatClient);
	}

	private void runAllPatterns(ChatClient chatClient) {
		//@formatter:off
		// 2. Prompting techniques (page 13)

			// 2.1 General prompting / zero shot (page 15)
			pt_zero_shot(chatClient);

			// 2.2 One-shot & few-shot
			pt_ones_shot_few_shots(chatClient);

			// 2.3 System, contextual and role prompting

				// 2.3.1 System prompting
				pt_system_prompting_1(chatClient);
				pt_system_prompting_2(chatClient);
				pt_system_prompting_2_springai_style(chatClient);

				// 2.3.2 Role prompting
				pt_role_prompting_1(chatClient);
				pt_role_prompting_2(chatClient); // in a humorous style.

				// 2.3.3 Contextual prompting
				pt_contextual_prompting(chatClient);

			// 2.4 Step-back prompting
			pt_step_back_prompting(chatClient.mutate());

			// 2.5 Chain of Thought (CoT)
			pt_chain_of_thought_zero_shot(chatClient);
			pt_chain_of_thought_singleshot_fewshots(chatClient);

			// 2.6 Self-consistency
			pt_self_consistency(chatClient);

			// 2.7 Tree of Thoughts (ToT)
			pt_tree_of_thoughts_game(chatClient);
			pt_tree_of_thoughts_problem(chatClient);

			// 2.8 Automatic Prompt Engineering
			pt_automatic_prompt_engineering(chatClient);

			// 2.9 Code prompting

				// 2.9.1 Prompts for writing code
				pt_code_prompting_writing_code(chatClient);

				// 2.9.2 Prompts for explaining code
				pt_code_prompting_explaining_code(chatClient);

				// 2.9.3 Prompts for translating code
				pt_code_prompting_translating_code(chatClient);
		//@formatter:on
	}

	// 2.1 General prompting / zero shot (page 15)
	public void pt_zero_shot(ChatClient chatClient) {
		// General prompting / zero shot (page 15)

		enum Sentiment {

			POSITIVE, NEUTRAL, NEGATIVE

		}

		Sentiment reviewSentiment = chatClient.prompt("""
				Classify movie reviews as POSITIVE, NEUTRAL or NEGATIVE.
				Review: "Her" is a disturbing study revealing the direction
				humanity is headed if AI is allowed to keep evolving,
				unchecked. I wish there were more movies like this masterpiece.
				Sentiment:
				""")
			.options(ChatOptions.builder().temperature(0.1).maxTokens(5).build())
			.call()
			.entity(Sentiment.class);

		System.out.println("Output: " + reviewSentiment);
	}

	// 2.2 One-shot & few-shot
	public void pt_ones_shot_few_shots(ChatClient chatClient) {

		// One-shot & few-shot
		// When creating prompts for AI models, it is helpful to provide examples.
		// These examples can help the model understand what you are asking for.
		// - one-shot prompt: provides a single example to the model.
		// - few-shot prompt: provides multiple examples to the model.

		// A few-shot prompt example, let's use the same gemini-pro model
		// configuration settings as before, other than increasing the token limit to
		// accommodate the
		// need for a longer response. (page 16)

		String pizzaOrder = chatClient.prompt("""
				Parse a customer's pizza order into valid JSON

				EXAMPLE 1:
				I want a small pizza with cheese, tomato sauce, and pepperoni.
				JSON Response:
				```
				{
					"size": "small",
					"type": "normal",
					"ingredients": ["cheese", "tomato sauce", "peperoni"]
				}
				```

				EXAMPLE 2:
				Can I get a large pizza with tomato sauce, basil and mozzarella.
				JSON Response:
				```
				{
					"size": "large",
					"type": "normal",
					"ingredients": ["tomato sauce", "basil", "mozzarella"]
				}
				```

				Now, I would like a large pizza, with the first half cheese and mozzarella.
				And the other tomato sauce, ham and pineapple.

				""")
			.options(ChatOptions.builder()
				.temperature(0.1)
				// Increasing the token limit to accommodate the need for a longer
				// response.
				.maxTokens(250)
				.build())
			.call()
			.content();

		System.out.println("Output: " + pizzaOrder);

		// NOTE: The number of examples you need for few-shot prompting depends on a few
		// factors,
		// including the complexity of the task, the quality of the examples, and the
		// capabilities of the
		// generative AI (gen AI) model you are using. As a general rule of thumb, you
		// should use at
		// least three to five examples for few-shot prompting.
		//
		// To generate output that is robust to a variety of inputs, it is important to
		// include edge
		// cases in your examples. Edge cases are inputs that are unusual or unexpected,
		// but that the model should still be able to handle.
	}

	// Sets the overall context and purpose for the language model. It
	// defines the 'big picture' of what the model should be doing, like translating
	// a language, classifying a review etc.

	// 2.3.1 System prompting (1)
	public void pt_system_prompting_1(ChatClient chatClient) {

		String movieReview = chatClient.prompt()
			.system("Classify movie reviews as positive, neutral or negative. Only return the label in uppercase.")
			.user("""
					Review: "Her" is a disturbing study revealing the direction
					humanity is headed if AI is allowed to keep evolving,
					unchecked. It's so disturbing I couldn't watch it.

					Sentiment:
					""")
			.options(ChatOptions.builder().temperature(1.0).topK(40).maxTokens(5).build())
			.call()
			.content();

		System.out.println("Output: " + movieReview);

		// We increased the temperature to get a higher creativity level, and I
		// specified a higher token limit. However, because of my clear instruction on
		// how to return the output the model didn't return extra text
	}

	// You could use a system prompt to generate a code snippet that is compatible
	// with a specific programming language, or you could use a system prompt to
	// return a certain structure like output in JSON format.

	// 2.3.1 System prompting (2)
	public void pt_system_prompting_2(ChatClient chatClient) {

		String movieReview = chatClient.prompt().system("""
				Classify movie reviews as positive, neutral or negative. Return
				valid JSON.

				Use the Schema:

				```
				MOVIE:
				{
				"sentiment": String "POSITIVE" | "NEGATIVE" | "NEUTRAL",
				"name": String
				}
				MOVIE REVIEWS:
				{
				"movie_reviews": [MOVIE]
				}
				```
				""").user("""
				Review: "Her" is a disturbing study revealing the direction
				humanity is headed if AI is allowed to keep evolving,
				unchecked. It's so disturbing I couldn't watch it.

				JSON Response:
				""").options(ChatOptions.builder().temperature(1.0).topK(40).maxTokens(1024).build()).call().content();

		System.out.println("Output: " + movieReview);

	}

	// 2.3.1 (3) spring-ai System prompting - SpringAI style
	public void pt_system_prompting_2_springai_style(ChatClient chatClient) {

		record MovieReviews(Movie[] movie_reviews) {
			enum Sentiment {

				POSITIVE, NEUTRAL, NEGATIVE

			}

			record Movie(Sentiment sentiment, String name) {
			}
		}

		MovieReviews movieReviews = chatClient.prompt()
			.system("""
					Classify movie reviews as positive, neutral or negative. Return
					valid JSON.
					""")
			.user("""
					Review: "Her" is a disturbing study revealing the direction
					humanity is headed if AI is allowed to keep evolving,
					unchecked. It's so disturbing I couldn't watch it.

					JSON Response:
					""")
			.options(ChatOptions.builder().temperature(1.0).topK(40).maxTokens(1024).build())
			.call()
			.entity(MovieReviews.class);

		System.out.println("Output: " + jsonMapper.writeValueAsString(movieReviews));
	}

	// 2.3.2 Role prompting (1)
	public void pt_role_prompting_1(ChatClient chatClient) {

		// Goal Act as travel guide and provide 3 travel suggestions

		String movieReview = chatClient.prompt().system("""
				I want you to act as a travel guide. I will write to you
				about my location and you will suggest 3 places to visit near
				me. In some cases, I will also give you the type of places I
				will visit.
				""").user("""
				My suggestion: "I am in Amsterdam and I want to visit only museums."
				Travel Suggestions:
				""").options(ChatOptions.builder().temperature(1.0).topK(40).maxTokens(1024).build()).call().content();

		System.out.println("Output: " + movieReview);

	}

	// 2.3.2 Role prompting (2)
	public void pt_role_prompting_2(ChatClient chatClient) {

		// Goal Act as travel guide and provide 3 travel suggestions
		//
		// In a humorous style.

		String movieReview = chatClient.prompt().system("""
				I want you to act as a travel guide. I will write to you about
				my location and you will suggest 3 places to visit near me in
				a humorous style.
				""").user("""
				My suggestion: "I am in Amsterdam and I want to visit only museums."
				Travel Suggestions:
				""").options(ChatOptions.builder().temperature(1.0).topK(40).maxTokens(1024).build()).call().content();

		System.out.println("Output: " + movieReview);
	}

	// 2.3.3 Contextual prompting
	public void pt_contextual_prompting(ChatClient chatClient) {

		// Goal: Suggest articles for a blog about retro games

		String movieReview = chatClient.prompt()
			.user(u -> u.text("""
					Suggest 3 topics to write an article about with a few lines of
					description of what this article should contain.

					Context: {context}
					""").param("context", "You are writing for a blog about retro 80's arcade video games."))
			.options(ChatOptions.builder().temperature(1.0).topK(40).maxTokens(1024).build())
			.call()
			.content();

		System.out.println("Output: " + movieReview);

	}

	// 2.4 Step-back prompting
	public void pt_step_back_prompting(ChatClient.Builder chatClientBuilder) {

		// (SpringAI tip) Set common options for the chat client.
		var chatClient = chatClientBuilder
			.defaultOptions(ChatOptions.builder().temperature(1.0).topK(40).maxTokens(1024).build())
			.build();

		// Goal: Write a storyline for a level of a first-person shooter video game.

		String stepBack = chatClient.prompt("""
				Based on popular first-person shooter action games, what are
				5 fictional key settings that contribute to a challenging and
				engaging level storyline in a first-person shooter video game?
				""").call().content();

		System.out.println("StepBack Output: " + stepBack + "\n");

		String story = chatClient.prompt().user(u -> u.text("""
				Write a one paragraph storyline for a new level of a first-
				person shooter video game that is challenging and engaging.

				Context: {step-back}
				""").param("step-back", stepBack)).call().content();

		System.out.println("Output: " + story);

	}

	// 2.5 Chain of Thought (CoT)

	// 'zero-shot' Chain of thought.
	public void pt_chain_of_thought_zero_shot(ChatClient chatClient) {

		String output = chatClient.prompt("""
				When I was 3 years old, my partner was 3 times my age. Now,
				I am 20 years old. How old is my partner?

				Let's think step by step.
				""").call().content();

		System.out.println("Output: " + output + "\n");

	}

	public void pt_chain_of_thought_singleshot_fewshots(ChatClient chatClient) {

		String output = chatClient.prompt("""
				Q: When my brother was 2 years old, I was double his age. Now
				I am 40 years old. How old is my brother? Let's think step
				by step.
				A: When my brother was 2 years, I was 2 * 2 = 4 years old.
				That's an age difference of 2 years and I am older. Now I am 40
				years old, so my brother is 40 - 2 = 38 years old. The answer
				is 38.
				Q: When I was 3 years old, my partner was 3 times my age. Now,
				I am 20 years old. How old is my partner? Let's think step
				by step.
				A:
				""").call().content();

		System.out.println("Output: " + output + "\n");

	}

	// 2.6 Self-consistency (simplified for basic testing)
	public void pt_self_consistency_simple(ChatClient chatClient) {
		String email = """
				Hi,
				I have seen you use Wordpress for your website. A great open
				source content management system. I have used it in the past
				too. It comes with lots of great user plugins. And it's pretty
				easy to set up.
				I did notice a bug in the contact form, which happens when
				you select the name field. See the attached screenshot of me
				entering text in the name field. Notice the JavaScript alert
				box that I inv0k3d.
				But for the rest it's a great website. I enjoy reading it. Feel
				free to leave the bug in the website, because it gives me more
				interesting things to read.
				Cheers,
				Harry the Hacker.
				""";

		record EmailClassification(Classification classification, String reasoning) {
			enum Classification {

				IMPORTANT, NOT_IMPORTANT

			}
		}

		// Simplified version - only one classification for basic testing
		EmailClassification output = chatClient.prompt()
			.user(u -> u.text("""
					Email: {email}
					Classify the above email as IMPORTANT or NOT IMPORTANT. Let's
					think step by step and explain why.
					""").param("email", email))
			.options(ChatOptions.builder().temperature(1.0).topK(60).maxTokens(1024).build())
			.call()
			.entity(EmailClassification.class);

		System.out.println("Classification: [" + output.classification() + "], Reason: " + output.reasoning());
		System.out.println("The email is " + output.classification() + ".");
	}

	// 2.6 Self-consistency (full version)
	public void pt_self_consistency(ChatClient chatClient) {

		String email = """
				Hi,
				I have seen you use Wordpress for your website. A great open
				source content management system. I have used it in the past
				too. It comes with lots of great user plugins. And it's pretty
				easy to set up.
				I did notice a bug in the contact form, which happens when
				you select the name field. See the attached screenshot of me
				entering text in the name field. Notice the JavaScript alert
				box that I inv0k3d.
				But for the rest it's a great website. I enjoy reading it. Feel
				free to leave the bug in the website, because it gives me more
				interesting things to read.
				Cheers,
				Harry the Hacker.
				""";

		record EmailClassification(Classification classification, String reasoning) {
			enum Classification {

				IMPORTANT, NOT_IMPORTANT

			}
		}

		int importantCount = 0;
		int notImportantCount = 0;

		for (int i = 0; i < 5; i++) {

			EmailClassification output = chatClient.prompt()
				.user(u -> u.text("""
						Email: {email}
						Classify the above email as IMPORTANT or NOT IMPORTANT. Let's
						think step by step and explain why.
						""").param("email", email))
				.options(ChatOptions.builder().temperature(1.0).topK(60).maxTokens(1024).build())
				.call()
				.entity(EmailClassification.class);

			if (output.classification() == EmailClassification.Classification.IMPORTANT) {
				importantCount++;
			}
			else {
				notImportantCount++;
			}

			System.out
				.println("Classification: [" + output.classification() + "], Reason: " + output.reasoning() + "\n");
		}

		if (importantCount > notImportantCount) {
			System.out.println("The email is IMPORTANT. Count: " + importantCount);
		}
		else {
			System.out.println("The email is NOT IMPORTANT. Count: " + notImportantCount);
		}

	}

	// 2.7 Tree of Thoughts (ToT)
	// Implementation of Section 2.7: Tree of Thoughts (ToT) - Game solving example
	public void pt_tree_of_thoughts_game(ChatClient chatClient) {
		// Step 1: Generate multiple initial moves
		String initialMoves = chatClient.prompt("""
				You are playing a game of chess. The board is in the starting position.
				Generate 3 different possible opening moves. For each move:
				1. Describe the move in algebraic notation
				2. Explain the strategic thinking behind this move
				3. Rate the move's strength from 1-10
				""").options(ChatOptions.builder().temperature(0.7).build()).call().content();

		System.out.println("Initial Moves: " + initialMoves + "\n");

		// Step 2: Evaluate and select the most promising move
		String bestMove = chatClient.prompt().user(u -> u.text("""
				Analyze these opening moves and select the strongest one:
				{moves}

				Explain your reasoning step by step, considering:
				1. Position control
				2. Development potential
				3. Long-term strategic advantage

				Then select the single best move.
				""").param("moves", initialMoves)).call().content();

		System.out.println("Best Move: " + bestMove + "\n");

		// Step 3: Explore future game states from the best move
		String gameProjection = chatClient.prompt().user(u -> u.text("""
				Based on this selected opening move:
				{best_move}

				Project the next 3 moves for both players. For each potential branch:
				1. Describe the move and counter-move
				2. Evaluate the resulting position
				3. Identify the most promising continuation

				Finally, determine the most advantageous sequence of moves.
				""").param("best_move", bestMove)).call().content();

		System.out.println("Game Projection: " + gameProjection + "\n");
	}

	// Implementation of Section 2.7: Tree of Thoughts (ToT) - Problem solving
	public void pt_tree_of_thoughts_problem(ChatClient chatClient) {
		String problem = "Design a system to recommend movies to users based on their viewing history.";

		// Step 1: Generate multiple solution approaches
		String approaches = chatClient.prompt().user(u -> u.text("""
				Problem: {problem}

				Generate 3 different approaches to solve this problem:
				1. A content-based filtering approach
				2. A collaborative filtering approach
				3. A hybrid approach

				For each approach, describe:
				- The core algorithm/technique
				- Key data requirements
				- Potential advantages and limitations
				""").param("problem", problem)).call().content();

		System.out.println("Approaches: " + approaches + "\n");

		// Step 2: Evaluate approaches and select the most promising
		String bestApproach = chatClient.prompt().user(u -> u.text("""
				Evaluate these solution approaches:
				{approaches}

				Compare them based on:
				1. Scalability
				2. Accuracy
				3. Implementation complexity
				4. Cold-start handling

				Select the most promising approach with detailed reasoning.
				""").param("approaches", approaches)).call().content();

		System.out.println("Best Approach: " + bestApproach + "\n");

		// Step 3: Refine and elaborate on the selected approach
		String refinedSolution = chatClient.prompt().user(u -> u.text("""
				Based on the selected approach:
				{best_approach}

				Develop a detailed implementation plan:
				1. System architecture
				2. Data processing pipeline
				3. Algorithm implementation details
				4. Evaluation methodology

				Address any limitations identified earlier and propose solutions.
				""").param("best_approach", bestApproach)).call().content();

		System.out.println("Refined Solution: " + refinedSolution + "\n");
	}
	// 2.8 Automatic Prompt Engineering

	public void pt_automatic_prompt_engineering(ChatClient chatClient) {

		String orderVariants = chatClient.prompt("""
				We have a band merchandise t-shirt webshop, and to train a
				chatbot we need various ways to order: "One Metallica t-shirt
				size S". Generate 10 variants, with the same semantics but keep
				the same meaning.
				""").options(ChatOptions.builder().temperature(1.0).topK(40).maxTokens(1024).build()).call().content();

		System.out.println("Variants: " + orderVariants + "\n");

		String output = chatClient.prompt()
			.user(u -> u.text("""
					Please perform BLEU (Bilingual Evaluation Understudy) evaluation on the following variants:
					----
					{variants}
					----

					Select the instruction candidate with the highest evaluation score.
					""").param("variants", orderVariants))
			.options(ChatOptions.builder().temperature(1.0).topK(40).maxTokens(1024).build())
			.call()
			.content();

		System.out.println("Output: " + output + "\n");

	}

	// 2.9.1 Prompts for writing code
	public void pt_code_prompting_writing_code(ChatClient chatClient) {

		// Goal: Write a prompt to write code in Bash to rename files in a folder.

		String output = chatClient.prompt("""
				Write a code snippet in Bash, which asks for a folder name.
				Then it takes the contents of the folder and renames all the
				files inside by prepending the name draft to the file name.
				""").options(ChatOptions.builder().temperature(0.1).maxTokens(1024).build()).call().content();

		System.out.println("Output: " + output + "\n");

	}

	// 2.9.2 Prompts for explaining code
	public void pt_code_prompting_explaining_code(ChatClient chatClient) {

		// Goal: Write a prompt to explain Bash code.

		String code = """
				#!/bin/bash
				echo "Enter the folder name: "
				read folder_name
				if [ ! -d "$folder_name" ]; then
				echo "Folder does not exist."
				exit 1
				fi
				files=( "$folder_name"/* )
				for file in "${files[@]}"; do
				new_file_name="draft_$(basename "$file")"
				mv "$file" "$new_file_name"
				done
				echo "Files renamed successfully."
				""";

		String output = chatClient.prompt()
			.user(u -> u.text("""
					Explain to me the below Bash code:
					```
					{code}
					```
					""").param("code", code))
			.options(ChatOptions.builder().temperature(0.1).maxTokens(1024).build())
			.call()
			.content();

		System.out.println("Output: " + output + "\n");

	}

	// 2.9.3 Prompts for translating code
	public void pt_code_prompting_translating_code(ChatClient chatClient) {

		// Goal: Write a prompt to translate Bash code to Python

		String bashCode = """
				```bash
				#!/bin/bash
				echo "Enter the folder name: "
				read folder_name
				if [ ! -d "$folder_name" ]; then
				echo "Folder does not exist."
				exit 1
				fi
				files=( "$folder_name"/* )
				for file in "${files[@]}"; do
				new_file_name="draft_$(basename "$file")"
				mv "$file" "$new_file_name"
				done
				echo "Files renamed successfully."
				```
				""";

		String output = chatClient.prompt()
			.user(u -> u.text("""
					Translate the below Bash code to a Python snippet:
					{code}
					""").param("code", bashCode))
			.options(ChatOptions.builder().temperature(0.1).maxTokens(1024).build())
			.call()
			.content();

		System.out.println("Output: " + output + "\n");

	}

}