package com.harshit.NeuroVault.extractor;

import com.harshit.NeuroVault.enums.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TextFileTextExtractor implements TextExtractor{
    @Override
    public boolean supports(String contentType) {
        return contentType!=null && ContentType.fromMimeType(contentType)==ContentType.TEXT;
    }

    @Override
    public String extractText(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
