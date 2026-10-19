package com.example.jinferdemo;

import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JinferDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JinferDemoApplication.class, args);
    }

    /** In-memory session store for the chat memory demo; sessions are lost on restart. */
    @Bean
    SessionService sessionService() {
        return DefaultSessionService.builder()
                .sessionRepository(InMemorySessionRepository.builder().build())
                .build();
    }
}
