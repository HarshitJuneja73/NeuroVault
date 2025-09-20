package com.harshit.NeuroVault.repository;

import com.harshit.NeuroVault.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Query("SELECT doc from Document doc WHERE doc.user.username = :username")
    List<Document> findByUser_Username(@Param("username") String username);
}
