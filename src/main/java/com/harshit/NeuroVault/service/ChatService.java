package com.harshit.NeuroVault.service;

import com.harshit.NeuroVault.model.ChatRequest;
import com.harshit.NeuroVault.model.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {
    private static final int AVG_DOCUMENT_CHUNKS = 10;
    private static final int MAX_TOP_K = 100;
    private static final int MAX_MESSAGES_IN_MEMORY = 15;
    private final ChatClient chatClient;
    private final UserService userService;
    private final VectorStore vectorStore;
    private final StorageService storageService;

    private Map<String, ChatMemory> chatMemories;

    @Autowired
    public ChatService(ChatClient.Builder chatClientBuilder, UserService userService,
                       VectorStore vectorStore, StorageService storageService) {
        this.userService = userService;
        this.vectorStore = vectorStore;
        this.storageService = storageService;
        this.chatClient = chatClientBuilder.build();
        this.chatMemories = new ConcurrentHashMap<>();
    }

    public String chatWithDocs(ChatRequest request, String username) {
        Long userId = userService.getUserByUsername(username).getId();
        List<Long> documentIDs = request.getDocumentIDs();

        if (documentIDs == null || documentIDs.isEmpty()) {
            List<Document> docsForUser = storageService.viewAllDocuments(username).getBody();
            if (docsForUser == null || docsForUser.isEmpty()) {
                return "No documents found";
            }
            documentIDs = docsForUser.stream().map(Document::getId).toList();
        }

        String filter = String.format("userId == %d && dbId IN %s", userId, documentIDs.toString());


        ChatMemory chatMemory = chatMemories.get(request.getConversationId());
        if(chatMemory == null){
            chatMemory = MessageWindowChatMemory.builder()
                    .maxMessages(MAX_MESSAGES_IN_MEMORY)
//                    TODO: make this JDBC memory repository instead of in memory.
                    .chatMemoryRepository(new InMemoryChatMemoryRepository())
                    .build();
            chatMemories.put(request.getConversationId(), chatMemory);
        }

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .filterExpression(filter)
                        .topK(Math.min(AVG_DOCUMENT_CHUNKS * documentIDs.size(), MAX_TOP_K))
                        .build())
                .build();


        return chatClient.prompt()
                .user(request.getQuery())
                .advisors(messageChatMemoryAdvisor, questionAnswerAdvisor)
                .call()
                .content();
    }
}