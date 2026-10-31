package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionCompetency;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.Review;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.model.UserProfile;
import com.example.questionbank.infrastructure.jpa.record.AnswerOptionRecord;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import com.example.questionbank.infrastructure.jpa.record.MultipleChoiceQuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionCompetencyRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionMetadataRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuizRecord;
import com.example.questionbank.infrastructure.jpa.record.ReviewRecord;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import com.example.questionbank.infrastructure.jpa.record.TrueFalseQuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.record.UserProfileRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Translates between technology-independent domain objects and JPA records.
 *
 * <p>This is the single place where both worlds meet: the domain never knows
 * the records and the records only serve the persistence mapping. Reference
 * resolution by identifier (loading a {@code UserRecord} for an author, for
 * example) stays in the repository implementations; the mapper only converts
 * object graphs.</p>
 */
public final class DomainMapper {

    private DomainMapper() {
    }

    /**
     * Converts a user record to its domain representation, including roles and
     * profile.
     *
     * @param record JPA user record
     * @return domain user
     */
    public static User toDomain(UserRecord record) {
        User user = new User(record.getId(), record.getFullName(), record.getEmail(), record.getRoles());
        if (record.getProfile() != null) {
            user.setProfile(toDomain(record.getProfile()));
        }
        return user;
    }

    /**
     * Converts a profile record to its domain representation.
     *
     * @param record JPA profile record
     * @return domain profile
     */
    public static UserProfile toDomain(UserProfileRecord record) {
        return new UserProfile(record.getId(), record.getAcademicProgram(), record.getPhone());
    }

    /**
     * Converts a topic record to its domain representation.
     *
     * @param record JPA topic record
     * @return domain topic
     */
    public static Topic toDomain(TopicRecord record) {
        return new Topic(record.getId(), record.getName(), record.getDescription());
    }

    /**
     * Converts a competency record to its domain representation.
     *
     * @param record JPA competency record
     * @return domain competency
     */
    public static Competency toDomain(CompetencyRecord record) {
        return new Competency(record.getId(), record.getCode(), record.getName(), record.getDescription());
    }

    /**
     * Converts a metadata record to its domain representation.
     *
     * @param record JPA metadata record
     * @return domain metadata
     */
    public static QuestionMetadata toDomain(QuestionMetadataRecord record) {
        return new QuestionMetadata(record.getId(), record.getCognitiveLevel(), record.getEstimatedTimeMinutes(),
            record.getLastReviewedAt(), record.getEditorialNotes());
    }

    /**
     * Converts a metadata domain object to a new record.
     *
     * @param metadata domain metadata
     * @return new JPA metadata record
     */
    public static QuestionMetadataRecord toRecord(QuestionMetadata metadata) {
        return new QuestionMetadataRecord(metadata.getCognitiveLevel(), metadata.getEstimatedTimeMinutes(),
            metadata.getLastReviewedAt(), metadata.getEditorialNotes());
    }

    /**
     * Converts an answer option record to its domain representation.
     *
     * @param record JPA option record
     * @return domain answer option
     */
    public static AnswerOption toDomain(AnswerOptionRecord record) {
        return new AnswerOption(record.getId(), record.getOptionText(), record.isCorrect(), record.getExplanation());
    }

    /**
     * Converts a question record to its domain representation, choosing the
     * concrete domain subtype according to the persistence subtype (JOINED
     * hierarchy) and rebuilding the full object graph: author, topic,
     * metadata, options and competency contributions.
     *
     * @param record JPA question record (any subtype, possibly a LAZY proxy)
     * @return domain question of the matching subtype
     */
    public static Question toDomain(QuestionRecord record) {
        QuestionRecord concrete = unwrap(record);
        Question question;
        if (concrete instanceof MultipleChoiceQuestionRecord multipleChoice) {
            MultipleChoiceQuestion mcq = new MultipleChoiceQuestion(concrete.getId(), concrete.getStatement(),
                concrete.getDifficulty(), null, null, multipleChoice.isAllowMultipleSelection());
            List<AnswerOption> options = new ArrayList<>();
            for (AnswerOptionRecord optionRecord : multipleChoice.getOptions()) {
                options.add(toDomain(optionRecord));
            }
            mcq.replaceOptions(options);
            question = mcq;
        } else if (concrete instanceof TrueFalseQuestionRecord trueFalse) {
            question = new TrueFalseQuestion(concrete.getId(), concrete.getStatement(), concrete.getDifficulty(),
                null, null, trueFalse.getExpectedAnswer());
        } else {
            throw new IllegalArgumentException("Unknown question record type: " + concrete.getClass());
        }
        question.restoreStatus(concrete.getStatus());
        if (concrete.getAuthor() != null) {
            question.setAuthor(toDomain(concrete.getAuthor()));
        }
        if (concrete.getTopic() != null) {
            question.setTopic(toDomain(concrete.getTopic()));
        }
        if (concrete.getMetadata() != null) {
            question.setMetadata(toDomain(concrete.getMetadata()));
        }
        for (QuestionCompetencyRecord contributionRecord : concrete.getCompetencyContributions()) {
            question.addCompetencyContribution(new QuestionCompetency(contributionRecord.getId(), question,
                toDomain(contributionRecord.getCompetency()), contributionRecord.getContributionWeight()));
        }
        return question;
    }

    /**
     * Resolves a LAZY inheritance proxy into its concrete record so the
     * subtype checks above work even when the association was not initialized
     * inside the current transaction.
     *
     * @param record possibly proxied question record
     * @return the underlying concrete record
     */
    private static QuestionRecord unwrap(QuestionRecord record) {
        if (record instanceof org.hibernate.proxy.HibernateProxy proxy
            && proxy.getHibernateLazyInitializer().getImplementation() instanceof QuestionRecord concrete) {
            return concrete;
        }
        return record;
    }

    /**
     * Creates a new question record of the concrete subtype for a domain
     * question that has never been persisted. The metadata record is created
     * here; the author and topic records are resolved by the repository
     * implementation and passed in. Options of a multiple choice question are
     * converted and attached as part of the record.
     *
     * @param question domain question (subtype decides the record)
     * @param author   author record resolved by the repository
     * @param topic    topic record resolved by the repository, may be {@code null}
     * @return new JPA question record without competency contributions
     */
    public static QuestionRecord toRecord(Question question, UserRecord author, TopicRecord topic) {
        QuestionRecord record;
        if (question instanceof MultipleChoiceQuestion multipleChoice) {
            MultipleChoiceQuestionRecord mcqRecord = new MultipleChoiceQuestionRecord(
                question.getStatement(), question.getDifficulty(), question.getStatus(), author,
                multipleChoice.isAllowMultipleSelection());
            for (AnswerOption option : multipleChoice.getOptions()) {
                mcqRecord.addOption(new AnswerOptionRecord(option.getOptionText(), option.isCorrect(),
                    option.getExplanation()));
            }
            record = mcqRecord;
        } else if (question instanceof TrueFalseQuestion trueFalse) {
            record = new TrueFalseQuestionRecord(question.getStatement(), question.getDifficulty(),
                question.getStatus(), author, trueFalse.getExpectedAnswer());
        } else {
            throw new IllegalArgumentException("Unknown question domain type: " + question.getClass());
        }
        record.setTopic(topic);
        return record;
    }

    /**
     * Converts a quiz record to its domain representation, including its
     * creator and the questions gathered through the join table.
     *
     * @param record JPA quiz record
     * @return domain quiz
     */
    public static Quiz toDomain(QuizRecord record) {
        Quiz quiz = new Quiz(record.getId(), record.getTitle(), record.getDescription(),
            record.getCreator() != null ? toDomain(record.getCreator()) : null, record.getCreatedAt());
        List<Question> questions = new ArrayList<>();
        record.getQuestions().forEach(questionRecord -> questions.add(toDomain(questionRecord)));
        quiz.replaceQuestions(questions);
        return quiz;
    }

    /**
     * Converts a review record to its domain representation.
     *
     * @param record JPA review record
     * @return domain review
     */
    public static Review toDomain(ReviewRecord record) {
        return new Review(record.getId(), toDomain(record.getQuestion()), toDomain(record.getReviewer()),
            record.getDecision(), record.getComments(), record.getReviewedAt());
    }
}
