package com.example.questionbank.application;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Review;
import com.example.questionbank.domain.model.ReviewDecision;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.ReviewRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Use cases of the editorial lifecycle executed by reviewers: taking a
 * question under review, approving it, rejecting it back to draft, publishing
 * it and archiving it.
 *
 * <p>Every use case validates the business rules before changing the state and
 * runs inside a transaction: the state change and the review record are
 * written together or not at all. This is the separation between persisting a
 * question and validating its lifecycle requested by the laboratory.</p>
 */
@Service
@Transactional
public class ReviewService {

    private final QuestionRepository questions;
    private final UserRepository users;
    private final ReviewRepository reviews;

    /**
     * Creates the service with its repository contracts.
     *
     * @param questions question contract
     * @param users     user contract
     * @param reviews   review contract
     */
    public ReviewService(QuestionRepository questions, UserRepository users, ReviewRepository reviews) {
        this.questions = questions;
        this.users = users;
        this.reviews = reviews;
    }

    /**
     * Moves a PENDING_REVIEW question to IN_REVIEW, marking that a reviewer
     * started the evaluation.
     *
     * @param questionId identity of the question
     * @param reviewerId identity of the reviewer
     * @return the question in IN_REVIEW state
     */
    public Question startReview(Long questionId, Long reviewerId) {
        User reviewer = loadReviewer(reviewerId);
        Question question = questions.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        question.changeStatus(QuestionStatus.IN_REVIEW);
        return questions.save(question);
    }

    /**
     * Approves a question under review, records the review decision and
     * stamps the last review date in the metadata.
     *
     * @param questionId identity of the question
     * @param reviewerId identity of the reviewer
     * @param comments   justification of the approval
     * @return the question in APPROVED state
     */
    public Question approve(Long questionId, Long reviewerId, String comments) {
        User reviewer = loadReviewer(reviewerId);
        Question question = questions.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        question.changeStatus(QuestionStatus.APPROVED);
        question.getMetadata().setLastReviewedAt(LocalDate.now());
        questions.save(question);
        reviews.save(new Review(null, question, reviewer, ReviewDecision.APPROVED, comments, LocalDateTime.now()));
        return question;
    }

    /**
     * Rejects a question under review: the state returns to DRAFT for
     * correction and the rejection is recorded in the history.
     *
     * @param questionId identity of the question
     * @param reviewerId identity of the reviewer
     * @param comments   reasons for the rejection
     * @return the question back in DRAFT state
     */
    public Question reject(Long questionId, Long reviewerId, String comments) {
        User reviewer = loadReviewer(reviewerId);
        Question question = questions.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        question.changeStatus(QuestionStatus.DRAFT);
        questions.save(question);
        reviews.save(new Review(null, question, reviewer, ReviewDecision.REJECTED, comments, LocalDateTime.now()));
        return question;
    }

    /**
     * Publishes an approved question, making it available for quizzes.
     *
     * @param questionId identity of the question
     * @return the question in PUBLISHED state
     */
    public Question publish(Long questionId) {
        Question question = questions.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        question.changeStatus(QuestionStatus.PUBLISHED);
        return questions.save(question);
    }

    /**
     * Archives a published question, retiring it from circulation.
     *
     * @param questionId identity of the question
     * @return the question in ARCHIVED state
     */
    public Question archive(Long questionId) {
        Question question = questions.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        question.changeStatus(QuestionStatus.ARCHIVED);
        return questions.save(question);
    }

    /**
     * Lists the review history of a question.
     *
     * @param questionId identity of the question
     * @return reviews recorded for that question
     */
    @Transactional(readOnly = true)
    public List<Review> reviewsOf(Long questionId) {
        return reviews.findByQuestionId(questionId);
    }

    /** Loads a user and checks that the user may review questions. */
    private User loadReviewer(Long reviewerId) {
        User reviewer = users.findById(reviewerId)
            .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewerId));
        if (!reviewer.hasAnyRole(Role.REVIEWER)) {
            throw new IllegalStateException("User " + reviewerId + " does not have the REVIEWER role.");
        }
        return reviewer;
    }
}
