package com.example.questionbank.domain.model;

import java.math.BigDecimal;

/**
 * Associative concept that links one {@link Question} with one {@link Competency}
 * and records the percentage of contribution of the competency to the question.
 *
 * <p>This is the design decision requested by the laboratory: because the
 * association carries an extra attribute (the weight), it becomes a first-class
 * concept instead of a bare many-to-many join. In the persistence layer it is
 * mapped with two one-to-many / many-to-one relationships over the
 * {@code question_competencies} table.</p>
 */
public class QuestionCompetency {

    private Long id;
    private Question question;
    private Competency competency;
    private BigDecimal contributionWeight;

    /** Creates an empty association; used by repositories and mappers. */
    public QuestionCompetency() {
    }

    /**
     * Creates the association between a question and a competency.
     *
     * @param id                 persistent identity, {@code null} before saving
     * @param question           owner question
     * @param competency         evaluated competency
     * @param contributionWeight percentage of contribution, expected between 0 and 100
     */
    public QuestionCompetency(Long id, Question question, Competency competency, BigDecimal contributionWeight) {
        this.id = id;
        this.question = question;
        this.competency = competency;
        this.contributionWeight = contributionWeight;
    }

    /** @return persistent identity assigned by the repository */
    public Long getId() {
        return id;
    }

    /** @param id identifier assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the question that evaluates the competency */
    public Question getQuestion() {
        return question;
    }

    /** @param question owner question */
    public void setQuestion(Question question) {
        this.question = question;
    }

    /** @return the evaluated competency */
    public Competency getCompetency() {
        return competency;
    }

    /** @return percentage of contribution of the competency to the question */
    public BigDecimal getContributionWeight() {
        return contributionWeight;
    }
}
