package com.ApiDrift.detector.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriftEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long endpointId;
    private LocalDateTime detectedAt;
    
    private String changeType; // FIELD_ADDED, FIELD_REMOVED, TYPE_CHANGED, FIELD_MOVED
    private String fieldPath;
    private String oldValue;
    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String rawOldSchema;

    @Column(columnDefinition = "TEXT")
    private String rawNewSchema;
}
