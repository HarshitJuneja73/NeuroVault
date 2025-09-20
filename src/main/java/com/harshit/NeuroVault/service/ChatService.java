package com.harshit.NeuroVault.service;

import com.harshit.NeuroVault.model.ChatRequest;
import com.harshit.NeuroVault.model.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    private static final int AVG_DOCUMENT_CHUNKS = 10;
    private final ChatClient chatClient;
    private final UserService userService;
    private final VectorStore vectorStore;
    private final StorageService storageService;

    @Autowired
    public ChatService(ChatClient.Builder chatClientBuilder, UserService userService,
                       VectorStore vectorStore, StorageService storageService) {
        this.userService = userService;
        this.vectorStore = vectorStore;
        this.storageService = storageService;
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }

    public String chatWithDocs(ChatRequest request, String username) {
        Long userId = userService.getUserByUsername(username).getId();
        List<Long> documentIDs = request.getDocumentIDs();

        if (documentIDs == null || documentIDs.isEmpty()) {
            List<Document> docsForUser = storageService.viewAllDocuments(username).getBody();
            if (docsForUser == null || docsForUser.isEmpty()) {
                return null;
            }
            documentIDs = docsForUser.stream().map(Document::getId).toList();
        }

        String filter = String.format("userId == %d && dbId IN %s", userId, documentIDs.toString());

        return chatClient.prompt()
                .user(request.getQuery())
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .filterExpression(filter)
                                .topK(AVG_DOCUMENT_CHUNKS * documentIDs.size())
                                .build())
                        .build())
                .call()
                .content();
    }
}