package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data adapter used internally by the review repository implementation. */
public interface SpringReviewDataRepository extends JpaRepository<ReviewRecord, Long> {

    /**
     * Lists the review history of a question.
     *
     * @param questionId identity of the question
     * @return reviews recorded for that question
     */
    List<ReviewRecord> findByQuestionId(Long questionId);
}
