package com.example.jinferdemo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Plain Spring AI code: nothing here is jinfer-specific. The starter auto-configures the
 * {@code ChatModel} (a local LLM running inside this JVM) behind the injected {@link ChatClient.Builder}.
 */
@RestController
class ChatController {

    record Incident(String severity, String summary, boolean pageOnCall) {}

    private final ChatClient chat;
    private final ChatClient memoryChat;

    ChatController(ChatClient.Builder builder, SessionService sessionService) {
        this.chat = builder.defaultSystem("You are a concise, helpful assistant.").build();
        // A copy of the client with the session advisors, which store and replay the history
        this.memoryChat = this.chat.mutate().defaultAdvisors(SessionAdvisors.builder(sessionService).build()).build();
    }

    /** Simple blocking call. */
    @GetMapping("/chat")
    String chat(@RequestParam(defaultValue = "Tell me a joke about Java.") String message) {
        return chat.prompt(message).call().content();
    }

    /** Token streaming as server-sent events. */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> stream(@RequestParam(defaultValue = "Write a haiku about rivers.") String message) {
        return chat.prompt(message).stream().content();
    }

    /** Multi-turn conversation: the session advisors store and replay history per conversation id. */
    @GetMapping("/chat/memory")
    String memory(@RequestParam String message, @RequestParam(defaultValue = "default") String conversationId) {
        return memoryChat.prompt(message)
                .advisors(SessionAdvisors.session(conversationId))
                .call()
                .content();
    }

    /** Tool calling: the model decides to call {@link WeatherTools}, Spring AI executes it. */
    @GetMapping("/weather")
    String weather(@RequestParam(defaultValue = "Sofia") String city) {
        return chat.prompt("Use the weather tool to check the current weather in " + city + ".")
                .tools(new WeatherTools())
                .call()
                .content();
    }

    /**
     * Structured output: Spring AI derives a JSON schema from the record and jinfer constrains
     * token sampling to it, so the response always parses.
     */
    @PostMapping(value = "/incident", consumes = MediaType.TEXT_PLAIN_VALUE)
    Incident incident(@RequestBody String report) {
        return chat.prompt("Extract this incident as a JSON object with severity (low, medium or high), "
                        + "summary (one short sentence) and pageOnCall (a boolean). Report: " + report)
                .call()
                .entity(Incident.class, ChatClient.EntityParamSpec::useProviderStructuredOutput);
    }
}
