package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Quiz;

import java.util.List;
import java.util.Optional;

/** Technology-independent persistence contract for quizzes. */
public interface QuizRepository {

    /**
     * Stores a quiz and its question associations.
     *
     * @param quiz quiz to store
     * @return stored quiz with its persistent identity
     */
    Quiz save(Quiz quiz);

    /**
     * Finds a quiz by identity.
     *
     * @param id persistent identity
     * @return the quiz, or empty when it does not exist
     */
    Optional<Quiz> findById(Long id);

    /** @return all registered quizzes */
    List<Quiz> findAll();
}
