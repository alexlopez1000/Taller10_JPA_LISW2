package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.QuizRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data adapter used internally by the quiz repository implementation. */
public interface SpringQuizDataRepository extends JpaRepository<QuizRecord, Long> {
}
