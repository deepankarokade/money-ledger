package com.deepankar.ledger_system.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity 
@Table (
    name = "idempotency_records",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "idempotency_key")
    }
)
@Data 
public class IdempotencyRecord {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

/*
| Field            | Purpose                                   |
| ---------------- | ----------------------------------------- |
| `id`             | Database ID                               |
| `idempotencyKey` | Unique identifier supplied by client      |
| `requestHash`    | Proves which request that key belonged to |
| `responseStatus` | Original HTTP status                      |
| `responseBody`   | Original response                         |
| `createdAt`      | When the request was processed            |
*/
