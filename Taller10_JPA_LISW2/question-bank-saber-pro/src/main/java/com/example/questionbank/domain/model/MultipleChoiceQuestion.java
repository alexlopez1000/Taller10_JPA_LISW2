package com.example.questionbank.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A question answered by selecting one or several predefined options.
 *
 * <p>Academic rule of the system: a single-answer question is registered with
 * exactly five options (four distractors and one correct answer); a
 * multiple-selection question needs at least two options and at least one
 * correct answer. The options live in their own one-to-many collection.</p>
 */
public class MultipleChoiceQuestion extends Question {

    /** Exact number of options required for a single-answer question. */
    public static final int SINGLE_ANSWER_OPTION_COUNT = 5;

    private final List<AnswerOption> options = new ArrayList<>();
    private boolean allowMultipleSelection;

    /** Creates an empty question; used by repositories and mappers. */
    public MultipleChoiceQuestion() {
        super();
    }

    /**
     * Creates a multiple choice question in DRAFT state.
     *
     * @param id                     persistent identity, {@code null} before saving
     * @param statement              wording shown to the student
     * @param difficulty             cognitive difficulty
     * @param author                 user that wrote the question
     * @param metadata               one-to-one editorial metadata
     * @param allowMultipleSelection whether several options may be selected
     */
    public MultipleChoiceQuestion(Long id, String statement, Difficulty difficulty, User author,
                                  QuestionMetadata metadata, boolean allowMultipleSelection) {
        super(id, statement, difficulty, author, metadata);
        this.allowMultipleSelection = allowMultipleSelection;
    }

    @Override
    public String getType() {
        return "MULTIPLE_CHOICE";
    }

    /** @return whether several options may be selected */
    public boolean isAllowMultipleSelection() {
        return allowMultipleSelection;
    }

    /** @return read-only view of the answer options (one-to-many) */
    public List<AnswerOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    /**
     * Replaces the set of options; used when building or editing the question.
     *
     * @param newOptions options to set
     */
    public void replaceOptions(List<AnswerOption> newOptions) {
        this.options.clear();
        this.options.addAll(newOptions);
    }

    /** @return number of options marked as correct */
    public long countCorrectOptions() {
        return options.stream().filter(AnswerOption::isCorrect).count();
    }

    @Override
    protected void addTypeSpecificViolations(List<String> violations) {
        if (allowMultipleSelection) {
            if (options.size() < 2) {
                violations.add("A multiple-selection question needs at least two options.");
            }
            if (countCorrectOptions() < 1) {
                violations.add("A multiple-selection question needs at least one correct option.");
            }
        } else {
            if (options.size() != SINGLE_ANSWER_OPTION_COUNT) {
                violations.add("A single-answer question needs exactly " + SINGLE_ANSWER_OPTION_COUNT
                    + " options (four distractors and one correct answer); found " + options.size() + ".");
            }
            if (countCorrectOptions() != 1) {
                violations.add("A single-answer question must mark exactly one correct option.");
            }
        }
    }
}
