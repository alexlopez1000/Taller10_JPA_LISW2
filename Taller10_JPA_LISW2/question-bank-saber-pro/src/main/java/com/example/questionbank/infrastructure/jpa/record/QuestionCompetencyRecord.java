package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

/**
 * Associative entity between questions and competencies.
 *
 * <p>This is the design alternative to a direct {@code @ManyToMany}: because
 * the association needs the extra attribute {@code contribution_weight}, it is
 * promoted to its own entity and the relationship is decomposed into two
 * one-to-many / many-to-one pairs over the {@code question_competencies}
 * table. The pair (question, competency) is unique.</p>
 */
@Entity
@Table(name = "question_competencies",
    uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "competency_id"}))
public class QuestionCompetencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionRecord question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competency_id", nullable = false)
    private CompetencyRecord competency;

    @Column(name = "contribution_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal contributionWeight;

    /** Required no-argument constructor for JPA. */
    protected QuestionCompetencyRecord() {
    }

    /**
     * Creates the associative record.
     *
     * @param competency         competency record
     * @param contributionWeight percentage of contribution
     */
    public QuestionCompetencyRecord(CompetencyRecord competency, BigDecimal contributionWeight) {
        this.competency = competency;
        this.contributionWeight = contributionWeight;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return owning question record */
    public QuestionRecord getQuestion() {
        return question;
    }

    /** @param question owning question record; keeps the bidirectional pair in sync */
    public void setQuestion(QuestionRecord question) {
        this.question = question;
    }

    /** @return competency record */
    public CompetencyRecord getCompetency() {
        return competency;
    }

    /** @return percentage of contribution */
    public BigDecimal getContributionWeight() {
        return contributionWeight;
    }

    /** @param contributionWeight new percentage; allows updating the association in place */
    public void setContributionWeight(BigDecimal contributionWeight) {
        this.contributionWeight = contributionWeight;
    }
}
