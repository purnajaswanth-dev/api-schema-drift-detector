package com.ApiDrift.detector.service;

import com.ApiDrift.detector.exception.ApiCallException;
import com.ApiDrift.detector.model.DriftEvent;
import com.ApiDrift.detector.model.MonitoredEndpoint;
import com.ApiDrift.detector.repository.DriftEventRepository;
import com.ApiDrift.detector.repository.MonitoredEndpointRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiPollingService {

    private final MonitoredEndpointRepository endpointRepository;
    private final DriftEventRepository driftEventRepository;
    private final SchemaExtractorService schemaExtractorService;
    private final DriftDetectionService driftDetectionService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callApi(MonitoredEndpoint endpoint) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (endpoint.getRequestHeaders() != null && !endpoint.getRequestHeaders().isEmpty()) {
                Map<String, String> headerMap = objectMapper.readValue(endpoint.getRequestHeaders(), Map.class);
                headerMap.forEach(headers::add);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            HttpMethod method = HttpMethod.valueOf(endpoint.getHttpMethod().toUpperCase());
            
            ResponseEntity<String> response = restTemplate.exchange(endpoint.getUrl(), method, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call API {}: {}", endpoint.getUrl(), e.getMessage());
            throw new ApiCallException(e.getMessage());
        }
    }

    public void checkEndpoint(MonitoredEndpoint endpoint) {
        try {
            String rawResponse = callApi(endpoint);
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            Map<String, String> currentSchema = schemaExtractorService.extractSchema(rootNode, "");

            if (endpoint.getBaselineSchema() == null) {
                // First time check, set baseline
                endpoint.setBaselineSchema(schemaExtractorService.schemaToJson(currentSchema));
                endpoint.setStatus("HEALTHY");
                endpoint.setLastChecked(LocalDateTime.now());
                endpointRepository.save(endpoint);
                log.info("Baseline captured for endpoint: {}", endpoint.getName());
                return;
            }

            // Compare with baseline
            Map<String, String> oldSchema = schemaExtractorService.jsonToSchema(endpoint.getBaselineSchema());
            List<DriftEvent> driftEvents = driftDetectionService.compareSchemas(endpoint.getId(), oldSchema, currentSchema);

            if (driftEvents.isEmpty()) {
                endpoint.setStatus("HEALTHY");
            } else {
                endpoint.setStatus("DRIFTED");
                String rawOldSchema = endpoint.getBaselineSchema();
                String rawNewSchema = schemaExtractorService.schemaToJson(currentSchema);

                for (DriftEvent event : driftEvents) {
                    event.setRawOldSchema(rawOldSchema);
                    event.setRawNewSchema(rawNewSchema);
                }
                driftEventRepository.saveAll(driftEvents);
                log.warn("Drift detected for endpoint: {}. Events count: {}", endpoint.getName(), driftEvents.size());
            }

            endpoint.setLastChecked(LocalDateTime.now());
            endpointRepository.save(endpoint);

        } catch (Exception e) {
            log.error("Error checking endpoint {}: {}", endpoint.getName(), e.getMessage());
            endpoint.setStatus("ERROR");
            endpoint.setLastChecked(LocalDateTime.now());
            endpointRepository.save(endpoint);
        }
    }

    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void checkAllEndpoints() {
        log.info("Starting scheduled check cycle for all endpoints...");
        List<MonitoredEndpoint> endpoints = endpointRepository.findAll();
        for (MonitoredEndpoint endpoint : endpoints) {
            log.info("Checking endpoint: {}", endpoint.getName());
            checkEndpoint(endpoint);
        }
        log.info("Scheduled check cycle completed.");
    }
}
