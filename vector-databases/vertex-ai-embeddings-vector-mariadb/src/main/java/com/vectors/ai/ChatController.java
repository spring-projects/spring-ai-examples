package com.vectors.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import com.google.cloud.vertexai.VertexAI;

import jakarta.annotation.PostConstruct;



@RestController
public class ChatController {
   
@Value("${spring.ai.vertex.ai.gemini.project-id}")
private String projectId;
@Value("${spring.ai.vertex.ai.gemini.location}")
private String location;
@Value("${spring.ai.model}")
private String model;

private VertexAiGeminiChatOptions options;
private VertexAiGeminiChatModel chatModel;

private List<Message> conversationHistory = new ArrayList<>();
@Autowired
private RagService ragService;

   @PostConstruct
   public void init() {
    
    ToolCallback[] getUserFunction = ToolCallbacks
        .from(new GetUserFunction());
    

    options = VertexAiGeminiChatOptions.builder()
        .model(model)
        .temperature(0.4)
        // tool call backs are not compatible with google search
//        .googleSearchRetrieval(true)
        .toolCallbacks(getUserFunction)
    .build();

    chatModel = VertexAiGeminiChatModel.builder()
 		.vertexAI(new VertexAI(projectId, location))
 		.defaultOptions(options)
 		.build();
     
    }

    @RequestMapping(path="/embeddingsMessage", method=RequestMethod.GET)
    public String requestEmbeddingsMessage(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        
        List<Document> revelantDocs = ragService.getEmbeddings(message);

        StringBuilder contextBuilder = new StringBuilder();
        if (revelantDocs != null && !revelantDocs.isEmpty()) {
            contextBuilder.append("Use the following information to answer the question:\n\n");
            revelantDocs.stream().forEach(doc -> {
            contextBuilder.append("Context Document ").append(":\n");
                contextBuilder.append(doc.getFormattedContent());
                contextBuilder.append("\n\n");
            }); 
        }
        // Adding the prompt to the history
        conversationHistory.add(new UserMessage(message));
        // Adding context from revelant files to the History
        conversationHistory.add(new UserMessage(contextBuilder.toString()));
        // Preparing the prompt with the augmented data
        String augmentedQuery = contextBuilder.toString() + "User Question: " + message;
        Prompt prompt = new Prompt(Collections.singletonList(new UserMessage(augmentedQuery)));
        // Calling the model
        ChatResponse response = this.chatModel.call(prompt);
        // Adding the model response to the history
        conversationHistory.add(new UserMessage(response.getResult().getOutput().getText()));

        return response.getResult().getOutput().getText();
    }

    @RequestMapping(path="/functionMessage", method=RequestMethod.GET)
    public String requestFunctionMessage(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        
        // Adding the prompt to the history
        conversationHistory.add(new UserMessage(message));
        Prompt prompt = new Prompt(conversationHistory);
        // Calling the model
        ChatResponse response = this.chatModel.call(prompt);
        // Adding the model response to the history
        conversationHistory.add(new UserMessage(response.getResult().getOutput().getText()));

        return response.getResult().getOutput().getText();
    }
}