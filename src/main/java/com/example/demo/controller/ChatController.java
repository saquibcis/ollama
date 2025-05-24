package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ChatMessage;
import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.dto.RAGRequest;

@RestController
public class ChatController {

	private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Autowired
    public ChatController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    /** 
     * Simple Q&A using Ollama (Mistral). 
     * Example: GET /qa?question=Hello
     */
    @GetMapping("/qa")
    public String qa(@RequestParam("question") String question) {
    	System.out.println("Question: "+question);
        return this.chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * Chat endpoint with history. Preserves chat context.
     * Expects JSON body: { "history": [ {"role":"user","content":"..."} , ... ], "prompt": "..." }
     */
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
    	System.out.println("Request: "+request);
        var promptBuilder = this.chatClient.prompt();
        // Replay history (assumes alternating user/assistant roles)
        List<ChatMessage> history = request.getHistory();
        if (history != null) {
            for (ChatMessage msg : history) {
                if ("assistant".equalsIgnoreCase(msg.getRole())) {
                    promptBuilder.system(msg.getContent());
                } else {
                    promptBuilder.user(msg.getContent());
                }
            }
        }
        // Add current user prompt
        promptBuilder.user(request.getPrompt());
        String answer = promptBuilder.call().content();
        return new ChatResponse(answer);
    }

    /**
     * RAG-based chat: retrieves context from Chroma vector DB and asks Ollama.
     * Expects JSON body: { "prompt": "..." }
     * Documents are searched by semantic similarity and appended as context.
     */
    @PostMapping("/rag-chat")
    public ChatResponse ragChat(@RequestBody RAGRequest request) {
    	
    	System.out.println("reuest "+request.toString());
        String prompt = request.getPrompt();
        // Retrieve top 3 similar documents from Chroma
        SearchRequest search = SearchRequest.builder()
                .query(prompt)
                .topK(3)
                .build();
        var results = vectorStore.similaritySearch(search);
        // Concatenate document contents as context
        String context = results.stream()
                .map(doc -> doc.getText())
                .collect(Collectors.joining("\n"));
        // Send context + question to LLM
        String fullPrompt = "Context:\n" + context + "\n\nQuestion: " + prompt;
        String answer = this.chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content();
        return new ChatResponse(answer);
    }
}
