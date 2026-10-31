package com.example.questionbank;

import com.example.questionbank.application.CatalogService;
import com.example.questionbank.application.QuestionService;
import com.example.questionbank.application.QuizService;
import com.example.questionbank.application.ReviewService;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Shared fixture for the integration tests: seeds catalog data inside a
 * transaction that is rolled back after every test, and offers factories for
 * questions in several lifecycle states.
 */
@SpringBootTest
@Transactional
public abstract class AbstractIntegrationTest {

    /** Author used by the factories. */
    protected Long authorId;
    /** Reviewer used by the factories. */
    protected Long reviewerId;
    /** Teacher used by the factories. */
    protected Long teacherId;
    /** Topic used by the factories. */
    protected Long topicId;
    /** Competency used by the factories. */
    protected Long competencyId;
    /** Second competency used when several competencies are needed. */
    protected Long secondCompetencyId;

    @Autowired
    protected QuestionService questionService;
    @Autowired
    protected ReviewService reviewService;
    @Autowired
    protected QuizService quizService;
    @Autowired
    protected CatalogService catalogService;
    @Autowired
    protected QuestionRepository questionRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected EntityManager entityManager;

    /** Seeds users, topics and competencies for every test. */
    @org.junit.jupiter.api.BeforeEach
    void seedCatalog() {
        authorId = catalogService.createUser("Laura Restrepo", "laura.author@example.com",
            Set.of(Role.AUTHOR), "Ingenieria de Sistemas", "3001112233").getId();
        reviewerId = catalogService.createUser("Raul Gomez", "raul.reviewer@example.com",
            Set.of(Role.REVIEWER), "Ingenieria de Sistemas", null).getId();
        teacherId = catalogService.createUser("Claudia Munoz", "claudia.teacher@example.com",
            Set.of(Role.TEACHER), "Licenciatura en Matematicas", null).getId();
        topicId = catalogService.createTopic("Arquitectura de software",
            "Design patterns, layered architecture and persistence").getId();
        competencyId = catalogService.createCompetency("COMP-DIS", "Diseno de soluciones de software",
            "Builds software solutions from requirements").getId();
        secondCompetencyId = catalogService.createCompetency("COMP-ANA", "Analisis de problemas",
            "Analyzes problems and models domain concepts").getId();
    }

    /** @return five options with exactly one correct answer (index B). */
    protected List<QuestionService.NewOption> singleAnswerOptions() {
        return List.of(
            new QuestionService.NewOption("Option A: a wrong distractor", false, "Review the JOINED tables."),
            new QuestionService.NewOption("Option B: base and subtype tables", true, "Correct: JOINED splits columns."),
            new QuestionService.NewOption("Option C: a single table", false, "That describes SINGLE_TABLE."),
            new QuestionService.NewOption("Option D: one table per class", false, "That describes TABLE_PER_CLASS."),
            new QuestionService.NewOption("Option E: no tables at all", false, "JPA always needs tables."));
    }

    /**
     * Creates a classified multiple choice draft: statement, author, metadata,
     * five options, one topic and one competency at 100 percent.
     */
    protected Question createClassifiedMultipleChoiceDraft() {
        Question created = questionService.createQuestion(authorId,
            "What does the JOINED inheritance strategy produce in the relational schema of the bank?",
            Difficulty.MEDIUM, "MULTIPLE_CHOICE", false, null, singleAnswerOptions(),
            new QuestionService.NewMetadata(QuestionMetadata.CognitiveLevel.UNDERSTAND, 3, null));
        questionService.classifyQuestion(created.getId(), topicId,
            List.of(new QuestionService.CompetencyWeight(competencyId, new BigDecimal("100"))));
        return questionService.getQuestion(created.getId());
    }

    /**
     * Takes a draft through the full editorial flow: submit, start review,
     * approve and publish.
     */
    protected Question publishQuestion(Question draft) {
        questionService.submitForReview(draft.getId());
        reviewService.startReview(draft.getId(), reviewerId);
        reviewService.approve(draft.getId(), reviewerId, "Clear statement and options.");
        return reviewService.publish(draft.getId());
    }

    /** @return a published multiple choice question ready for quizzes. */
    protected Question createPublishedMultipleChoice() {
        return publishQuestion(createClassifiedMultipleChoiceDraft());
    }

    /** @return the current stored status of a question. */
    protected QuestionStatus statusOf(Long questionId) {
        return questionService.getQuestion(questionId).getStatus();
    }
}
