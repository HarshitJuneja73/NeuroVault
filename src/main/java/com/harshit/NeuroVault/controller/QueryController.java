package com.harshit.NeuroVault.controller;

import com.harshit.NeuroVault.model.ChatRequest;
import com.harshit.NeuroVault.model.Document;
import com.harshit.NeuroVault.model.User;
import com.harshit.NeuroVault.service.ChatService;
import com.harshit.NeuroVault.service.StorageService;
import com.harshit.NeuroVault.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("api/query")
public class QueryController {
    private ChatClient chatClient;
    @Autowired
    private UserService userService;

    private VectorStore vectorStore;

    @Autowired
    private ChatService chatService;

    @Autowired
    public QueryController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }

//    user gives question and document ID(s). Gets a string in response

    @PostMapping("chat")
    public ResponseEntity<String> chatWithDocs(@RequestBody ChatRequest request, @AuthenticationPrincipal UserDetails userDetails){
        String result = chatService.chatWithDocs(request, userDetails.getUsername());
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No documents found for user.");
        }
        return ResponseEntity.ok(result);

    }
}
