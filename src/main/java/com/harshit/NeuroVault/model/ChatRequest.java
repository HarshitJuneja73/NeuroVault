package com.harshit.NeuroVault.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
public class ChatRequest {
    @Schema(
            description = "User's query to be processed",
            example = "What is the summary of document 123?"
    )
    private String query;
    @Schema(
            description = "List of document IDs to search within",
            example = "[123, 456]"
    )
    private List<Long> documentIDs;
    @NotNull(message = "Conversation ID cannot be null")
    @Pattern(
            regexp = "^\\d+_.+$",
            message = "Conversation ID must start with user id, followed by an underscore, and at least one character (e.g., 1_1)"
    )
    @Schema(
            description = "Unique identifier for the conversation. Must start with user id, followed by an underscore, and at least one character.",
            example = "1_1"
    )
    private String conversationId;

}
