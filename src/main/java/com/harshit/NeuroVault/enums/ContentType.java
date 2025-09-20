package com.harshit.NeuroVault.enums;

import java.util.Set;

public enum ContentType {
    PDF("application/pdf"),
    IMAGE("image/jpeg", "image/jpg", "image/png"),
    TEXT("text/plain"),
    DEFAULT();

    private final Set<String> mimeTypes;

    ContentType(String... mimeTypes) {
        this.mimeTypes = Set.of(mimeTypes);
    }

    public static ContentType fromMimeType(String mimeType) {
        if (mimeType == null) return DEFAULT;

        for (ContentType type : values()) {
            if (type.mimeTypes.contains(mimeType)) {
                return type;
            }
        }
        return DEFAULT;
    }
}

