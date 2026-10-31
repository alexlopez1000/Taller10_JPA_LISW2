package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.QuestionMetadata;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * JPA record for the one-to-one editorial metadata of a question.
 *
 * <p>The owning side of the association is {@link QuestionRecord}, which holds
 * the {@code metadata_id} foreign key with a unique constraint; this record
 * exposes the inverse side through {@code mappedBy}. The cascade to the
 * question guarantees that the metadata is created and removed together with
 * its question.</p>
 */
@Entity
@Table(name = "question_metadata")
public class QuestionMetadataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "cognitive_level", nullable = false, length = 20)
    private QuestionMetadata.CognitiveLevel cognitiveLevel;

    @Column(name = "estimated_time_minutes", nullable = false)
    private Integer estimatedTimeMinutes;

    @Column(name = "last_reviewed_at")
    private LocalDate lastReviewedAt;

    @Column(name = "editorial_notes", length = 500)
    private String editorialNotes;

    @OneToOne(mappedBy = "metadata", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private QuestionRecord question;

    /** Required no-argument constructor for JPA. */
    public QuestionMetadataRecord() {
    }

    /**
     * Creates the metadata record.
     *
     * @param cognitiveLevel       Bloom cognitive level
     * @param estimatedTimeMinutes expected answering time in minutes
     * @param lastReviewedAt       date of the last review, may be {@code null}
     * @param editorialNotes       editorial observations, may be {@code null}
     */
    public QuestionMetadataRecord(QuestionMetadata.CognitiveLevel cognitiveLevel, Integer estimatedTimeMinutes,
                                  LocalDate lastReviewedAt, String editorialNotes) {
        this.cognitiveLevel = cognitiveLevel;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.lastReviewedAt = lastReviewedAt;
        this.editorialNotes = editorialNotes;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return Bloom cognitive level */
    public QuestionMetadata.CognitiveLevel getCognitiveLevel() {
        return cognitiveLevel;
    }

    /** @param cognitiveLevel Bloom cognitive level from the domain object */
    public void setCognitiveLevel(QuestionMetadata.CognitiveLevel cognitiveLevel) {
        this.cognitiveLevel = cognitiveLevel;
    }

    /** @return expected answering time in minutes */
    public Integer getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    /** @param estimatedTimeMinutes expected answering time from the domain object */
    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    /** @return date of the last review */
    public LocalDate getLastReviewedAt() {
        return lastReviewedAt;
    }

    /** @param lastReviewedAt date in which the review finished */
    public void setLastReviewedAt(LocalDate lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    /** @return editorial observations */
    public String getEditorialNotes() {
        return editorialNotes;
    }

    /** @param editorialNotes editorial observations from the domain object */
    public void setEditorialNotes(String editorialNotes) {
        this.editorialNotes = editorialNotes;
    }
}
