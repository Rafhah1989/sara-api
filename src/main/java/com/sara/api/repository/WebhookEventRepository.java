package com.sara.api.repository;

import com.sara.api.model.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    Optional<WebhookEvent> findByRequestId(String requestId);
    boolean existsByRequestId(String requestId);
}
