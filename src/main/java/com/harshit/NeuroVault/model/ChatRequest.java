package com.harshit.NeuroVault.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class ChatRequest {
    private String query;
    private List<Long> documentIDs;
    @NotNull(message = "Conversation ID cannot be null")
    private String conversationId;

}
