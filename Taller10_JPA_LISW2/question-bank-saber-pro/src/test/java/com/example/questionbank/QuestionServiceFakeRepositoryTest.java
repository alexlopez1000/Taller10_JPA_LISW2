package com.example.questionbank;

import com.example.questionbank.application.QuestionService;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionCompetency;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Demonstrates that the repository contracts can be exercised WITHOUT JPA:
 * the same {@link QuestionService} runs against plain in-memory
 * implementations of the domain contracts, with no Spring context and no
 * database. This is the decoupling promised by the layered architecture.
 */
class QuestionServiceFakeRepositoryTest {

    private QuestionService service;
    private InMemoryQuestionRepository questionRepo;
    private Long topicId;
    private Long competencyId;
    private Long authorId;

    /** Simple in-memory implementation of the question contract. */
    static class InMemoryQuestionRepository implements QuestionRepository {
        private final Map<Long, Question> store = new HashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public Question save(Question question) {
            if (question.getId() == null) {
                question.setId(sequence.incrementAndGet());
            }
            store.put(question.getId(), question);
            return question;
        }

        @Override
        public Optional<Question> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Question> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Question> findByAuthorId(Long authorId) {
            return store.values().stream().filter(q -> q.getAuthor() != null
                && authorId.equals(q.getAuthor().getId())).toList();
        }

        @Override
        public List<Question> findByStatus(QuestionStatus status) {
            return store.values().stream().filter(q -> q.getStatus() == status).toList();
        }

        @Override
        public List<Question> findPublishedByCompetency(Long competencyId) {
            return store.values().stream()
                .filter(q -> q.getStatus() == QuestionStatus.PUBLISHED)
                .filter(q -> q.getCompetencyContributions().stream()
                    .anyMatch(c -> competencyId.equals(c.getCompetency().getId())))
                .toList();
        }

        @Override
        public void deleteById(Long id) {
            store.remove(id);
        }
    }

    /** Simple in-memory implementation of the user contract. */
    static class InMemoryUserRepository implements UserRepository {
        private final Map<Long, User> store = new HashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public User save(User user) {
            if (user.getId() == null) {
                user.setId(sequence.incrementAndGet());
            }
            store.put(user.getId(), user);
            return user;
        }

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return store.values().stream().filter(u -> u.getEmail().equals(email)).findFirst();
        }
    }

    /** Simple in-memory implementation of the topic contract. */
    static class InMemoryTopicRepository implements TopicRepository {
        private final Map<Long, Topic> store = new HashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public Topic save(Topic topic) {
            if (topic.getId() == null) {
                topic.setId(sequence.incrementAndGet());
            }
            store.put(topic.getId(), topic);
            return topic;
        }

        @Override
        public Optional<Topic> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Topic> findAll() {
            return new ArrayList<>(store.values());
        }
    }

    /** Simple in-memory implementation of the competency contract. */
    static class InMemoryCompetencyRepository implements CompetencyRepository {
        private final Map<Long, Competency> store = new HashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public Competency save(Competency competency) {
            if (competency.getId() == null) {
                competency.setId(sequence.incrementAndGet());
            }
            store.put(competency.getId(), competency);
            return competency;
        }

        @Override
        public Optional<Competency> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Competency> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Optional<Competency> findByCode(String code) {
            return store.values().stream().filter(c -> c.getCode().equals(code)).findFirst();
        }
    }

    /** Builds the service on top of the fake repositories. */
    @BeforeEach
    void setUp() {
        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        InMemoryTopicRepository topicRepo = new InMemoryTopicRepository();
        InMemoryCompetencyRepository competencyRepo = new InMemoryCompetencyRepository();
        questionRepo = new InMemoryQuestionRepository();
        service = new QuestionService(questionRepo, userRepo, topicRepo, competencyRepo);

        authorId = userRepo.save(new User(null, "Ana Autora", "ana@example.com",
            new HashSet<>(Set.of(Role.AUTHOR)))).getId();
        topicId = topicRepo.save(new Topic(null, "Pruebas de software", "Unit and integration testing")).getId();
        competencyId = competencyRepo.save(new Competency(null, "COMP-PRUEB", "Verificacion y validacion",
            "Designs test strategies")).getId();
    }

    @Test
    void test20ServiceRunsEndToEndWithoutJpa() {
        Question created = service.createQuestion(authorId,
            "Which layer defines the repository contracts in the layered architecture?",
            Difficulty.MEDIUM, "TRUE_FALSE", false, true, List.of(),
            new QuestionService.NewMetadata(QuestionMetadata.CognitiveLevel.UNDERSTAND, 2, null));
        service.classifyQuestion(created.getId(), topicId,
            List.of(new QuestionService.CompetencyWeight(competencyId, new BigDecimal("100"))));

        Question classified = service.getQuestion(created.getId());
        assertEquals(QuestionStatus.DRAFT, classified.getStatus());

        Question submitted = service.submitForReview(created.getId());
        assertEquals(QuestionStatus.PENDING_REVIEW, submitted.getStatus());
        assertEquals(QuestionStatus.PENDING_REVIEW, questionRepo.findById(created.getId()).orElseThrow().getStatus());
    }

    @Test
    void test21ServiceRejectsIncompleteSubmissionWithoutJpa() {
        Question created = service.createQuestion(authorId,
            "An unclassified draft question.", Difficulty.LOW, "TRUE_FALSE", false, true, List.of(),
            new QuestionService.NewMetadata(QuestionMetadata.CognitiveLevel.REMEMBER, 1, null));

        assertThrows(IllegalStateException.class, () -> service.submitForReview(created.getId()));
        assertEquals(QuestionStatus.DRAFT, questionRepo.findById(created.getId()).orElseThrow().getStatus());
    }
}
