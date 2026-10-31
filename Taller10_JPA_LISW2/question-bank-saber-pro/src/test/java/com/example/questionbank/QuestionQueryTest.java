package com.example.questionbank;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of the repository query methods required by the laboratory: questions
 * by status, by author and published questions filtered by competency.
 */
class QuestionQueryTest extends AbstractIntegrationTest {

    @Test
    void test18FindPublishedByCompetency() {
        Question publishedA = createPublishedMultipleChoice(); // competencyId at 100
        Question publishedB = createPublishedMultipleChoice();
        Question draftA = createClassifiedMultipleChoiceDraft(); // same competency, still draft

        // Another published question linked to the second competency only.
        Question other = createClassifiedMultipleChoiceDraft();
        questionService.classifyQuestion(other.getId(), topicId, List.of(
            new com.example.questionbank.application.QuestionService.CompetencyWeight(secondCompetencyId,
                new java.math.BigDecimal("100"))));
        publishQuestion(other);

        List<Question> result = questionRepository.findPublishedByCompetency(competencyId);

        assertEquals(2, result.size(), "Only the two published questions of the competency are returned");
        assertTrue(result.stream().allMatch(q -> q.getStatus() == QuestionStatus.PUBLISHED));
        assertTrue(result.stream().anyMatch(q -> q.getId().equals(publishedA.getId())));
        assertTrue(result.stream().anyMatch(q -> q.getId().equals(publishedB.getId())));
    }

    @Test
    void test19FindQuestionsByStatusAndByAuthor() {
        Question draft = createClassifiedMultipleChoiceDraft();
        createPublishedMultipleChoice();

        var drafts = questionRepository.findByStatus(QuestionStatus.DRAFT);
        var published = questionRepository.findByStatus(QuestionStatus.PUBLISHED);
        var byAuthor = questionRepository.findByAuthorId(authorId);

        assertEquals(1, drafts.size(), "Only the explicit draft remains in DRAFT");
        assertEquals(1, published.size(), "The published question is the only one in PUBLISHED");
        assertEquals(2, byAuthor.size(), "Both questions belong to the seeded author");
        assertTrue(drafts.stream().allMatch(q -> q.getStatus() == QuestionStatus.DRAFT));
    }
}
