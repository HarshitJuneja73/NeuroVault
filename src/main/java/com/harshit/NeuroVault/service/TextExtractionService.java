package com.harshit.NeuroVault.service;

import com.harshit.NeuroVault.extractor.TextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class TextExtractionService {
    private final List<TextExtractor> extractors;

    @Autowired
    public TextExtractionService(List<TextExtractor> extractors){
        this.extractors = extractors;
    }

    public String extractText(MultipartFile file) throws IOException {
        for(TextExtractor extractor : extractors){
            if(extractor.supports(file.getContentType())){
                return extractor.extractText(file);
            }
        }
        throw new UnsupportedOperationException("Unsupported File type: "+file.getContentType());
    }
}
