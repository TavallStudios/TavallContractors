package org.tavall.contractors.repo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.contractors.domain.jpa.CheckoutRecord;

public interface CheckoutRecordRepository extends JpaRepository<CheckoutRecord, Long> {
}