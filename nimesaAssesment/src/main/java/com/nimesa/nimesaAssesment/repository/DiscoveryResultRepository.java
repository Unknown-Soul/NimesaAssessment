package com.nimesa.nimesaAssesment.repository;
import com.nimesa.nimesaAssesment.entity.DiscoveryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscoveryResultRepository extends JpaRepository<DiscoveryResult, Long> {
    List<DiscoveryResult> findByServiceName(String serviceName);
    List<DiscoveryResult> findByJobId(String jobId);
}
