package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data adapter used internally by the competency repository implementation. */
public interface SpringCompetencyDataRepository extends JpaRepository<CompetencyRecord, Long> {

    /**
     * Finds a competency by its unique code.
     *
     * @param code short code
     * @return the competency, or empty when it does not exist
     */
    Optional<CompetencyRecord> findByCode(String code);
}
