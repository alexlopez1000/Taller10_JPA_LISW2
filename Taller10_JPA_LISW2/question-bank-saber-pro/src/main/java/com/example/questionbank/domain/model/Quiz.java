package com.example.questionbank.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A quiz (simulacro) composed of published questions.
 *
 * <p>A quiz gathers many questions and a question may be reused in many
 * quizzes, which is a direct many-to-many association without extra
 * attributes: the persistence layer maps it with {@code @ManyToMany} over the
 * {@code quiz_questions} join table. This class enforces the business rule
 * that only PUBLISHED questions may enter a quiz.</p>
 */
public class Quiz {

    private Long id;
    private String title;
    private String description;
    private User creator;
    private LocalDateTime createdAt;
    private final List<Question> questions = new ArrayList<>();

    /** Creates an empty quiz; used by repositories and mappers. */
    public Quiz() {
    }

    /**
     * Creates a quiz.
     *
     * @param id          persistent identity, {@code null} before saving
     * @param title       quiz title
     * @param description purpose of the quiz
     * @param creator     teacher that organized the quiz
     * @param createdAt   creation timestamp
     */
    public Quiz(Long id, String title, String description, User creator, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.createdAt = createdAt;
    }

    /** @return persistent identity assigned by the repository */
    public Long getId() {
        return id;
    }

    /** @param id identifier assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return quiz title */
    public String getTitle() {
        return title;
    }

    /** @return purpose of the quiz */
    public String getDescription() {
        return description;
    }

    /** @return teacher that organized the quiz */
    public User getCreator() {
        return creator;
    }

    /** @return creation timestamp */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @param createdAt creation timestamp; used when rebuilding the object graph */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** @return read-only view of the questions gathered by the quiz (many-to-many) */
    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    /**
     * Adds a question to this quiz.
     *
     * @param question question to add
     * @throws IllegalArgumentException when the question is null
     * @throws IllegalStateException when the question is not PUBLISHED
     */
    public void addQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null.");
        }
        if (question.getStatus() != QuestionStatus.PUBLISHED) {
            throw new IllegalStateException("Question " + question.getId()
                + " is not published (status: " + question.getStatus() + ").");
        }
        if (!questions.contains(question)) {
            questions.add(question);
        }
    }

    /**
     * Replaces the questions of the quiz while enforcing the publication rule.
     *
     * @param newQuestions questions to set
     */
    public void replaceQuestions(List<Question> newQuestions) {
        questions.clear();
        if (newQuestions != null) {
            newQuestions.forEach(this::addQuestion);
        }
    }
}
