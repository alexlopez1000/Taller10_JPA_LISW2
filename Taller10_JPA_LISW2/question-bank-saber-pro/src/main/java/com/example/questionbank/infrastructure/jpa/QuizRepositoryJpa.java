package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.repository.QuizRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuizRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuizDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Concrete JPA implementation of the domain quiz contract. Saving a quiz
 * resolves the creator and every referenced question, and Hibernate materializes
 * the many-to-many rows in the {@code quiz_questions} join table.
 */
@Repository
public class QuizRepositoryJpa implements QuizRepository {

    private final SpringQuizDataRepository quizzes;
    private final SpringUserDataRepository users;
    private final SpringQuestionDataRepository questions;

    /**
     * Creates the adapter.
     *
     * @param quizzes   Spring Data repository for quiz records
     * @param users     Spring Data repository for creator resolution
     * @param questions Spring Data repository for question resolution
     */
    public QuizRepositoryJpa(SpringQuizDataRepository quizzes, SpringUserDataRepository users,
                             SpringQuestionDataRepository questions) {
        this.quizzes = quizzes;
        this.users = users;
        this.questions = questions;
    }

    @Override
    @Transactional
    public Quiz save(Quiz quiz) {
        if (quiz.getId() == null) {
            UserRecord creator = users.findById(quiz.getCreator().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Creator not found: " + quiz.getCreator().getId()));
            QuizRecord record = new QuizRecord(quiz.getTitle(), quiz.getDescription(), creator,
                quiz.getCreatedAt() != null ? quiz.getCreatedAt() : LocalDateTime.now());
            for (var question : quiz.getQuestions()) {
                record.getQuestions().add(resolveQuestion(question.getId()));
            }
            QuizRecord saved = quizzes.save(record);
            quiz.setId(saved.getId());
            quiz.setCreatedAt(saved.getCreatedAt());
            return quiz;
        }
        QuizRecord record = quizzes.findById(quiz.getId())
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quiz.getId()));
        for (var question : quiz.getQuestions()) {
            boolean alreadyLinked = record.getQuestions().stream()
                .anyMatch(existing -> existing.getId().equals(question.getId()));
            if (!alreadyLinked) {
                record.getQuestions().add(resolveQuestion(question.getId()));
            }
        }
        quizzes.save(record);
        return quiz;
    }

    /** Resolves a question identity into its record, failing fast when absent. */
    private QuestionRecord resolveQuestion(Long questionId) {
        return questions.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quiz> findById(Long id) {
        return quizzes.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findAll() {
        return quizzes.findAll().stream().map(DomainMapper::toDomain).toList();
    }
}
