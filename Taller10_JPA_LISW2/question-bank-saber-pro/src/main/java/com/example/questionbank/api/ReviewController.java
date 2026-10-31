package com.example.questionbank.api;

import com.example.questionbank.application.ReviewService;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Review;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for the editorial lifecycle executed by reviewers. Each
 * endpoint maps to one transition of the state machine
 * DRAFT &rarr; PENDING_REVIEW &rarr; IN_REVIEW &rarr; APPROVED &rarr; PUBLISHED &rarr; ARCHIVED.
 */
@RestController
@RequestMapping("/api/questions/{id}")
public class ReviewController {

    private final ReviewService service;

    /** Creates the controller. @param service lifecycle use cases */
    public ReviewController(ReviewService service) {
        this.service = service;
    }

    /** Request body with the reviewer identity and optional comments. */
    public record ReviewRequest(Long reviewerId, String comments) {
    }

    /** Response for one review entry. */
    public record ReviewResponse(Long id, Long questionId, Long reviewerId, String reviewerName, String decision,
                                 String comments, String reviewedAt) {

        /** Builds the response from a domain review. */
        public static ReviewResponse from(Review review) {
            return new ReviewResponse(review.getId(), review.getQuestion().getId(),
                review.getReviewer().getId(), review.getReviewer().getFullName(), review.getDecision().name(),
                review.getComments(), review.getReviewedAt().toString());
        }
    }

    /** Starts the review of a pending question. */
    @PostMapping("/start-review")
    public QuestionLifecycleResponse startReview(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return QuestionLifecycleResponse.from(service.startReview(id, request.reviewerId()));
    }

    /** Approves a question under review. */
    @PostMapping("/approve")
    public QuestionLifecycleResponse approve(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return QuestionLifecycleResponse.from(service.approve(id, request.reviewerId(), request.comments()));
    }

    /** Rejects a question under review; it returns to DRAFT. */
    @PostMapping("/reject")
    public QuestionLifecycleResponse reject(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return QuestionLifecycleResponse.from(service.reject(id, request.reviewerId(), request.comments()));
    }

    /** Publishes an approved question. */
    @PostMapping("/publish")
    public QuestionLifecycleResponse publish(@PathVariable Long id) {
        return QuestionLifecycleResponse.from(service.publish(id));
    }

    /** Archives a published question. */
    @PostMapping("/archive")
    public QuestionLifecycleResponse archive(@PathVariable Long id) {
        return QuestionLifecycleResponse.from(service.archive(id));
    }

    /** Lists the review history of the question. */
    @GetMapping("/reviews")
    public List<ReviewResponse> reviews(@PathVariable Long id) {
        return service.reviewsOf(id).stream().map(ReviewResponse::from).toList();
    }

    /** Compact response used by lifecycle transitions. */
    public record QuestionLifecycleResponse(Long id, String type, String status) {

        /** Builds the response from a domain question. */
        public static QuestionLifecycleResponse from(Question question) {
            return new QuestionLifecycleResponse(question.getId(), question.getType(), question.getStatus().name());
        }
    }
}
