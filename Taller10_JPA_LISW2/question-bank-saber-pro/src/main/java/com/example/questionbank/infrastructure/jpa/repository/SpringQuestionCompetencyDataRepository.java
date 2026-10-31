package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.QuestionCompetencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data adapter used internally by the question repository to manage the
 * rows of the {@code question_competencies} associative table explicitly.
 * Deleting and inserting association rows through this adapter keeps the
 * synchronization deterministic, independent of collection flush ordering.
 */
public interface SpringQuestionCompetencyDataRepository extends JpaRepository<QuestionCompetencyRecord, Long> {
}
