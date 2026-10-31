package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.ReviewDecision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA record for the review history of a question.
 *
 * <p>Each row records one decision (approve or reject) taken by one reviewer,
 * so the evaluation history of a question is a one-to-many collection of
 * reviews owned by the question through the {@code question_id} foreign
 * key.</p>
 */
@Entity
@Table(name = "reviews")
public class ReviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionRecord question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private UserRecord reviewer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewDecision decision;

    @Column(length = 500)
    private String comments;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;

    /** Required no-argument constructor for JPA. */
    protected ReviewRecord() {
    }

    /**
     * Creates a review record.
     *
     * @param reviewer   reviewer record
     * @param decision   decision taken
     * @param comments   justification of the decision
     * @param reviewedAt timestamp of the decision
     */
    public ReviewRecord(UserRecord reviewer, ReviewDecision decision, String comments, LocalDateTime reviewedAt) {
        this.reviewer = reviewer;
        this.decision = decision;
        this.comments = comments;
        this.reviewedAt = reviewedAt;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return reviewed question record */
    public QuestionRecord getQuestion() {
        return question;
    }

    /** @param question reviewed question record; keeps the association in sync */
    public void setQuestion(QuestionRecord question) {
        this.question = question;
    }

    /** @return reviewer record */
    public UserRecord getReviewer() {
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
