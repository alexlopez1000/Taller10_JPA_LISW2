package com.example.questionbank.api;

import com.example.questionbank.application.QuestionService;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST endpoints for the question use cases: creation, consultation,
 * classification and submission to review. Responses are transport records so
 * domain objects never cross the API boundary.
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService service;

    /** Creates the controller. @param service question use cases */
    public QuestionController(QuestionService service) {
        this.service = service;
    }

    /** Request body for creating a question. */
    public record QuestionRequest(String statement, String difficulty, String type, Long authorId,
                                  Boolean allowMultipleSelection, Boolean expectedAnswer,
                                  List<OptionRequest> options, MetadataRequest metadata) {
    }

    /** Request body for one option. */
    public record OptionRequest(String text, boolean correct, String explanation) {
    }

    /** Request body for the editorial metadata. */
    public record MetadataRequest(String cognitiveLevel, Integer estimatedTimeMinutes, String editorialNotes) {
    }

    /** Request body for classification. */
    public record ClassifyRequest(Long topicId, List<CompetencyWeightRequest> competencies) {
    }

    /** Request body for one competency weight. */
    public record CompetencyWeightRequest(Long competencyId, BigDecimal weight) {
    }

    /** Response with the full question data. */
    public record QuestionResponse(Long id, String type, String statement, String difficulty, String status,
                                   Long authorId, String authorName, Long topicId, String topicName,
                                   MetadataResponse metadata, List<OptionResponse> options,
                                   List<CompetencyResponse> competencies) {

        /** Builds the response from a domain question. */
        public static QuestionResponse from(Question question) {
            MetadataResponse metadataResponse = null;
            if (question.getMetadata() != null) {
                metadataResponse = new MetadataResponse(question.getMetadata().getCognitiveLevel().name(),
                    question.getMetadata().getEstimatedTimeMinutes(),
                    question.getMetadata().getLastReviewedAt() == null ? null
                        : question.getMetadata().getLastReviewedAt().toString(),
                    question.getMetadata().getEditorialNotes());
            }
            List<OptionResponse> optionResponses = question instanceof MultipleChoiceQuestion mcq
                ? mcq.getOptions().stream().map(o -> new OptionResponse(o.getId(), o.getOptionText(), o.isCorrect(),
                    o.getExplanation())).toList()
                : List.of();
            List<CompetencyResponse> competencyResponses = question.getCompetencyContributions().stream()
                .map(c -> new CompetencyResponse(c.getCompetency().getId(), c.getCompetency().getCode(),
                    c.getContributionWeight())).toList();
            return new QuestionResponse(question.getId(), question.getType(), question.getStatement(),
                question.getDifficulty().name(), question.getStatus().name(),
                question.getAuthor() != null ? question.getAuthor().getId() : null,
                question.getAuthor() != null ? question.getAuthor().getFullName() : null,
                question.getTopic() != null ? question.getTopic().getId() : null,
                question.getTopic() != null ? question.getTopic().getName() : null,
                metadataResponse, optionResponses, competencyResponses);
        }
    }

    /** Response for the metadata part. */
    public record MetadataResponse(String cognitiveLevel, Integer estimatedTimeMinutes, String lastReviewedAt,
                                   String editorialNotes) {
    }

    /** Response for one option. */
    public record OptionResponse(Long id, String text, boolean correct, String explanation) {
    }

    /** Response for one competency contribution. */
    public record CompetencyResponse(Long competencyId, String code, BigDecimal weight) {
    }

    /** Creates a question (use case 1). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse create(@RequestBody QuestionRequest request) {
        Question question = service.createQuestion(request.authorId(), request.statement(),
            Difficulty.valueOf(request.difficulty()), request.type(),
            Boolean.TRUE.equals(request.allowMultipleSelection()), request.expectedAnswer(),
            request.options() == null ? List.of()
                : request.options().stream()
                    .map(o -> new QuestionService.NewOption(o.text(), o.correct(), o.explanation())).toList(),
            request.metadata() == null ? null
                : new QuestionService.NewMetadata(
                    com.example.questionbank.domain.model.QuestionMetadata.CognitiveLevel
                        .valueOf(request.metadata().cognitiveLevel()),
                    request.metadata().estimatedTimeMinutes(), request.metadata().editorialNotes()));
        return QuestionResponse.from(question);
    }

    /** Lists questions, optionally filtered by status. */
    @GetMapping
    public List<QuestionResponse> list(@RequestParam(required = false) QuestionStatus status) {
        List<Question> questions = status == null ? service.listQuestions() : service.listQuestionsByStatus(status);
        return questions.stream().map(QuestionResponse::from).toList();
    }

    /** Lists the questions of one author. */
    @GetMapping("/author/{authorId}")
    public List<QuestionResponse> byAuthor(@PathVariable Long authorId) {
        return service.listQuestionsByAuthor(authorId).stream().map(QuestionResponse::from).toList();
    }

    /** Lists published questions of one competency. */
    @GetMapping("/published-by-competency/{competencyId}")
    public List<QuestionResponse> publishedByCompetency(@PathVariable Long competencyId) {
        return service.listPublishedByCompetency(competencyId).stream().map(QuestionResponse::from).toList();
    }

    /** Finds one question by identity. */
    @GetMapping("/{id}")
    public QuestionResponse get(@PathVariable Long id) {
        return QuestionResponse.from(service.getQuestion(id));
    }

    /** Classifies a question with a topic and competencies (use case 3). */
    @PostMapping("/{id}/classify")
    public QuestionResponse classify(@PathVariable Long id, @RequestBody ClassifyRequest request) {
        List<QuestionService.CompetencyWeight> weights = request.competencies() == null ? List.of()
            : request.competencies().stream()
                .map(c -> new QuestionService.CompetencyWeight(c.competencyId(), c.weight())).toList();
        return QuestionResponse.from(service.classifyQuestion(id, request.topicId(), weights));
    }

    /** Submits the question to review (use case 4). */
    @PostMapping("/{id}/submit-review")
    public QuestionResponse submitForReview(@PathVariable Long id) {
        return QuestionResponse.from(service.submitForReview(id));
    }
}
