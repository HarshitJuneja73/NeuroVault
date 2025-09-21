package com.harshit.NeuroVault.controller;

import com.harshit.NeuroVault.model.Document;
import com.harshit.NeuroVault.model.User;
import com.harshit.NeuroVault.service.StorageService;
import com.harshit.NeuroVault.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("api/documents")
public class DocumentController {
    @Autowired
    private StorageService storageService;
    @Autowired
    private UserService userService;

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload documents",
            description = "Uploads one or more files for the authenticated user. Returns status and messages for each file."
    )
    public ResponseEntity<List<String>> upload(
            @Parameter(description = "Files to upload")
            @RequestPart("file") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        List<String> results = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return new ResponseEntity<>(List.of("No files provided"), HttpStatus.BAD_REQUEST);
        }

        String username = userDetails.getUsername();
        User user = userService.getUserByUsername(username);
        int successCount = 0;

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                results.add("File is empty: " + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"));
            } else {
                ResponseEntity<String> response = storageService.saveFileAndStoreText(file, user);
                if (response.hasBody()) {
                    successCount++;
                    results.add(response.getBody());
                } else {
                    results.add("Failed to upload file: " +
                            (file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"));
                }
            }
        }
        HttpStatus status;
        if (successCount == files.size()) {
            status = HttpStatus.OK;
        } else if (successCount > 0) {
            status = HttpStatus.MULTI_STATUS;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(results, status);
    }


    @GetMapping("/")
    @Operation(
            summary = "View all documents",
            description = "Retrieves all documents uploaded by the authenticated user."
    )
    public ResponseEntity<List<Document>> viewAllDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return storageService.viewAllDocuments(username);

    }

    @DeleteMapping("/")
    @Operation(
            summary = "Delete documents",
            description = "Deletes documents by IDs for the authenticated user from storage, database, and vector store."
    )
    public ResponseEntity<List<String>> delete(@RequestBody List<Long> IdsToDelete, @AuthenticationPrincipal UserDetails userDetails) {
//        we have to delete from S3, DB and vector_store
        List<String> result = new ArrayList<>(IdsToDelete.size());
        String userName = userDetails.getUsername();
        User user = userService.getUserByUsername(userName);
        for (Long id : IdsToDelete) {
            result.add(storageService.deleteFile(id, user));
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("getByID")
    @Operation(
            summary = "Get documents by IDs",
            description = "Fetches documents by their IDs for the authenticated user."
    )
    public ResponseEntity<List<Document>> getDocumentById(@RequestBody List<Long> documentIDs, @AuthenticationPrincipal UserDetails userDetails) {
        List<Document> result = new ArrayList<>(documentIDs.size());
        String userName = userDetails.getUsername();
        User user = userService.getUserByUsername(userName);
        for (Long id : documentIDs) {
            result.add(storageService.findDocById(id, user).orElse(null));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
