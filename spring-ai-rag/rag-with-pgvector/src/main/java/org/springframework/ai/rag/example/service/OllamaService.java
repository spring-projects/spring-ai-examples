package org.springframework.ai.rag.example.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class OllamaService {
    private final OllamaChatModel chatModel;
    private final VectorStore vectorStore;
    private final OllamaEmbeddingModel embeddingModel;

    public OllamaService(OllamaChatModel chatModel, VectorStore vectorStore, OllamaEmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
    }

    public String callModelOnOllamaToGetRes(String userText) {
        return ChatClient.builder(chatModel).build().prompt().advisors(new QuestionAnswerAdvisor(vectorStore)).user(userText).call().chatResponse().getResult().getOutput().getText();
    }
}
