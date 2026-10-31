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

/**
 * JPA record for one answer option of a multiple choice question.
 *
 * <p>The many-to-one association back to the question is mandatory
 * ({@code nullable = false}), so a row of {@code answer_options} can never
 * exist without its owner question: the database itself prevents orphan
 * options.</p>
 */
@Entity
@Table(name = "answer_options")
public class AnswerOptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_text", nullable = false, length = 500)
    private String optionText;

    @Column(nullable = false)
    private boolean correct;

    @Column(length = 500)
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionRecord question;

    /** Required no-argument constructor for JPA. */
    protected AnswerOptionRecord() {
    }

    /**
     * Creates an answer option record.
     *
     * @param optionText  text shown to the student
     * @param correct     whether the option is the expected answer
     * @param explanation feedback shown when the option is selected
     */
    public AnswerOptionRecord(String optionText, boolean correct, String explanation) {
        this.optionText = optionText;
        this.correct = correct;
        this.explanation = explanation;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return text shown to the student */
    public String getOptionText() {
        return optionText;
    }

    /** @return whether the option is the expected answer */
    public boolean isCorrect() {
        return correct;
    }

    /** @return feedback shown when the option is selected */
    public String getExplanation() {
        return explanation;
    }

    /** @return owning question record */
    public QuestionRecord getQuestion() {
        return question;
    }

    /** @param question owning question record; keeps both sides in sync */
    public void setQuestion(QuestionRecord question) {
        this.question = question;
    }
}
