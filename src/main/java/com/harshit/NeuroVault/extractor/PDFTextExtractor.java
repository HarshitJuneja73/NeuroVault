package com.harshit.NeuroVault.extractor;

import com.harshit.NeuroVault.enums.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
public class PDFTextExtractor implements TextExtractor {
    @Override
    public boolean supports(String contentType) {
        return contentType!=null && ContentType.fromMimeType(contentType)==ContentType.PDF;
    }

    @Override
    public String extractText(MultipartFile file) throws IOException {
        if(file == null || file.isEmpty()){
            return "";
        }
        if(!supports(file.getContentType())){
            throw new IllegalArgumentException("File type not supported: " + file.getContentType());
        }
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            return text;

        } catch (IOException e) {
            log.error(e.getMessage());
            return "Error while processing the PDF file.";
        }
    }
}
