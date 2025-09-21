package com.harshit.NeuroVault.service;

import com.harshit.NeuroVault.enums.ContentType;
import com.harshit.NeuroVault.model.Document;
import com.harshit.NeuroVault.model.User;
import com.harshit.NeuroVault.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class StorageService {
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private TextExtractionService textExtractionService;
    @Autowired
    private EmbeddingModel embeddingModel;


    public ResponseEntity<Document> saveFileToDBandS3(MultipartFile file, User user) throws IOException {
        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setContentType(fileContentType(file.getContentType()));
        document.setFileSize(file.getSize());

//        s3 upload
        String s3url;

        try {
            s3url = s3Service.uploadFile(file);
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        document.setS3Url(s3url);
        document.setUser(user);

        Document savedDoc = null;
        try {
            savedDoc = documentRepository.save(document);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(savedDoc, HttpStatus.OK);
    }

    private ContentType fileContentType(String contentType) {
        return ContentType.fromMimeType(contentType);
    }

    @Transactional
    public ResponseEntity<List<Document>> viewAllDocuments(String username) {
        List<Document> userDocuments = documentRepository.findByUser_Username(username);
        return new ResponseEntity<>(userDocuments, HttpStatus.OK);
    }

    /**
     * Returns a ResponseEntity with a body on success; failures return a ResponseEntity
     * with no body and an appropriate error status.
     */
    public ResponseEntity<String> saveFileAndStoreText(MultipartFile file, User user) throws IOException {
        String extractedText = textExtractionService.extractText(file);
        String cleanText = extractedText
                .replaceAll("[ \\t]+", " ")
                // collapse 3+ newlines into 2 (keep paragraph breaks but remove page padding)
                .replaceAll("\\n{3,}", "\n\n")
                // trim leading/trailing spaces
                .trim();


        ResponseEntity<Document> saveOutput = saveFileToDBandS3(file, user);

        if (!saveOutput.hasBody()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        org.springframework.ai.document.Document ragDoc = getRagDocument(saveOutput, cleanText);

        // splitting text into chunks
        TokenTextSplitter splitter = new TokenTextSplitter();

        List<org.springframework.ai.document.Document> chunks = splitter.split(ragDoc);

        try {
            vectorStore.add(chunks);
        } catch (Exception e) {
            if (saveOutput.getBody() != null) {
                deleteFile(saveOutput.getBody().getId(), user);
            }
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return new ResponseEntity<>("Upload complete", saveOutput.getStatusCode());
    }

    private static org.springframework.ai.document.Document getRagDocument(ResponseEntity<Document> saveOutput, String extractedText) {
        Document savedDocument = saveOutput.getBody();
        assert savedDocument != null;
        org.springframework.ai.document.Document ragDoc = new org.springframework.ai.document.Document(
                savedDocument.getId().toString(),
                extractedText,
                Map.of(
                        "dbId", savedDocument.getId(),
                        "userId", savedDocument.getUser().getId(),
                        "fileName", savedDocument.getFileName(),
                        "uploadedAt", savedDocument.getUploadedAt().toString()
                )
        );
        return ragDoc;
    }

    public String deleteFile(Long id, User user) {
        Optional<Document> docToDelete = findDocById(id, user);
        if (docToDelete.isEmpty()) return "File not found";
        Document document = docToDelete.get();
        String s3Url = document.getS3Url();
        if (!document.getUser().getId().equals(user.getId())) {
            return "File not found";
        }
        // deleting from S3
        log.info(s3Service.deleteFile(s3Url));
        String filterForDeletion = String.format("dbId == %d", id);
        // deleting from vectorStore
        vectorStore.delete(filterForDeletion);
        // deleting from DB
        documentRepository.deleteById(id);
        return String.format("Deletion Successful for id %d", id);
    }


    public Optional<Document> findDocById(Long id, User user) {

        Optional<Document> doc = documentRepository.findById(id);
        if (doc.isPresent()) {
            if (!doc.get().getUser().getId().equals(user.getId())) {
                return Optional.empty();
            }
        }
        return doc;

    }
}
