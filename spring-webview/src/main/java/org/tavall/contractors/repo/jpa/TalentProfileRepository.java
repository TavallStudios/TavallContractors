package org.tavall.contractors.repo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.tavall.contractors.domain.jpa.TalentProfile;

import java.util.List;
import java.util.Optional;

public interface TalentProfileRepository extends JpaRepository<TalentProfile, Long> {

    @Query("""
        select tp from TalentProfile tp
        join fetch tp.user u
        where u.primaryRole = org.tavall.contractors.domain.jpa.UserRole.TALENT
    """)
    List<TalentProfile> findAllTalentProfiles();

    Optional<TalentProfile> findByUserId(Long userId);
}