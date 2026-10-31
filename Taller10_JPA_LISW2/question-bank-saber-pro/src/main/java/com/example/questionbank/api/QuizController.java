package com.example.questionbank.api;

import com.example.questionbank.application.QuizService;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Quiz;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for quiz building (use case 5). The service guarantees that
 * only published questions enter a quiz.
 */
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService service;

    /** Creates the controller. @param service quiz use cases */
    public QuizController(QuizService service) {
        this.service = service;
    }

    /** Request body for creating a quiz. */
    public record QuizRequest(String title, String description, Long creatorId, List<Long> questionIds) {
    }

    /** Response for one question inside a quiz. */
    public record QuizQuestionResponse(Long id, String type, String statement, String difficulty, String status) {

        /** Builds the response from a domain question. */
        public static QuizQuestionResponse from(Question question) {
            return new QuizQuestionResponse(question.getId(), question.getType(), question.getStatement(),
                question.getDifficulty().name(), question.getStatus().name());
        }
    }

    /** Response with the full quiz data. */
    public record QuizResponse(Long id, String title, String description, Long creatorId, String creatorName,
                               String createdAt, List<QuizQuestionResponse> questions) {

        /** Builds the response from a domain quiz. */
        public static QuizResponse from(Quiz quiz) {
            return new QuizResponse(quiz.getId(), quiz.getTitle(), quiz.getDescription(),
                quiz.getCreator() != null ? quiz.getCreator().getId() : null,
                quiz.getCreator() != null ? quiz.getCreator().getFullName() : null,
                quiz.getCreatedAt() == null ? null : quiz.getCreatedAt().toString(),
                quiz.getQuestions().stream().map(QuizQuestionResponse::from).toList());
        }
    }

    /** Creates a quiz with published questions (use case 5). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse create(@RequestBody QuizRequest request) {
        return QuizResponse.from(service.createQuiz(request.title(), request.description(), request.creatorId(),
            request.questionIds()));
    }

    /** Lists all quizzes. */
    @GetMapping
    public List<QuizResponse> list() {
        return service.listQuizzes().stream().map(QuizResponse::from).toList();
    }

    /** Finds one quiz with its questions. */
    @GetMapping("/{id}")
    public QuizResponse get(@PathVariable Long id) {
        return QuizResponse.from(service.getQuiz(id));
    }

    /** Adds one published question to an existing quiz. */
    @PostMapping("/{id}/questions/{questionId}")
    public QuizResponse addQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        return QuizResponse.from(service.addQuestion(id, questionId));
    }
}
