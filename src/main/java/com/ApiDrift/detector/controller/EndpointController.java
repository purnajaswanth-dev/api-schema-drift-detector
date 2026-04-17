package com.ApiDrift.detector.controller;

import com.ApiDrift.detector.model.MonitoredEndpoint;
import com.ApiDrift.detector.repository.DriftEventRepository;
import com.ApiDrift.detector.repository.MonitoredEndpointRepository;
import com.ApiDrift.detector.service.ApiPollingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class EndpointController {

    private final MonitoredEndpointRepository endpointRepository;
    private final DriftEventRepository driftEventRepository;
    private final ApiPollingService apiPollingService;

    @PostMapping
    public ResponseEntity<MonitoredEndpoint> createEndpoint(@RequestBody MonitoredEndpoint endpoint) {
        if (endpoint.getHttpMethod() == null) endpoint.setHttpMethod("GET");
        MonitoredEndpoint saved = endpointRepository.save(endpoint);
        apiPollingService.checkEndpoint(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<MonitoredEndpoint> getAllEndpoints() {
        return endpointRepository.findAllByOrderByCreatedAtDesc();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteEndpoint(@PathVariable Long id) {
        if (!endpointRepository.existsById(id)) {
            throw new EntityNotFoundException("Endpoint not found with id: " + id);
        }
        driftEventRepository.deleteByEndpointId(id);
        endpointRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/check")
    public MonitoredEndpoint manuallyCheck(@PathVariable Long id) {
        MonitoredEndpoint endpoint = endpointRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endpoint not found with id: " + id));
        apiPollingService.checkEndpoint(endpoint);
        return endpointRepository.findById(id).get();
    }
}
