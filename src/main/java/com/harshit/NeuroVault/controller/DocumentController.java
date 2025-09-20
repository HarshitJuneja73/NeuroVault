package com.harshit.NeuroVault.controller;

import com.harshit.NeuroVault.model.Document;
import com.harshit.NeuroVault.model.User;
import com.harshit.NeuroVault.service.StorageService;
import com.harshit.NeuroVault.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Controller
@RequestMapping("api/documents")
public class DocumentController {
    @Autowired
    private StorageService storageService;
    @Autowired
    private UserService userService;

    @PostMapping("/")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file,
                                         @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        if(file.isEmpty()){
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }
        String username = userDetails.getUsername();
        User user = userService.getUserByUsername(username);
        return storageService.saveFileAndStoreText(file, user);
    }

    @GetMapping("/")
    public ResponseEntity<List<Document>> viewAllDocuments(@AuthenticationPrincipal UserDetails userDetails){
        String username = userDetails.getUsername();
        return storageService.viewAllDocuments(username);

    }

    @DeleteMapping("/")
    public ResponseEntity<List<String>> delete(@RequestBody List<Long> IdsToDelete, @AuthenticationPrincipal UserDetails userDetails){
//        we have to delete from S3, DB and vector_store
        List<String> result = new ArrayList<>(IdsToDelete.size());
        String userName = userDetails.getUsername();
        User user = userService.getUserByUsername(userName);
        for(Long id : IdsToDelete){
            result.add(storageService.deleteFile(id,user));
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("getByID")
    public ResponseEntity<List<Document>> getDocumentById(@RequestBody List<Long> documentIDs, @AuthenticationPrincipal UserDetails userDetails){
        List<Document> result = new ArrayList<>(documentIDs.size());
        String userName = userDetails.getUsername();
        User user = userService.getUserByUsername(userName);
        for(Long id : documentIDs){
            result.add(storageService.findDocById(id,user).orElse(null));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
