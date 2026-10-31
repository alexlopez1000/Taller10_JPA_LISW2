package com.example.questionbank.domain.model;

import java.util.List;

/**
 * A question whose expected answer is simply true or false.
 *
 * <p>Second specialization of the hierarchy; together with
 * {@link MultipleChoiceQuestion} it demonstrates the JOINED inheritance
 * mapping: shared columns live in the {@code questions} table and the expected
 * truth value lives in the {@code true_false_questions} table.</p>
 */
public class TrueFalseQuestion extends Question {

    private Boolean expectedAnswer;

    /** Creates an empty question; used by repositories and mappers. */
    public TrueFalseQuestion() {
        super();
    }

    /**
     * Creates a true/false question in DRAFT state.
     *
     * @param id             persistent identity, {@code null} before saving
     * @param statement      wording shown to the student
     * @param difficulty     cognitive difficulty
     * @param author         user that wrote the question
     * @param metadata       one-to-one editorial metadata
     * @param expectedAnswer expected truth value; may stay {@code null} while drafting
     */
    public TrueFalseQuestion(Long id, String statement, Difficulty difficulty, User author,
                             QuestionMetadata metadata, Boolean expectedAnswer) {
        super(id, statement, difficulty, author, metadata);
        this.expectedAnswer = expectedAnswer;
    }

    @Override
    public String getType() {
        return "TRUE_FALSE";
    }

    /** @return expected truth value, {@code null} while undefined */
    public Boolean getExpectedAnswer() {
        return expectedAnswer;
    }

    /** @param expectedAnswer expected truth value */
    public void setExpectedAnswer(Boolean expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }

    @Override
    protected void addTypeSpecificViolations(List<String> violations) {
        if (expectedAnswer == null) {
            violations.add("A true/false question must declare its expected truth value.");
        }
    }
}
