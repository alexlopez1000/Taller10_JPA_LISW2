package com.example.questionbank;

import com.example.questionbank.application.QuestionService;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persistence round-trip tests: entities must be saved and recovered
 * correctly, and the JOINED inheritance strategy must materialize the shared
 * columns in the base table and the specific columns in the subtype tables.
 */
class QuestionPersistenceTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager entityManager;

    @Test
    void test01PersistAndReloadMultipleChoiceQuestion() {
        Question saved = createClassifiedMultipleChoiceDraft();

        // Force the reload below to read from the database, not from the first-level cache.
        entityManager.flush();
        entityManager.clear();

        Question reloaded = questionService.getQuestion(saved.getId());

        assertInstanceOf(MultipleChoiceQuestion.class, reloaded);
        MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) reloaded;
        assertNotNull(mcq.getId());
        assertTrue(mcq.getStatement().startsWith("What does the JOINED"));
        assertEquals(5, mcq.getOptions().size());
        assertEquals(1, mcq.countCorrectOptions());
        assertEquals(3, mcq.getMetadata().getEstimatedTimeMinutes());
        assertEquals("UNDERSTAND", mcq.getMetadata().getCognitiveLevel().name());
        assertEquals("Arquitectura de software", mcq.getTopic().getName());
        assertEquals("Laura Restrepo", mcq.getAuthor().getFullName());
        assertEquals(1, mcq.getCompetencyContributions().size());
        assertEquals(0, mcq.getCompetencyContributions().get(0).getContributionWeight()
            .compareTo(new java.math.BigDecimal("100")));
    }

    @Test
    void test02PersistAndReloadTrueFalseQuestion() {
        Question created = questionService.createQuestion(authorId,
            "A many-to-many association always requires an associative entity in JPA.",
            Difficulty.LOW, "TRUE_FALSE", false, false, List.of(),
            new QuestionService.NewMetadata(QuestionMetadata.CognitiveLevel.REMEMBER, 2, "Quick check item"));

        // Force the reload below to read from the database, not from the first-level cache.
        entityManager.flush();
        entityManager.clear();

        Question reloaded = questionService.getQuestion(created.getId());

        assertInstanceOf(TrueFalseQuestion.class, reloaded);
        assertEquals(Boolean.FALSE, ((TrueFalseQuestion) reloaded).getExpectedAnswer());
        assertEquals("TRUE_FALSE", reloaded.getType());
        assertEquals("Laura Restrepo", reloaded.getAuthor().getFullName());
    }

    @Test
    void test03JOINEDStrategyCreatesBaseAndSubtypeTables() {
        createClassifiedMultipleChoiceDraft();
        questionService.createQuestion(authorId,
            "A many-to-many association always requires an associative entity in JPA.",
            Difficulty.LOW, "TRUE_FALSE", false, true, List.of(),
            new QuestionService.NewMetadata(QuestionMetadata.CognitiveLevel.REMEMBER, 2, null));

        Integer baseRows = jdbcTemplate.queryForObject("select count(*) from questions", Integer.class);
        Integer mcqRows = jdbcTemplate.queryForObject(
            "select count(*) from multiple_choice_questions", Integer.class);
        Integer tfRows = jdbcTemplate.queryForObject(
            "select count(*) from true_false_questions", Integer.class);

        entityManager.flush(); // make sure pending changes hit the database before counting
        assertEquals(2, baseRows, "The questions table holds both subtypes");
        assertEquals(1, mcqRows, "Only the multiple choice question lives in its subtype table");
        assertEquals(1, tfRows, "Only the true/false question lives in its subtype table");
    }

    @Test
    void test04DeleteQuestionCascadesToOwnedData() {
        Question draft = createClassifiedMultipleChoiceDraft();
        Long questionId = draft.getId();

        entityManager.flush();
        Integer optionsBefore = jdbcTemplate.queryForObject(
            "select count(*) from answer_options where question_id = ?", Integer.class, questionId);
        assertEquals(5, optionsBefore);

        questionRepository.deleteById(questionId);
        entityManager.flush(); // cascade deletes run during flush

        Integer optionsAfter = jdbcTemplate.queryForObject(
            "select count(*) from answer_options where question_id = ?", Integer.class, questionId);
        Integer metadataAfter = jdbcTemplate.queryForObject(
            "select count(*) from question_metadata where id = ?", Integer.class,
            draft.getMetadata().getId());
        Integer contributionsAfter = jdbcTemplate.queryForObject(
            "select count(*) from question_competencies where question_id = ?", Integer.class, questionId);
        assertEquals(0, optionsAfter, "Options are removed with their question");
        assertEquals(0, metadataAfter, "Metadata is removed with its question");
        assertEquals(0, contributionsAfter, "Competency contributions are removed with their question");
    }
}
