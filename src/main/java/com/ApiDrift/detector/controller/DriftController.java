package com.ApiDrift.detector.controller;

import com.ApiDrift.detector.model.DriftEvent;
import com.ApiDrift.detector.model.MonitoredEndpoint;
import com.ApiDrift.detector.repository.DriftEventRepository;
import com.ApiDrift.detector.repository.MonitoredEndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drift")
@RequiredArgsConstructor
public class DriftController {

    private final DriftEventRepository driftEventRepository;
    private final MonitoredEndpointRepository endpointRepository;

    @GetMapping("/{endpointId}")
    public List<DriftEvent> getDriftEventsForEndpoint(@PathVariable Long endpointId) {
        return driftEventRepository.findByEndpointIdOrderByDetectedAtDesc(endpointId);
    }

    @GetMapping("/latest")
    public List<Map<String, Object>> getLatestDriftFeed() {
        List<DriftEvent> events = driftEventRepository.findTop20ByOrderByDetectedAtDesc();
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (DriftEvent event : events) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", event.getId());
            map.put("endpointId", event.getEndpointId());
            map.put("detectedAt", event.getDetectedAt());
            map.put("changeType", event.getChangeType());
            map.put("fieldPath", event.getFieldPath());
            map.put("oldValue", event.getOldValue());
            map.put("newValue", event.getNewValue());
            
            MonitoredEndpoint endpoint = endpointRepository.findById(event.getEndpointId()).orElse(null);
            map.put("endpointName", endpoint != null ? endpoint.getName() : "Unknown");
            
            response.add(map);
        }
        
        return response;
    }

    @DeleteMapping("/endpoint/{endpointId}")
    @Transactional
    public ResponseEntity<Void> clearDriftHistory(@PathVariable Long endpointId) {
        driftEventRepository.deleteByEndpointId(endpointId);
        MonitoredEndpoint endpoint = endpointRepository.findById(endpointId).orElse(null);
        if (endpoint != null) {
            endpoint.setStatus("HEALTHY");
            endpointRepository.save(endpoint);
        }
        return ResponseEntity.noContent().build();
    }
}
