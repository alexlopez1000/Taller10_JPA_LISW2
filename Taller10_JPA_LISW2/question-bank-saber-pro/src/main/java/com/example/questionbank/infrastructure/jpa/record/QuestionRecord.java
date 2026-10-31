package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA record for the base of the question hierarchy, mapped with the
 * <strong>JOINED</strong> inheritance strategy: shared attributes live in the
 * {@code questions} table while subtype-specific attributes live in
 * {@code multiple_choice_questions} and {@code true_false_questions}.
 *
 * <p>This record owns the question associations:</p>
 * <ul>
 *   <li>{@code author} and {@code topic}: many-to-one, foreign keys
 *       {@code author_id} and {@code topic_id} in {@code questions}.</li>
 *   <li>{@code metadata}: one-to-one with cascade, foreign key
 *       {@code metadata_id} (unique and not null) so a metadata record can never
 *       be shared by two questions.</li>
 *   <li>{@code competencyContributions}: one-to-many over the associative
 *       entity {@link QuestionCompetencyRecord}.</li>
 * </ul>
 */
@Entity
@Table(name = "questions")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class QuestionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String statement;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QuestionStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserRecord author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private TopicRecord topic;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metadata_id", nullable = false, unique = true)
    private QuestionMetadataRecord metadata;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionCompetencyRecord> competencyContributions = new ArrayList<>();

    /** Required no-argument constructor for JPA. */
    protected QuestionRecord() {
    }

    /**
     * Creates the base record of a question.
     *
     * @param statement  wording shown to the student
     * @param difficulty cognitive difficulty
     * @param status     lifecycle state
     * @param author     author record, owning a many-to-one reference
     */
    protected QuestionRecord(String statement, Difficulty difficulty, QuestionStatus status, UserRecord author) {
        this.statement = statement;
        this.difficulty = difficulty;
        this.status = status;
        this.author = author;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return wording shown to the student */
    public String getStatement() {
        return statement;
    }

    /** @param statement new wording of the question */
    public void setStatement(String statement) {
        this.statement = statement;
    }

    /** @return cognitive difficulty */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /** @param difficulty cognitive difficulty */
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /** @return lifecycle state */
    public QuestionStatus getStatus() {
        return status;
    }

    /** @param status lifecycle state */
    public void setStatus(QuestionStatus status) {
        this.status = status;
    }

    /** @return author record */
    public UserRecord getAuthor() {
        return author;
    }

    /** @return topic record, or {@code null} until the question is classified */
    public TopicRecord getTopic() {
        return topic;
    }

    /** @param topic topic record to associate */
    public void setTopic(TopicRecord topic) {
        this.topic = topic;
    }

    /** @return metadata record owned by this question */
    public QuestionMetadataRecord getMetadata() {
        return metadata;
    }

    /** @param metadata metadata record owned by this question */
    public void setMetadata(QuestionMetadataRecord metadata) {
        this.metadata = metadata;
    }

    /** @return competency contributions of this question */
    public List<QuestionCompetencyRecord> getCompetencyContributions() {
        return competencyContributions;
    }

    /**
     * Updates the common columns of the record with domain data.
     *
     * @param domainStatement  statement from the domain object
     * @param domainDifficulty difficulty from the domain object
     * @param domainStatus     status from the domain object
     */
    public void updateFrom(String domainStatement, Difficulty domainDifficulty, QuestionStatus domainStatus) {
        this.statement = domainStatement;
        this.difficulty = domainDifficulty;
        this.status = domainStatus;
    }
}
