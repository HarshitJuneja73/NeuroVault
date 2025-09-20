package com.harshit.NeuroVault.extractor;

import com.harshit.NeuroVault.enums.ContentType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface TextExtractor {
    boolean supports(String contentType);
    String extractText(MultipartFile file) throws IOException;
}
