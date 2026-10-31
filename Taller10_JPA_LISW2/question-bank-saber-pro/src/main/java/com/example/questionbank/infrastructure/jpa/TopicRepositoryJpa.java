package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringTopicDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Concrete JPA implementation of the domain topic contract. */
@Repository
public class TopicRepositoryJpa implements TopicRepository {

    private final SpringTopicDataRepository data;

    /** Creates the adapter. @param data Spring Data repository */
    public TopicRepositoryJpa(SpringTopicDataRepository data) {
        this.data = data;
    }

    @Override
    @Transactional
    public Topic save(Topic topic) {
        TopicRecord saved = data.save(new TopicRecord(topic.getName(), topic.getDescription()));
        topic.setId(saved.getId());
        return topic;
    }

    @Override
    public Optional<Topic> findById(Long id) {
        return data.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    public List<Topic> findAll() {
        return data.findAll().stream().map(DomainMapper::toDomain).toList();
    }
}
