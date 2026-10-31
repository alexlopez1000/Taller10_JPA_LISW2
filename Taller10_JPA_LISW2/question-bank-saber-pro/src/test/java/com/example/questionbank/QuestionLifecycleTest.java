package com.example.questionbank;

import com.example.questionbank.application.QuestionService;
import com.example.questionbank.application.ReviewService;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.ReviewDecision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of the question lifecycle rules: valid submissions, rejection of
 * incomplete questions, forbidden transitions, the full happy path until
 * ARCHIVED and the way back from rejection to DRAFT.
 */
class QuestionLifecycleTest extends AbstractIntegrationTest {

    @Test
    void test09SubmitValidQuestionForReview() {
        Question draft = createClassifiedMultipleChoiceDraft();
        assertEquals(QuestionStatus.DRAFT, draft.getStatus());

        Question submitted = questionService.submitForReview(draft.getId());

        assertEquals(QuestionStatus.PENDING_REVIEW, submitted.getStatus());
        assertEquals(QuestionStatus.PENDING_REVIEW, statusOf(draft.getId()));
    }

    @Test
    void test10SubmitIncompleteQuestionFails() {
        // A question without topic, competencies or options.
        Question incomplete = questionService.createQuestion(authorId,
            "Short and unclassified statement.", Difficulty.LOW, "TRUE_FALSE", false, null, List.of(),
            new QuestionService.NewMetadata(QuestionMetadata.CognitiveLevel.REMEMBER, 1, null));

        var exception = assertThrows(IllegalStateException.class,
            () -> questionService.submitForReview(incomplete.getId()));
        assertTrue(exception.getMessage().contains("topic"), "The violations mention the missing topic");
        assertTrue(exception.getMessage().contains("competency"), "The violations mention the missing competency");
        assertTrue(exception.getMessage().contains("truth value"), "The violations mention the missing answer");
        assertEquals(QuestionStatus.DRAFT, statusOf(incomplete.getId()),
            "An incomplete question must remain in DRAFT");
    }

    @Test
    void test11InvalidTransitionIsRejected() {
        Question draft = createClassifiedMultipleChoiceDraft();

        // Skipping states: a DRAFT question cannot jump straight to APPROVED.
        assertThrows(IllegalStateException.class, () -> reviewService.approve(draft.getId(), reviewerId, "skip"));
        assertThrows(IllegalStateException.class, () -> reviewService.publish(draft.getId()));
        assertEquals(QuestionStatus.DRAFT, statusOf(draft.getId()));
    }

    @Test
    void test12FullLifecycleUntilArchived() {
        Question draft = createClassifiedMultipleChoiceDraft();

        questionService.submitForReview(draft.getId());
        assertEquals(QuestionStatus.PENDING_REVIEW, statusOf(draft.getId()));
        reviewService.startReview(draft.getId(), reviewerId);
        assertEquals(QuestionStatus.IN_REVIEW, statusOf(draft.getId()));
        reviewService.approve(draft.getId(), reviewerId, "Meets the editorial rules.");
        assertEquals(QuestionStatus.APPROVED, statusOf(draft.getId()));
        reviewService.publish(draft.getId());
        assertEquals(QuestionStatus.PUBLISHED, statusOf(draft.getId()));
        reviewService.archive(draft.getId());
        assertEquals(QuestionStatus.ARCHIVED, statusOf(draft.getId()));

        var reviews = reviewService.reviewsOf(draft.getId());
        assertEquals(1, reviews.size());
        assertEquals(ReviewDecision.APPROVED, reviews.get(0).getDecision());
    }

    @Test
    void test13RejectReturnsQuestionToDraft() {
        Question draft = createClassifiedMultipleChoiceDraft();
        questionService.submitForReview(draft.getId());
        reviewService.startReview(draft.getId(), reviewerId);

        Question rejected = reviewService.reject(draft.getId(), reviewerId,
            "The statement is ambiguous; please rewrite it.");

        assertEquals(QuestionStatus.DRAFT, rejected.getStatus());
        var reviews = reviewService.reviewsOf(draft.getId());
        assertEquals(1, reviews.size());
        assertEquals(ReviewDecision.REJECTED, reviews.get(0).getDecision());
        assertEquals("Raul Gomez", reviews.get(0).getReviewer().getFullName());
    }

    @Test
    void test14OnlyReviewersCanApprove() {
        Question draft = createClassifiedMultipleChoiceDraft();
        questionService.submitForReview(draft.getId());
        reviewService.startReview(draft.getId(), reviewerId);

        // The author does not hold the REVIEWER role.
        assertThrows(IllegalStateException.class, () -> reviewService.approve(draft.getId(), authorId, "self ok"));
        assertEquals(QuestionStatus.IN_REVIEW, statusOf(draft.getId()));
    }

    @Test
    void test22ReviewsReadableAfterPersistenceContextIsCleared() {
        Question draft = createClassifiedMultipleChoiceDraft();
        questionService.submitForReview(draft.getId());
        reviewService.startReview(draft.getId(), reviewerId);
        reviewService.approve(draft.getId(), reviewerId, "Fine work.");
        entityManager.clear(); // simulate a fresh session: the question becomes a LAZY proxy

        var reviews = reviewService.reviewsOf(draft.getId());

        assertEquals(1, reviews.size());
        assertEquals(ReviewDecision.APPROVED, reviews.get(0).getDecision());
        assertEquals("MULTIPLE_CHOICE", reviews.get(0).getQuestion().getType(),
            "The JOINED proxy must be resolved to its concrete subtype");
    }
}
