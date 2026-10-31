package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA record for multiple choice questions. With the JOINED strategy the
 * shared columns stay in {@code questions} and the flag that allows multiple
 * selection is stored in the {@code multiple_choice_questions} table, joined
 * by the primary key.
 */
@Entity
@Table(name = "multiple_choice_questions")
@PrimaryKeyJoinColumn(name = "question_id")
public class MultipleChoiceQuestionRecord extends QuestionRecord {

    @Column(name = "allow_multiple_selection", nullable = false)
    private boolean allowMultipleSelection;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerOptionRecord> options = new ArrayList<>();

    /** Required no-argument constructor for JPA. */
    protected MultipleChoiceQuestionRecord() {
    }

    /**
     * Creates the record of a multiple choice question.
     *
     * @param statement              wording shown to the student
     * @param difficulty             cognitive difficulty
     * @param status                 lifecycle state
     * @param author                 author record
     * @param allowMultipleSelection whether several options may be selected
     */
    public MultipleChoiceQuestionRecord(String statement, Difficulty difficulty, QuestionStatus status,
                                        UserRecord author, boolean allowMultipleSelection) {
        super(statement, difficulty, status, author);
        this.allowMultipleSelection = allowMultipleSelection;
    }

    /** @return whether several options may be selected */
    public boolean isAllowMultipleSelection() {
        return allowMultipleSelection;
    }

    /** @param allowMultipleSelection flag from the domain object */
    public void setAllowMultipleSelection(boolean allowMultipleSelection) {
        this.allowMultipleSelection = allowMultipleSelection;
    }

    /** @return answer options owned by this question */
    public List<AnswerOptionRecord> getOptions() {
        return options;
    }

    /**
     * Adds an option and keeps both sides of the one-to-many in sync.
     *
     * @param option option record to add
     */
    public void addOption(AnswerOptionRecord option) {
        options.add(option);
        option.setQuestion(this);
    }
}
