package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionCompetency;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.AnswerOptionRecord;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import com.example.questionbank.infrastructure.jpa.record.MultipleChoiceQuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionCompetencyRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionMetadataRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import com.example.questionbank.infrastructure.jpa.record.TrueFalseQuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringCompetencyDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionCompetencyDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionMetadataDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringTopicDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Concrete JPA implementation of the domain question contract.
 *
 * <p>This adapter is responsible for resolving identifiers into records
 * (author, topic, competencies), for keeping both sides of every association
 * in sync, and for translating back to domain objects. It is the only place
 * where the polymorphism of the JOINED hierarchy is materialized: inserting
 * and updating spread over the {@code questions} base table and the subtype
 * tables inside the same transaction.</p>
 */
@Repository
public class QuestionRepositoryJpa implements QuestionRepository {

    private final SpringQuestionDataRepository questions;
    private final SpringUserDataRepository users;
    private final SpringTopicDataRepository topics;
    private final SpringCompetencyDataRepository competencies;
    private final SpringQuestionMetadataDataRepository metadataData;
    private final SpringQuestionCompetencyDataRepository contributions;

    /**
     * Creates the adapter.
     *
     * @param questions     Spring Data repository for question records
     * @param users         Spring Data repository for author resolution
     * @param topics        Spring Data repository for topic resolution
     * @param competencies  Spring Data repository for competency resolution
     * @param metadataData  Spring Data repository for metadata resolution
     * @param contributions Spring Data repository for the associative rows
     */
    public QuestionRepositoryJpa(SpringQuestionDataRepository questions, SpringUserDataRepository users,
                                 SpringTopicDataRepository topics, SpringCompetencyDataRepository competencies,
                                 SpringQuestionMetadataDataRepository metadataData,
                                 SpringQuestionCompetencyDataRepository contributions) {
        this.questions = questions;
        this.users = users;
        this.topics = topics;
        this.competencies = competencies;
        this.metadataData = metadataData;
        this.contributions = contributions;
    }

    @Override
    @Transactional
    public Question save(Question question) {
        if (question.getId() == null) {
            return persistNew(question);
        }
        return updateExisting(question);
    }

    /**
     * Inserts a new question record together with its metadata, options and
     * competency contributions, resolving referenced identities first. When
     * the domain metadata already carries an identity, the existing record is
     * reused; the unique constraint on {@code metadata_id} then prevents two
     * questions from sharing it.
     */
    private Question persistNew(Question question) {
        UserRecord authorRecord = users.findById(question.getAuthor().getId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Author not found: " + question.getAuthor().getId()));
        TopicRecord topicRecord = resolveTopic(question.getTopic());
        QuestionRecord record = DomainMapper.toRecord(question, authorRecord, topicRecord);
        record.setMetadata(resolveMetadata(question.getMetadata()));
        attachCompetencyContributions(record, question);
        QuestionRecord saved = questions.save(record);
        syncGeneratedIds(question, saved);
        return question;
    }

    /** Resolves the metadata into a new record or the referenced existing one. */
    private QuestionMetadataRecord resolveMetadata(com.example.questionbank.domain.model.QuestionMetadata metadata) {
        if (metadata.getId() == null) {
            return DomainMapper.toRecord(metadata);
        }
        return metadataData.findById(metadata.getId())
            .orElseThrow(() -> new IllegalArgumentException("Metadata not found: " + metadata.getId()));
    }

    /**
     * Builds one {@link QuestionCompetencyRecord} per domain contribution of a
     * brand-new question, resolving the competency identity; the cascade on
     * the question side persists them together with the question.
     */
    private void attachCompetencyContributions(QuestionRecord record, Question question) {
        for (QuestionCompetency contribution : question.getCompetencyContributions()) {
            Long competencyId = contribution.getCompetency().getId();
            CompetencyRecord competencyRecord = competencies.findById(competencyId)
                .orElseThrow(() -> new IllegalArgumentException("Competency not found: " + competencyId));
            QuestionCompetencyRecord contributionRecord =
                new QuestionCompetencyRecord(competencyRecord, contribution.getContributionWeight());
            contributionRecord.setQuestion(record);
            record.getCompetencyContributions().add(contributionRecord);
        }
    }

    /**
     * Updates an existing question: shared columns, subtype columns, topic,
     * metadata fields and the owned collections (options and contributions are
     * replaced, and orphan removal deletes the ones that disappear).
     */
    private Question updateExisting(Question question) {
        QuestionRecord record = questions.findById(question.getId())
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + question.getId()));
        if (question instanceof MultipleChoiceQuestion mcq && record instanceof MultipleChoiceQuestionRecord mcqRecord) {
            mcqRecord.setAllowMultipleSelection(mcq.isAllowMultipleSelection());
            mcqRecord.getOptions().clear();
            for (AnswerOption option : mcq.getOptions()) {
                mcqRecord.addOption(new AnswerOptionRecord(option.getOptionText(), option.isCorrect(),
                    option.getExplanation()));
            }
        } else if (question instanceof TrueFalseQuestion tfq && record instanceof TrueFalseQuestionRecord tfRecord) {
            tfRecord.setExpectedAnswer(tfq.getExpectedAnswer());
        } else {
            throw new IllegalStateException(
                "Question type mismatch between domain and persistence for question " + question.getId());
        }
        record.updateFrom(question.getStatement(), question.getDifficulty(), question.getStatus());
        record.setTopic(resolveTopic(question.getTopic()));
        updateMetadata(record.getMetadata(), question.getMetadata());
        syncCompetencyContributions(record, question);
        questions.save(record);
        syncGeneratedIds(question, record);
        return question;
    }

    /** Copies the editable metadata fields from the domain object to the record. */
    private void updateMetadata(QuestionMetadataRecord record, com.example.questionbank.domain.model.QuestionMetadata metadata) {
        if (metadata == null) {
            return;
        }
        record.setCognitiveLevel(metadata.getCognitiveLevel());
        record.setEstimatedTimeMinutes(metadata.getEstimatedTimeMinutes());
        record.setLastReviewedAt(metadata.getLastReviewedAt());
        record.setEditorialNotes(metadata.getEditorialNotes());
    }

    /** Resolves the topic identity into its record, or {@code null} when absent. */
    private TopicRecord resolveTopic(Topic topic) {
        if (topic == null) {
            return null;
        }
        if (topic.getId() == null) {
            throw new IllegalArgumentException("Topic must be an existing catalog entry with identity");
        }
        return topics.findById(topic.getId())
            .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topic.getId()));
    }

    /**
     * Synchronizes the competency contributions of an existing record with the
     * domain list. Removed rows are deleted and new rows are inserted through
     * the dedicated associative repository, which keeps the synchronization
     * deterministic and avoids conflicts with the unique constraint on
     * (question_id, competency_id); unchanged rows are only updated in place.
     */
    private void syncCompetencyContributions(QuestionRecord record, Question question) {
        List<QuestionCompetencyRecord> existing = record.getCompetencyContributions();
        List<QuestionCompetencyRecord> stale = existing.stream()
            .filter(current -> question.getCompetencyContributions().stream()
                .noneMatch(desired -> current.getCompetency().getId().equals(desired.getCompetency().getId())))
            .toList();
        for (QuestionCompetencyRecord removed : stale) {
            existing.remove(removed);
            contributions.delete(removed);
        }
        for (QuestionCompetency contribution : question.getCompetencyContributions()) {
            QuestionCompetencyRecord match = existing.stream()
                .filter(current -> current.getCompetency().getId().equals(contribution.getCompetency().getId()))
                .findFirst().orElse(null);
            if (match == null) {
                Long competencyId = contribution.getCompetency().getId();
                CompetencyRecord competencyRecord = competencies.findById(competencyId)
                    .orElseThrow(() -> new IllegalArgumentException("Competency not found: " + competencyId));
                QuestionCompetencyRecord contributionRecord =
                    new QuestionCompetencyRecord(competencyRecord, contribution.getContributionWeight());
                contributionRecord.setQuestion(record);
                existing.add(contributionRecord);
                contributions.save(contributionRecord);
            } else {
                match.setContributionWeight(contribution.getContributionWeight());
            }
        }
    }

    /** Copies the generated identifiers back into the domain object graph. */
    private void syncGeneratedIds(Question question, QuestionRecord record) {
        question.setId(record.getId());
        if (question.getMetadata() != null && record.getMetadata() != null) {
            question.getMetadata().setId(record.getMetadata().getId());
        }
        if (question instanceof MultipleChoiceQuestion mcq && record instanceof MultipleChoiceQuestionRecord mcqRecord) {
            List<AnswerOption> domainOptions = mcq.getOptions();
            List<AnswerOptionRecord> savedOptions = mcqRecord.getOptions();
            for (int i = 0; i < domainOptions.size() && i < savedOptions.size(); i++) {
                domainOptions.get(i).setId(savedOptions.get(i).getId());
            }
        }
        List<QuestionCompetency> domainContributions = question.getCompetencyContributions();
        List<QuestionCompetencyRecord> savedContributions = record.getCompetencyContributions();
        for (QuestionCompetency domainContribution : domainContributions) {
            savedContributions.stream()
                .filter(saved -> saved.getCompetency().getId().equals(domainContribution.getCompetency().getId()))
                .findFirst()
                .ifPresent(saved -> domainContribution.setId(saved.getId()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Question> findById(Long id) {
        return questions.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findAll() {
        return questions.findAll().stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findByAuthorId(Long authorId) {
        return questions.findByAuthorId(authorId).stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findByStatus(QuestionStatus status) {
        return questions.findByStatus(status).stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findPublishedByCompetency(Long competencyId) {
        return questions.findPublishedByCompetency(competencyId, QuestionStatus.PUBLISHED).stream()
            .map(DomainMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        questions.deleteById(id);
    }
}
