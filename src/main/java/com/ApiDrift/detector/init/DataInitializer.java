package com.ApiDrift.detector.init;

import com.ApiDrift.detector.model.MonitoredEndpoint;
import com.ApiDrift.detector.repository.MonitoredEndpointRepository;
import com.ApiDrift.detector.service.ApiPollingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final MonitoredEndpointRepository endpointRepository;
    private final ApiPollingService apiPollingService;

    @Override
    public void run(ApplicationArguments args) {
        if (endpointRepository.count() == 0) {
            log.info("Initializing seed data for monitored endpoints...");
            
            List<MonitoredEndpoint> initialEndpoints = Arrays.asList(
                MonitoredEndpoint.builder()
                    .name("JSONPlaceholder Todo")
                    .url("https://jsonplaceholder.typicode.com/todos/1")
                    .httpMethod("GET")
                    .build(),
                MonitoredEndpoint.builder()
                    .name("JSONPlaceholder User")
                    .url("https://jsonplaceholder.typicode.com/users/1")
                    .httpMethod("GET")
                    .build(),
                MonitoredEndpoint.builder()
                    .name("GitHub Zen")
                    .url("https://api.github.com/zen")
                    .httpMethod("GET")
                    .requestHeaders("{\"User-Agent\":\"API-Schema-Drift-Detector\"}")
                    .build()
            );

            for (MonitoredEndpoint endpoint : initialEndpoints) {
                MonitoredEndpoint saved = endpointRepository.save(endpoint);
                log.info("Registered endpoint: {}. Capturing baseline...", saved.getName());
                try {
                    apiPollingService.checkEndpoint(saved);
                } catch (Exception e) {
                    log.error("Failed to capture baseline for {}: {}", saved.getName(), e.getMessage());
                }
            }
            log.info("Seed data initialization complete.");
        }
    }
}
