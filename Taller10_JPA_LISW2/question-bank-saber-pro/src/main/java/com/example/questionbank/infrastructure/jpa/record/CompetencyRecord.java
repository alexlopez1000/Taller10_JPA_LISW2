package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** JPA record for academic competencies evaluated by questions. */
@Entity
@Table(name = "competencies")
public class CompetencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    /**
     * Inverse side of the association entity: one competency appears in many question contributions.
     * The foreign key lives in {@code question_competencies.competency_id}, owned by
     * {@link QuestionCompetencyRecord#getCompetency()}. No cascade: deleting a competency must never
     * silently delete the contributions of existing questions.
     */
    @OneToMany(mappedBy = "competency")
    private List<QuestionCompetencyRecord> questionContributions = new ArrayList<>();

    /** Required no-argument constructor for JPA. */
    public CompetencyRecord() {
    }

    /**
     * Creates a competency record.
     *
     * @param code        unique short code
     * @param name        human readable name
     * @param description what the competency measures
     */
    public CompetencyRecord(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return unique short code */
    public String getCode() {
        return code;
    }

    /** @return human readable name */
    public String getName() {
        return name;
    }

    /** @return what the competency measures */
    public String getDescription() {
        return description;
    }

    /** @return read-only view of the question contributions that reference this competency */
    public List<QuestionCompetencyRecord> getQuestionContributions() {
        return Collections.unmodifiableList(questionContributions);
    }
}
