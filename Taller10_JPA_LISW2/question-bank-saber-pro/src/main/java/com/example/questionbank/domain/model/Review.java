package com.example.questionbank.domain.model;

import java.time.LocalDateTime;

/**
 * A review decision recorded by a reviewer over a question.
 *
 * <p>Each time a reviewer approves or rejects a question, a review entry is
 * stored with the reviewer identity, the decision and comments. This keeps the
 * evaluation history of the question without polluting the question concept
 * itself.</p>
 */
public class Review {

    private Long id;
    private Question question;
    private User reviewer;
    private ReviewDecision decision;
    private String comments;
    private LocalDateTime reviewedAt;

    /** Creates an empty review; used by repositories and mappers. */
    public Review() {
    }

    /**
     * Creates a review entry.
     *
     * @param id         persistent identity, {@code null} before saving
     * @param question   reviewed question
     * @param reviewer   user that evaluated the question
     * @param decision   decision taken
     * @param comments   justification of the decision
     * @param reviewedAt timestamp of the decision
     */
    public Review(Long id, Question question, User reviewer, ReviewDecision decision,
                  String comments, LocalDateTime reviewedAt) {
        this.id = id;
        this.question = question;
        this.reviewer = reviewer;
        this.decision = decision;
        this.comments = comments;
        this.reviewedAt = reviewedAt;
    }

    /** @return persistent identity assigned by the repository */
    public Long getId() {
        return id;
    }

    /** @param id identifier assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return reviewed question */
    public Question getQuestion() {
        return question;
    }

    /** @return user that evaluated the question */
    public User getReviewer() {
        return reviewer;
    }

    /** @return decision taken */
    public ReviewDecision getDecision() {
        return decision;
    }

    /** @return justification of the decision */
    public String getComments() {
        return comments;
    }

    /** @return timestamp of the decision */
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}
