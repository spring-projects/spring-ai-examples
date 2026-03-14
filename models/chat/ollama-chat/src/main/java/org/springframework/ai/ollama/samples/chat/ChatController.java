package org.springframework.ai.ollama.samples.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/ai/chat")
    public String chat(@RequestParam(name = "prompt",defaultValue = "how can you help me?") String message) {
        return chatClient.prompt(message).call().content();
    }
}
