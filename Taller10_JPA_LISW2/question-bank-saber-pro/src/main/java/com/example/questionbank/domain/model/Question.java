package com.example.questionbank.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base concept of the question bank: an item that evaluates one or more
 * competencies and travels through the editorial lifecycle
 * DRAFT &rarr; PENDING_REVIEW &rarr; IN_REVIEW &rarr; APPROVED &rarr; PUBLISHED &rarr; ARCHIVED.
 *
 * <p>This class is the root of the domain inheritance hierarchy. It contains
 * only business rules: no JPA annotations, no Spring types. The persistence
 * hierarchy lives in the infrastructure layer and mirrors this one with the
 * JOINED strategy.</p>
 */
public abstract class Question {

    /** Minimum length accepted for the statement of a reviewable question. */
    public static final int MIN_STATEMENT_LENGTH = 20;

    protected Long id;
    protected String statement;
    protected Difficulty difficulty;
    protected QuestionStatus status;
    protected User author;
    protected Topic topic;
    protected QuestionMetadata metadata;
    protected final List<QuestionCompetency> competencyContributions = new ArrayList<>();

    /**
     * Creates an empty question in DRAFT state; used by repositories and
     * mappers when rebuilding the object graph.
     */
    protected Question() {
        this.status = QuestionStatus.DRAFT;
    }

    /**
     * Creates a question in DRAFT state.
     *
     * @param id         persistent identity, {@code null} before saving
     * @param statement  wording shown to the student
     * @param difficulty cognitive difficulty
     * @param author     user that wrote the question
     * @param metadata   one-to-one editorial metadata owned by the question
     */
    protected Question(Long id, String statement, Difficulty difficulty, User author, QuestionMetadata metadata) {
        this.id = id;
        this.statement = statement;
        this.difficulty = difficulty;
        this.status = QuestionStatus.DRAFT;
        this.author = author;
        this.metadata = metadata;
    }

    /** @return discriminant of the concrete type, for example "MULTIPLE_CHOICE" */
    public abstract String getType();

    /** @return persistent identity assigned by the repository */
    public Long getId() {
        return id;
    }

    /** @param id identifier assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return wording shown to the student */
    public String getStatement() {
        return statement;
    }

    /** @return cognitive difficulty */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /** @return current lifecycle state */
    public QuestionStatus getStatus() {
        return status;
    }

    /**
     * Restores a persisted lifecycle state when the persistence mapper rebuilds
     * the domain object. Application services must use {@link #changeStatus(QuestionStatus)}
     * so that lifecycle transitions are validated.
     *
     * @param status persisted lifecycle state
     */
    public void restoreStatus(QuestionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Question status cannot be null.");
        }
        this.status = status;
    }

    /** @return user that wrote the question (many-to-one) */
    public User getAuthor() {
        return author;
    }

    /** @return academic topic of the question (many-to-one), {@code null} until classified */
    public Topic getTopic() {
        return topic;
    }

    /** @return one-to-one editorial metadata owned by the question */
    public QuestionMetadata getMetadata() {
        return metadata;
    }

    /** @param metadata one-to-one editorial metadata; used when rebuilding the object graph */
    public void setMetadata(QuestionMetadata metadata) {
        this.metadata = metadata;
    }

    /** @param author author of the question; used when rebuilding the object graph */
    public void setAuthor(User author) {
        this.author = author;
    }

    /** @param topic academic topic; used when rebuilding the object graph */
    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    /**
     * Adds one competency contribution to this question.
     *
     * @param contribution association with a competency and its weight
     */
    public void addCompetencyContribution(QuestionCompetency contribution) {
        contribution.setQuestion(this);
        this.competencyContributions.add(contribution);
    }

    /** @return read-only view of the competency contributions (one-to-many association) */
    public List<QuestionCompetency> getCompetencyContributions() {
        return Collections.unmodifiableList(competencyContributions);
    }

    /**
     * Classifies the question: assigns its topic and replaces the set of
     * competency contributions with weights that must total 100 percent.
     *
     * @param topic             academic topic of the question
     * @param newContributions  associations with competencies and their weights
     */
    public void classify(Topic topic, List<QuestionCompetency> newContributions) {
        this.topic = topic;
        this.competencyContributions.clear();
        for (QuestionCompetency contribution : newContributions) {
            contribution.setQuestion(this);
            this.competencyContributions.add(contribution);
        }
    }

    /**
     * Moves the question to another lifecycle state enforcing the transition
     * rules of {@link QuestionStatus#canTransitionTo(QuestionStatus)}.
     *
     * @param target desired state
     * @throws IllegalStateException when the transition is not allowed
     */
    public void changeStatus(QuestionStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException(
                "Invalid lifecycle transition: " + status + " -> " + target + " for question " + id);
        }
        this.status = target;
    }

    /**
     * Collects the business violations that prevent the question from being
     * submitted to review. An empty list means the question is complete.
     *
     * @return list of human readable violations
     */
    public List<String> validateForSubmission() {
        List<String> violations = new ArrayList<>();
        if (statement == null || statement.isBlank()) {
            violations.add("The statement is required.");
        } else if (statement.length() < MIN_STATEMENT_LENGTH) {
            violations.add("The statement must have at least " + MIN_STATEMENT_LENGTH + " characters.");
        }
        if (author == null) {
            violations.add("The question must have an author.");
        }
        if (topic == null) {
            violations.add("The question must be classified with a topic before review.");
        }
        if (metadata == null) {
            violations.add("The question must have editorial metadata.");
        }
        if (competencyContributions.isEmpty()) {
            violations.add("The question must evaluate at least one competency.");
        }
        addTypeSpecificViolations(violations);
        return violations;
    }

    /**
     * Hook that lets each concrete type add its own submission rules, for
     * example the number of options of a multiple choice question.
     *
     * @param violations accumulator of violations
     */
    protected abstract void addTypeSpecificViolations(List<String> violations);
}
