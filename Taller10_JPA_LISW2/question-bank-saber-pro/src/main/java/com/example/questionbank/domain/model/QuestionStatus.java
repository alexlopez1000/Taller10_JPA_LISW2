package com.example.questionbank.domain.model;

/**
 * Lifecycle states of a question in the Saber Pro question bank.
 *
 * <p>The allowed transitions are:</p>
 * <pre>
 * DRAFT -&gt; PENDING_REVIEW -&gt; IN_REVIEW -&gt; APPROVED -&gt; PUBLISHED -&gt; ARCHIVED
 *                             |
 *                             +-&gt; DRAFT (rejected, back for correction)
 * </pre>
 *
 * <p>Only PUBLISHED questions may be used in quizzes. A question reaches
 * PUBLISHED only through the APPROVED state. The transition rules are enforced
 * by {@link #canTransitionTo(QuestionStatus)}.</p>
 */
public enum QuestionStatus {

    /** Newly created question, still being written by the author. */
    DRAFT,
    /** Submitted and waiting for a reviewer to take it. */
    PENDING_REVIEW,
    /** A reviewer is currently evaluating the question. */
    IN_REVIEW,
    /** Accepted by a reviewer; ready to be published. */
    APPROVED,
    /** Visible for quizzes and simulacros. */
    PUBLISHED,
    /** Retired from circulation; terminal state. */
    ARCHIVED;

    /**
     * Checks whether a transition from this state to the target state is allowed.
     *
     * @param target desired target state
     * @return {@code true} when the transition is valid according to the lifecycle rules
     */
    public boolean canTransitionTo(QuestionStatus target) {
        return switch (this) {
            case DRAFT -> target == PENDING_REVIEW;
            case PENDING_REVIEW -> target == IN_REVIEW;
            case IN_REVIEW -> target == APPROVED || target == DRAFT;
            case APPROVED -> target == PUBLISHED;
            case PUBLISHED -> target == ARCHIVED;
            case ARCHIVED -> false;
        };
    }
}
