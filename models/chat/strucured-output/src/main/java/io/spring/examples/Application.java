package io.spring.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner cli(ChatClient.Builder builder) {
		return args -> {
			var chat = builder.build();
			var scanner = new Scanner(System.in);
			var mapper = new ObjectMapper();

			while (true) {
				System.out.println("\nWho is your favorite technical author? (e.g., 'Rod Johnson', or type 'exit' to quit)");
				System.out.print("USER: ");
				String authorName = scanner.nextLine();

				if (authorName.equalsIgnoreCase("exit")) {
					System.out.println("Goodbye!");
					break;
				}

				var authorInfo = chat.prompt()
						.user(u -> {
							u.text("Please give me some information about my favorite technical author ${author}");
							u.param("author", authorName);
						})
						.call()
						.entity(AuthorInfo.class);

				log.info("\n=== Author Analysis ===");
				log.info("Name: {}", authorInfo.name());
				log.info("Primary Genre: {}", authorInfo.primaryGenre());
				log.info("Notable Books: {}", String.join(", ", authorInfo.notableBooks()));
				log.info("Years Active: {}", authorInfo.yearsActive());
				log.info("Specialization: {}", authorInfo.specialization());
			}
		};
	}
}