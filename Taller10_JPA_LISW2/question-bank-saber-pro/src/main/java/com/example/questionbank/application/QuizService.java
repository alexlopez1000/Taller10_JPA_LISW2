package com.example.questionbank.application;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.QuizRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Use cases for building quizzes (simulacros). This service coordinates
 * repositories and transactions; the Quiz domain object enforces that only
 * PUBLISHED questions may be included.
 */
@Service
@Transactional
public class QuizService {

    private final QuizRepository quizzes;
    private final QuestionRepository questions;
    private final UserRepository users;

    /**
     * Creates the service with its repository contracts.
     *
     * @param quizzes   quiz contract
     * @param questions question contract
     * @param users     user contract
     */
    public QuizService(QuizRepository quizzes, QuestionRepository questions, UserRepository users) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.users = users;
    }

    /**
     * Use case 5: create a quiz. The Quiz domain object rejects any selected
     * question that is not PUBLISHED, aborting the transactional operation.
     *
     * @param title       quiz title
     * @param description purpose of the quiz
     * @param creatorId   identity of the teacher
     * @param questionIds identities of the questions to include
     * @return the created quiz
     */
    public Quiz createQuiz(String title, String description, Long creatorId, List<Long> questionIds) {
        User creator = loadCreator(creatorId);
        List<Question> selected = loadPublishedQuestions(questionIds);
        Quiz quiz = new Quiz(null, title, description, creator, LocalDateTime.now());
        quiz.replaceQuestions(selected);
        return quizzes.save(quiz);
    }

    /**
     * Adds one question to an existing quiz; the domain validates its status.
     *
     * @param quizId     identity of the quiz
     * @param questionId identity of the question
     * @return the updated quiz
     */
    public Quiz addQuestion(Long quizId, Long questionId) {
        Quiz quiz = quizzes.findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));
        Question question = questions.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        quiz.addQuestion(question);
        return quizzes.save(quiz);
    }

    /**
     * Use case 5 (query): find one quiz with its questions.
     *
     * @param id quiz identity
     * @return the quiz
     */
    @Transactional(readOnly = true)
    public Quiz getQuiz(Long id) {
        return quizzes.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + id));
    }

    /**
     * Use case 5 (query): list all quizzes.
     *
     * @return all registered quizzes
     */
    @Transactional(readOnly = true)
    public List<Quiz> listQuizzes() {
        return quizzes.findAll();
    }

    /** Loads the creator and checks the TEACHER privilege. */
    private User loadCreator(Long creatorId) {
        User creator = users.findById(creatorId)
            .orElseThrow(() -> new IllegalArgumentException("Creator not found: " + creatorId));
        if (!creator.hasAnyRole(Role.TEACHER, Role.ADMIN)) {
            throw new IllegalStateException("User " + creatorId + " does not have the TEACHER role.");
        }
        return creator;
    }

    /**
     * Loads every requested question. The Quiz domain object validates that
     * each selected question is PUBLISHED before accepting it.
     */
    private List<Question> loadPublishedQuestions(List<Long> questionIds) {
        List<Question> selected = new ArrayList<>();
        for (Long id : questionIds == null ? List.<Long>of() : questionIds) {
            Question question = questions.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
            selected.add(question);
        }
        return selected;
    }
}
