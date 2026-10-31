package com.example.questionbank;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Quiz;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of use case 5: quizzes may only contain PUBLISHED questions, and the
 * rule is enforced both at creation time and when adding questions later.
 */
class QuizServiceTest extends AbstractIntegrationTest {

    @Test
    void test15CreateQuizWithPublishedQuestions() {
        Question published = createPublishedMultipleChoice();

        Quiz quiz = quizService.createQuiz("Simulacro Saber Pro", "Practice exam", teacherId,
            List.of(published.getId()));

        Quiz reloaded = quizService.getQuiz(quiz.getId());
        assertEquals(1, reloaded.getQuestions().size());
        assertEquals("Claudia Munoz", reloaded.getCreator().getFullName());
        assertEquals(published.getId(), reloaded.getQuestions().get(0).getId());
    }

    @Test
    void test16QuizRejectsNotPublishedQuestions() {
        Question draft = createClassifiedMultipleChoiceDraft();
        Question approved = publishQuestion(createClassifiedMultipleChoiceDraft());
        reviewService.archive(approved.getId()); // ARCHIVED is not usable either

        var exception = assertThrows(IllegalStateException.class,
            () -> quizService.createQuiz("Simulacro invalido", "Contains drafts", teacherId,
                List.of(draft.getId(), approved.getId())));
        assertTrue(exception.getMessage().contains("published"),
            "The error explains that only published questions may join a quiz");

        assertEquals(0, quizService.listQuizzes().size(),
            "The quiz is not written when the rule fails (transactional use case)");
    }

    @Test
    void test17AddingDraftQuestionToQuizFails() {
        Question published = createPublishedMultipleChoice();
        Quiz quiz = quizService.createQuiz("Simulacro vigente", "Exam in force", teacherId,
            List.of(published.getId()));
        Question draft = createClassifiedMultipleChoiceDraft();

        assertThrows(IllegalStateException.class, () -> quizService.addQuestion(quiz.getId(), draft.getId()));

        assertEquals(1, quizService.getQuiz(quiz.getId()).getQuestions().size(),
            "The quiz keeps only its published questions");
    }
}
