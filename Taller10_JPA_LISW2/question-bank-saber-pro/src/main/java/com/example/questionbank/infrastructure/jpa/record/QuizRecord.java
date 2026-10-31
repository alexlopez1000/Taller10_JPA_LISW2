package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA record for quizzes (simulacros).
 *
 * <p>The association with questions is a <strong>direct many-to-many</strong>
 * mapped over the {@code quiz_questions} join table with its two foreign keys
 * ({@code quiz_id} and {@code question_id}). No extra attributes are needed
 * here, which is precisely why a join table is preferred over an associative
 * entity for this relationship.</p>
 */
@Entity
@Table(name = "quizzes")
public class QuizRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserRecord creator;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(name = "quiz_questions",
        joinColumns = @JoinColumn(name = "quiz_id"),
        inverseJoinColumns = @JoinColumn(name = "question_id"))
    private Set<QuestionRecord> questions = new HashSet<>();

    /** Required no-argument constructor for JPA. */
    public QuizRecord() {
    }

    /**
     * Creates a quiz record.
     *
     * @param title       quiz title
     * @param description purpose of the quiz
     * @param creator     teacher record that organizes the quiz
     * @param createdAt   creation timestamp
     */
    public QuizRecord(String title, String description, UserRecord creator, LocalDateTime createdAt) {
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.createdAt = createdAt;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return quiz title */
    public String getTitle() {
        return title;
    }

    /** @return purpose of the quiz */
    public String getDescription() {
        return description;
    }

    /** @return teacher record that organizes the quiz */
    public UserRecord getCreator() {
        return creator;
    }

    /** @return creation timestamp */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return questions gathered by the quiz through the join table */
    public Set<QuestionRecord> getQuestions() {
        return questions;
    }

    /** @param questions questions gathered by the quiz */
    public void setQuestions(Set<QuestionRecord> questions) {
        this.questions = questions;
    }
}
