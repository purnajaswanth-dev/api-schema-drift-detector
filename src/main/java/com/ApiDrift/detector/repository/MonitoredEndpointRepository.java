package com.ApiDrift.detector.repository;

import com.ApiDrift.detector.model.MonitoredEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MonitoredEndpointRepository extends JpaRepository<MonitoredEndpoint, Long> {
    List<MonitoredEndpoint> findByStatus(String status);
    List<MonitoredEndpoint> findAllByOrderByCreatedAtDesc();
}
