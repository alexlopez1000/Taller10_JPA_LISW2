package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.Review;
import com.example.questionbank.domain.repository.ReviewRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.ReviewRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringReviewDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Concrete JPA implementation of the domain review contract. */
@Repository
public class ReviewRepositoryJpa implements ReviewRepository {

    private final SpringReviewDataRepository reviews;
    private final SpringQuestionDataRepository questions;
    private final SpringUserDataRepository users;

    /**
     * Creates the adapter.
     *
     * @param reviews   Spring Data repository for review records
     * @param questions Spring Data repository for question resolution
     * @param users     Spring Data repository for reviewer resolution
     */
    public ReviewRepositoryJpa(SpringReviewDataRepository reviews, SpringQuestionDataRepository questions,
                               SpringUserDataRepository users) {
        this.reviews = reviews;
        this.questions = questions;
        this.users = users;
    }

    @Override
    @Transactional
    public Review save(Review review) {
        QuestionRecord questionRecord = questions.findById(review.getQuestion().getId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Question not found: " + review.getQuestion().getId()));
        UserRecord reviewerRecord = users.findById(review.getReviewer().getId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Reviewer not found: " + review.getReviewer().getId()));
        ReviewRecord record = new ReviewRecord(reviewerRecord, review.getDecision(), review.getComments(),
            review.getReviewedAt() != null ? review.getReviewedAt() : LocalDateTime.now());
        record.setQuestion(questionRecord);
        ReviewRecord saved = reviews.save(record);
        review.setId(saved.getId());
        return review;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findById(Long id) {
        return reviews.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findByQuestionId(Long questionId) {
        return reviews.findByQuestionId(questionId).stream().map(DomainMapper::toDomain).toList();
    }
}
