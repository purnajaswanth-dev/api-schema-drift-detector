package com.ApiDrift.detector.repository;

import com.ApiDrift.detector.model.DriftEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DriftEventRepository extends JpaRepository<DriftEvent, Long> {
    List<DriftEvent> findByEndpointIdOrderByDetectedAtDesc(Long endpointId);
    
    @Query(value = "SELECT de.* FROM drift_event de ORDER BY de.detected_at DESC LIMIT 20", nativeQuery = true)
    List<DriftEvent> findTop20ByOrderByDetectedAtDesc();

    void deleteByEndpointId(Long endpointId);
}
