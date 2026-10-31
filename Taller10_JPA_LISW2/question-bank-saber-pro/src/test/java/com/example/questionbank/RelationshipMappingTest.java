package com.example.questionbank;

import com.example.questionbank.application.QuestionService;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionCompetency;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.repository.QuestionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the four relationship types: the unique constraint of the
 * one-to-one, the mandatory many-to-one foreign keys, the many-to-many join
 * table of quizzes and the associative entity between questions and
 * competencies.
 */
class RelationshipMappingTest extends AbstractIntegrationTest {

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void test05MetadataUniqueConstraintPreventsSharing() {
        Question owner = createClassifiedMultipleChoiceDraft();

        // A second question that tries to reference the same metadata record.
        QuestionMetadata shared = new QuestionMetadata(owner.getMetadata().getId(),
            QuestionMetadata.CognitiveLevel.APPLY, 2, null, "trying to share");
        Question duplicate = new MultipleChoiceQuestion(null, "Another question trying to reuse the same metadata.",
            Difficulty.LOW, owner.getAuthor(), shared, false);
        ((MultipleChoiceQuestion) duplicate).replaceOptions(
            List.of(new com.example.questionbank.domain.model.AnswerOption(null, "Only A", true, null)));

        assertThrows(DataIntegrityViolationException.class, () -> {
            questionRepository.save(duplicate);
            entityManager.flush();
        }, "The unique constraint on questions.metadata_id forbids sharing metadata");
    }

    @Test
    void test06QuestionWithUnknownAuthorRejected() {
        // 1) Database level: even bypassing the repository, the schema rejects invalid references.
        Question stored = createClassifiedMultipleChoiceDraft();
        entityManager.flush();
        assertThrows(DataIntegrityViolationException.class,
            () -> jdbcTemplate.update("update questions set author_id = ? where id = ?", 9999L, stored.getId()),
            "questions.author_id is a foreign key to users");
        assertThrows(DataIntegrityViolationException.class,
            () -> jdbcTemplate.update("update questions set topic_id = ? where id = ?", 9999L, stored.getId()),
            "questions.topic_id is a foreign key to topics");

        // 2) Repository level: the adapter resolves the author before persisting.
        QuestionMetadata metadata = new QuestionMetadata(null, QuestionMetadata.CognitiveLevel.APPLY, 4, null, null);
        Question orphan = new MultipleChoiceQuestion(null, "A question whose author does not exist at all.",
            Difficulty.MEDIUM, new com.example.questionbank.domain.model.User(9999L, "Ghost", "ghost@x.com", null),
            metadata, false);

        assertThrows(org.springframework.dao.InvalidDataAccessApiUsageException.class,
            () -> questionRepository.save(orphan),
            "The repository must resolve the author before persisting (Spring translates "
                + "the IllegalArgumentException thrown by the adapter)");
    }

    @Test
    void test07ManyToManyQuizJoinTableStoresBothDirections() {
        Question first = createPublishedMultipleChoice();
        Question second = createPublishedMultipleChoice();

        Quiz quiz = quizService.createQuiz("Simulacro parcial 1", "Mock exam for week 6", teacherId,
            List.of(first.getId(), second.getId()));
        Quiz otherQuiz = quizService.createQuiz("Simulacro final", "Final mock exam", teacherId,
            List.of(second.getId()));

        Quiz reloaded = quizService.getQuiz(quiz.getId());
        assertEquals(2, reloaded.getQuestions().size());
        Quiz otherReloaded = quizService.getQuiz(otherQuiz.getId());
        assertEquals(1, otherReloaded.getQuestions().size());
        assertTrue(otherReloaded.getQuestions().stream().anyMatch(q -> q.getId().equals(second.getId())),
            "A question may belong to several quizzes (many-to-many both ways)");
    }

    @Test
    void test08QuestionCompetencyWeightsPersisted() {
        Question draft = createClassifiedMultipleChoiceDraft();

        entityManager.flush();
        entityManager.clear(); // read back from the database, not from the first-level cache
        Question reloaded = questionService.getQuestion(draft.getId());
        List<QuestionCompetency> contributions = reloaded.getCompetencyContributions();
        assertEquals(1, contributions.size());
        assertEquals(competencyId, contributions.get(0).getCompetency().getId());
        assertNotNull(contributions.get(0).getId(), "The associative row got its identity");

        questionService.classifyQuestion(draft.getId(), topicId, List.of(
            new QuestionService.CompetencyWeight(competencyId, new BigDecimal("60")),
            new QuestionService.CompetencyWeight(secondCompetencyId, new BigDecimal("40"))));

        entityManager.flush();
        entityManager.clear();
        Question reclassified = questionService.getQuestion(draft.getId());
        assertEquals(2, reclassified.getCompetencyContributions().size());
        BigDecimal total = reclassified.getCompetencyContributions().stream()
            .map(QuestionCompetency::getContributionWeight)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, total.compareTo(new BigDecimal("100")), "Weights still total 100 percent");
    }
}
