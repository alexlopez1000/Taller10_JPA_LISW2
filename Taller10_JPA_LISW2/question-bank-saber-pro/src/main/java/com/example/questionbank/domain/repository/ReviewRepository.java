package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Review;

import java.util.List;
import java.util.Optional;

/** Technology-independent persistence contract for review entries. */
public interface ReviewRepository {

    /**
     * Stores a review entry.
     *
     * @param review review to store
     * @return stored review with its persistent identity
     */
    Review save(Review review);

    /**
     * Finds a review entry by identity.
     *
     * @param id persistent identity
     * @return the review, or empty when it does not exist
     */
    Optional<Review> findById(Long id);

    /**
     * Lists the review history of a question.
     *
     * @param questionId identity of the question
     * @return reviews recorded for that question
     */
    List<Review> findByQuestionId(Long questionId);
}
