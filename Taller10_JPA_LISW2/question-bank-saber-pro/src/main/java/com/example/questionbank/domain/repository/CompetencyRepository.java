package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Competency;

import java.util.List;
import java.util.Optional;

/** Technology-independent persistence contract for academic competencies. */
public interface CompetencyRepository {

    /**
     * Stores a competency.
     *
     * @param competency competency to store
     * @return stored competency with its persistent identity
     */
    Competency save(Competency competency);

    /**
     * Finds a competency by identity.
     *
     * @param id persistent identity
     * @return the competency, or empty when it does not exist
     */
    Optional<Competency> findById(Long id);

    /** @return all registered competencies */
    List<Competency> findAll();

    /**
     * Finds a competency by its unique code.
     *
     * @param code short code of the competency
     * @return the competency, or empty when it does not exist
     */
    Optional<Competency> findByCode(String code);
}
