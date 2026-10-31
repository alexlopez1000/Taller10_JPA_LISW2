package com.example.questionbank.domain.model;

import java.time.LocalDate;

/**
 * Complementary editorial data of exactly one question: cognitive level,
 * estimated answering time, date of the last review and editorial notes.
 *
 * <p>Modeled as a one-to-one association because each question owns exactly one
 * metadata record and each record belongs to exactly one question. The owning
 * side is the question: the foreign key lives in the {@code questions} table
 * with a unique constraint, which guarantees that a question can never point to
 * two metadata records.</p>
 */
public class QuestionMetadata {

    private Long id;
    private CognitiveLevel cognitiveLevel;
    private Integer estimatedTimeMinutes;
    private LocalDate lastReviewedAt;
    private String editorialNotes;

    /** Cognitive level of the question, following the Bloom taxonomy. */
    public enum CognitiveLevel {
        /** Remember facts and basic concepts. */
        REMEMBER,
        /** Explain ideas or concepts. */
        UNDERSTAND,
        /** Use information in new situations. */
        APPLY,
        /** Draw connections among ideas. */
        ANALYZE,
        /** Justify a decision or course of action. */
        EVALUATE,
        /** Produce new or original work. */
        CREATE
    }

    /** Creates empty metadata; used by repositories and mappers. */
    public QuestionMetadata() {
    }

    /**
     * Creates the metadata of a question.
     *
     * @param id                   persistent identity, {@code null} before saving
     * @param cognitiveLevel       Bloom cognitive level
     * @param estimatedTimeMinutes expected answering time in minutes
     * @param lastReviewedAt       date of the last review, {@code null} until reviewed
     * @param editorialNotes       editorial observations, may be {@code null}
     */
    public QuestionMetadata(Long id, CognitiveLevel cognitiveLevel, Integer estimatedTimeMinutes,
                            LocalDate lastReviewedAt, String editorialNotes) {
        this.id = id;
        this.cognitiveLevel = cognitiveLevel;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.lastReviewedAt = lastReviewedAt;
        this.editorialNotes = editorialNotes;
    }

    /** @return persistent identity assigned by the repository */
    public Long getId() {
        return id;
    }

    /** @param id identifier assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return Bloom cognitive level */
    public CognitiveLevel getCognitiveLevel() {
        return cognitiveLevel;
    }

    /** @return expected answering time in minutes */
    public Integer getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    /** @return date of the last review, or {@code null} when not reviewed yet */
    public LocalDate getLastReviewedAt() {
        return lastReviewedAt;
    }

    /** @param lastReviewedAt date in which a reviewer finished evaluating the question */
    public void setLastReviewedAt(LocalDate lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    /** @return editorial observations, may be {@code null} */
    public String getEditorialNotes() {
        return editorialNotes;
    }
}
