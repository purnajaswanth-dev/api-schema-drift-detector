package com.ApiDrift.detector.service;

import com.ApiDrift.detector.model.DriftEvent;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DriftDetectionService {

    public List<DriftEvent> compareSchemas(Long endpointId, Map<String, String> oldSchema, Map<String, String> newSchema) {
        List<DriftEvent> events = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Check for removals and type changes
        for (Map.Entry<String, String> entry : oldSchema.entrySet()) {
            String path = entry.getKey();
            String oldType = entry.getValue();

            if (!newSchema.containsKey(path)) {
                events.add(DriftEvent.builder()
                        .endpointId(endpointId)
                        .detectedAt(now)
                        .changeType("FIELD_REMOVED")
                        .fieldPath(path)
                        .oldValue(oldType)
                        .newValue("absent")
                        .build());
            } else if (!newSchema.get(path).equals(oldType)) {
                events.add(DriftEvent.builder()
                        .endpointId(endpointId)
                        .detectedAt(now)
                        .changeType("TYPE_CHANGED")
                        .fieldPath(path)
                        .oldValue(oldType)
                        .newValue(newSchema.get(path))
                        .build());
            }
        }

        // Check for additions
        for (Map.Entry<String, String> entry : newSchema.entrySet()) {
            String path = entry.getKey();
            String newType = entry.getValue();

            if (!oldSchema.containsKey(path)) {
                events.add(DriftEvent.builder()
                        .endpointId(endpointId)
                        .detectedAt(now)
                        .changeType("FIELD_ADDED")
                        .fieldPath(path)
                        .oldValue("absent")
                        .newValue(newType)
                        .build());
            }
        }

        return events;
    }
}
