package com.harshit.NeuroVault.extractor;

import com.harshit.NeuroVault.enums.ContentType;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

@Slf4j
@Component
public class ImageTextExtractor implements TextExtractor{

    @Value("${tesseract.datapath}")
    private String datapath;

    @Override
    public boolean supports(String contentType) {
        return contentType!=null && ContentType.fromMimeType(contentType)==ContentType.IMAGE;
    }

    @Override
    public String extractText(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The provided file is empty.");
        }

        File tempFile = null;
        try {
            // Create a temporary file to save the uploaded content
            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            tempFile = File.createTempFile("ocr-temp-", getFileExtension(originalFilename));

            try (OutputStream os = new FileOutputStream(tempFile)) {
                os.write(file.getBytes());
            }

            // Perform OCR on the temporary file
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(datapath);
            tesseract.setLanguage("eng+hin");
            tesseract.setOcrEngineMode(1);

            return tesseract.doOCR(tempFile);

        } catch (IOException e) {
            throw new RuntimeException("Failed to process the uploaded file.", e);
        } catch (TesseractException e) {
            throw new RuntimeException("Failed to perform OCR on the image.", e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return "." + fileName.substring(lastIndexOf + 1);
    }
}
