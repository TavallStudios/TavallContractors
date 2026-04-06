package org.tavall.contractors.repo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.contractors.domain.jpa.UserAccount;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
}