package com.harshit.NeuroVault.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class S3Service {
    @Autowired
    private AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException{
        String fileName = System.currentTimeMillis()+"_"+file.getOriginalFilename();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return s3Client.getUrl(bucketName, fileName).toString();
    }
    public String deleteFile(String s3Url){
        String key = extractKeyFromUrl(s3Url);
        s3Client.deleteObject(bucketName, key);
        return String.format("File with key %s deleted from S3 bucket", key);
    }

    private String extractKeyFromUrl(String fileUrl) {
        // Assuming the format is https://{bucket}.s3.amazonaws.com/{key}
        int index = fileUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length();
        return fileUrl.substring(index);
    }
}
