package com.example.questionbank.application;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionCompetency;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Use cases for creating, consulting and classifying questions, including the
 * submission to review that opens the editorial lifecycle.
 *
 * <p>The service depends only on repository contracts; it never touches JPA
 * types. Every state-changing use case runs inside a transaction so either the
 * question and its associations change together, or nothing changes.</p>
 */
@Service
@Transactional
public class QuestionService {

    /** Input for one answer option of a multiple choice question. */
    public record NewOption(String text, boolean correct, String explanation) {
    }

    /** Input for the one-to-one editorial metadata of a question. */
    public record NewMetadata(QuestionMetadata.CognitiveLevel cognitiveLevel, Integer estimatedTimeMinutes,
                              String editorialNotes) {
    }

    /** Input for the weight of one competency inside a question. */
    public record CompetencyWeight(Long competencyId, BigDecimal weight) {
    }

    private final QuestionRepository questions;
    private final UserRepository users;
    private final TopicRepository topics;
    private final CompetencyRepository competencies;

    /**
     * Creates the service with its repository contracts.
     *
     * @param questions    question contract
     * @param users        user contract
     * @param topics       topic contract
     * @param competencies competency contract
     */
    public QuestionService(QuestionRepository questions, UserRepository users, TopicRepository topics,
                           CompetencyRepository competencies) {
        this.questions = questions;
        this.users = users;
        this.topics = topics;
        this.competencies = competencies;
    }

    /**
     * Use case 1: create a question with its statement, author, metadata,
     * type and options when applicable. The question starts in DRAFT state.
     *
     * @param authorId               identity of the author
     * @param statement              wording of the question
     * @param difficulty             cognitive difficulty
     * @param type                   "MULTIPLE_CHOICE" or "TRUE_FALSE"
     * @param allowMultipleSelection whether several options may be selected (MCQ only)
     * @param expectedAnswer         expected truth value (true/false only)
     * @param options                options of a multiple choice question
     * @param metadataInput          editorial metadata of the question
     * @return the created question with its persistent identity
     */
    public Question createQuestion(Long authorId, String statement, Difficulty difficulty, String type,
                                   boolean allowMultipleSelection, Boolean expectedAnswer,
                                   List<NewOption> options, NewMetadata metadataInput) {
        User author = loadAuthor(authorId);
        QuestionMetadata metadata = buildMetadata(metadataInput);
        Question question;
        if ("MULTIPLE_CHOICE".equals(type)) {
            question = new MultipleChoiceQuestion(null, statement, difficulty, author, metadata,
                allowMultipleSelection);
            List<AnswerOption> domainOptions = new ArrayList<>();
            for (NewOption option : options == null ? List.<NewOption>of() : options) {
                domainOptions.add(new AnswerOption(null, option.text(), option.correct(), option.explanation()));
            }
            ((MultipleChoiceQuestion) question).replaceOptions(domainOptions);
        } else if ("TRUE_FALSE".equals(type)) {
            question = new TrueFalseQuestion(null, statement, difficulty, author, metadata, expectedAnswer);
        } else {
            throw new IllegalArgumentException("Unknown question type: " + type);
        }
        return questions.save(question);
    }

    /**
     * Use case 2 (query): find one question by identity.
     *
     * @param id question identity
     * @return the question
     */
    @Transactional(readOnly = true)
    public Question getQuestion(Long id) {
        return questions.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
    }

    /**
     * Use case 2 (query): list all questions.
     *
     * @return all registered questions
     */
    @Transactional(readOnly = true)
    public List<Question> listQuestions() {
        return questions.findAll();
    }

    /**
     * Use case 2 (query): list questions of one author.
     *
     * @param authorId identity of the author
     * @return questions authored by that user
     */
    @Transactional(readOnly = true)
    public List<Question> listQuestionsByAuthor(Long authorId) {
        return questions.findByAuthorId(authorId);
    }

    /**
     * Use case 2 (query): list questions in one lifecycle state.
     *
     * @param status state to filter by
     * @return questions in that state
     */
    @Transactional(readOnly = true)
    public List<Question> listQuestionsByStatus(QuestionStatus status) {
        return questions.findByStatus(status);
    }

    /**
     * Use case 3: classify a question with a topic and a set of competencies
     * whose contribution weights must total exactly 100 percent.
     *
     * @param questionId identity of the question
     * @param topicId    identity of the topic to assign
     * @param weights    competencies with their contribution percentage
     * @return the classified question
     */
    public Question classifyQuestion(Long questionId, Long topicId, List<CompetencyWeight> weights) {
        Question question = questions.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        Topic topic = topics.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicId));
        List<QuestionCompetency> contributions = buildContributions(weights);
        question.classify(topic, contributions);
        return questions.save(question);
    }

    /**
     * Use case 4: submit a question for review. The domain validates the
     * minimum conditions; only a complete DRAFT question may move to
     * PENDING_REVIEW.
     *
     * @param questionId identity of the question
     * @return the question in PENDING_REVIEW state
     */
    public Question submitForReview(Long questionId) {
        Question question = questions.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        List<String> violations = question.validateForSubmission();
        if (!violations.isEmpty()) {
            throw new IllegalStateException("The question cannot be submitted: " + violations);
        }
        question.changeStatus(QuestionStatus.PENDING_REVIEW);
        return questions.save(question);
    }

    /**
     * Supporting query for quiz building: published questions of a competency.
     *
     * @param competencyId identity of the competency
     * @return published questions contributing to that competency
     */
    @Transactional(readOnly = true)
    public List<Question> listPublishedByCompetency(Long competencyId) {
        return questions.findPublishedByCompetency(competencyId);
    }

    /** Loads the author and checks that the user may write questions. */
    private User loadAuthor(Long authorId) {
        User author = users.findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("Author not found: " + authorId));
        if (!author.hasAnyRole(Role.AUTHOR, Role.ADMIN)) {
            throw new IllegalStateException("User " + authorId + " does not have the AUTHOR role.");
        }
        return author;
    }

    /** Builds the metadata domain object from the use case input. */
    private QuestionMetadata buildMetadata(NewMetadata input) {
        if (input == null || input.cognitiveLevel() == null || input.estimatedTimeMinutes() == null
            || input.estimatedTimeMinutes() <= 0) {
            throw new IllegalArgumentException(
                "The question requires metadata with a cognitive level and a positive estimated time.");
        }
        return new QuestionMetadata(null, input.cognitiveLevel(), input.estimatedTimeMinutes(), null,
            input.editorialNotes());
    }

    /**
     * Builds the competency contributions, validating that every referenced
     * competency exists and that the weights total 100 percent.
     */
    private List<QuestionCompetency> buildContributions(List<CompetencyWeight> weights) {
        if (weights == null || weights.isEmpty()) {
            throw new IllegalArgumentException("At least one competency is required for classification.");
        }
        List<QuestionCompetency> contributions = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CompetencyWeight entry : weights) {
            Competency competency = competencies.findById(entry.competencyId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Competency not found: " + entry.competencyId()));
            contributions.add(new QuestionCompetency(null, null, competency, entry.weight()));
            total = total.add(entry.weight());
        }
        if (total.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalStateException(
                "Competency contribution weights must total 100 percent; found " + total + ".");
        }
        return contributions;
    }
}
