package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;

import java.util.List;
import java.util.Optional;

/**
 * Technology-independent persistence contract for questions.
 *
 * <p>The application layer depends only on this interface; Spring Data JPA
 * appears exclusively in the infrastructure implementation.</p>
 */
public interface QuestionRepository {

    /**
     * Stores a new question or updates the existing one.
     *
     * @param question question to store
     * @return stored question with its persistent identity
     */
    Question save(Question question);

    /**
     * Finds a question by identity.
     *
     * @param id persistent identity
     * @return the question, or empty when it does not exist
     */
    Optional<Question> findById(Long id);

    /** @return all registered questions */
    List<Question> findAll();

    /**
     * Lists the questions written by an author.
     *
     * @param authorId identity of the author
     * @return questions authored by that user
     */
    List<Question> findByAuthorId(Long authorId);

    /**
     * Lists the questions in a given lifecycle state.
     *
     * @param status lifecycle state to filter by
     * @return questions currently in that state
     */
    List<Question> findByStatus(QuestionStatus status);

    /**
     * Lists PUBLISHED questions that evaluate a given competency; this is the
     * query that feeds quiz building.
     *
     * @param competencyId identity of the competency
     * @return published questions contributing to the competency
     */
    List<Question> findPublishedByCompetency(Long competencyId);

    /**
     * Deletes a question together with its owned data (options, metadata and
     * competency contributions).
     *
     * @param id persistent identity
     */
    void deleteById(Long id);
}
