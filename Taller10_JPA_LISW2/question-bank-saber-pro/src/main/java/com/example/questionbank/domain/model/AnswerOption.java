package com.example.questionbank.domain.model;

/**
 * One answer option of a multiple choice question.
 *
 * <p>An option always belongs to exactly one question (many-to-one from the
 * option point of view). When the question is deleted its options are deleted
 * with it, which is expressed in the persistence layer through cascade and
 * orphan removal on the owning collection.</p>
 */
public class AnswerOption {

    private Long id;
    private String optionText;
    private boolean correct;
    private String explanation;

    /** Creates an empty option; used by repositories and mappers. */
    public AnswerOption() {
    }

    /**
     * Creates an answer option.
     *
     * @param id          persistent identity, {@code null} before saving
     * @param optionText  text shown to the student
     * @param correct     whether this option is the expected answer
     * @param explanation feedback shown when the option is selected
     */
    public AnswerOption(Long id, String optionText, boolean correct, String explanation) {
        this.id = id;
        this.optionText = optionText;
        this.correct = correct;
        this.explanation = explanation;
    }

    /** @return persistent identity assigned by the repository */
    public Long getId() {
        return id;
    }

    /** @param id identifier assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return text shown to the student */
    public String getOptionText() {
        return optionText;
    }

    /** @return whether this option is the expected answer */
    public boolean isCorrect() {
        return correct;
    }

    /** @return feedback shown when the option is selected */
    public String getExplanation() {
        return explanation;
    }
}
