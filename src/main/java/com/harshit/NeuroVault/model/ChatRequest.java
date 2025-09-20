package com.harshit.NeuroVault.model;

import java.util.List;

public class ChatRequest {
    private String query;
    private List<Long> documentIDs;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<Long> getDocumentIDs() {
        return documentIDs;
    }

    public void setDocumentIDs(List<Long> documentIDs) {
        this.documentIDs = documentIDs;
    }
}
