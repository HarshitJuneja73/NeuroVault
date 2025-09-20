package com.harshit.NeuroVault.model;

import com.harshit.NeuroVault.enums.ContentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    @Enumerated(EnumType.STRING)
    private ContentType contentType;
    private long fileSize;
    private String s3Url;

    @CreationTimestamp
    private Instant uploadedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
