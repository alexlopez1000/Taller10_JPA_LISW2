package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * JPA record for true/false questions. The expected truth value is stored in
 * the {@code true_false_questions} table while the shared attributes remain in
 * {@code questions} (JOINED strategy).
 */
@Entity
@Table(name = "true_false_questions")
@PrimaryKeyJoinColumn(name = "question_id")
public class TrueFalseQuestionRecord extends QuestionRecord {

    @Column(name = "expected_answer")
    private Boolean expectedAnswer;

    /** Required no-argument constructor for JPA. */
    protected TrueFalseQuestionRecord() {
    }

    /**
     * Creates the record of a true/false question.
     *
     * @param statement      wording shown to the student
     * @param difficulty     cognitive difficulty
     * @param status         lifecycle state
     * @param author         author record
     * @param expectedAnswer expected truth value, may be {@code null} while drafting
     */
    public TrueFalseQuestionRecord(String statement, Difficulty difficulty, QuestionStatus status,
                                   UserRecord author, Boolean expectedAnswer) {
        super(statement, difficulty, status, author);
        this.expectedAnswer = expectedAnswer;
    }

    /** @return expected truth value */
    public Boolean getExpectedAnswer() {
        return expectedAnswer;
    }

    /** @param expectedAnswer expected truth value from the domain object */
    public void setExpectedAnswer(Boolean expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }
}
