package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.QuestionMetadataRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data adapter used internally by the question repository to resolve
 * existing metadata records when a question references them.
 */
public interface SpringQuestionMetadataDataRepository extends JpaRepository<QuestionMetadataRecord, Long> {
}
