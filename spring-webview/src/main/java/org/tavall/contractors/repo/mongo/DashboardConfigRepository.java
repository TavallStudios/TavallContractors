package org.tavall.contractors.repo.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.tavall.contractors.domain.mongo.DashboardConfig;

import java.util.Optional;

public interface DashboardConfigRepository extends MongoRepository<DashboardConfig, String> {
    Optional<DashboardConfig> findByUserId(Long userId);
}