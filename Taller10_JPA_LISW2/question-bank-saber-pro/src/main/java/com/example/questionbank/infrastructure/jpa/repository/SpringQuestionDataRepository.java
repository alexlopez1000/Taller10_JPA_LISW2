package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA adapter used internally by {@code QuestionRepositoryJpa}.
 * It never leaks to the application layer, which only knows the domain
 * contract.
 */
public interface SpringQuestionDataRepository extends JpaRepository<QuestionRecord, Long> {

    /**
     * Lists the questions written by an author.
     *
     * @param authorId identity of the author
     * @return questions authored by that user
     */
    @Query("select q from QuestionRecord q where q.author.id = :authorId")
    List<QuestionRecord> findByAuthorId(@Param("authorId") Long authorId);

    /**
     * Lists the questions in a lifecycle state.
     *
     * @param status state to filter by
     * @return questions in that state
     */
    List<QuestionRecord> findByStatus(QuestionStatus status);

    /**
     * Lists PUBLISHED questions that evaluate a given competency through the
     * associative entity {@code QuestionCompetencyRecord}.
     *
     * @param competencyId identity of the competency
     * @param status       expected lifecycle state (PUBLISHED)
     * @return published questions contributing to the competency
     */
    @Query("select distinct q from QuestionRecord q join q.competencyContributions qc "
        + "where qc.competency.id = :competencyId and q.status = :status")
    List<QuestionRecord> findPublishedByCompetency(@Param("competencyId") Long competencyId,
                                                   @Param("status") QuestionStatus status);
}
