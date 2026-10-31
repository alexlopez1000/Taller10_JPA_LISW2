package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Topic;

import java.util.List;
import java.util.Optional;

/** Technology-independent persistence contract for academic topics. */
public interface TopicRepository {

    /**
     * Stores a topic.
     *
     * @param topic topic to store
     * @return stored topic with its persistent identity
     */
    Topic save(Topic topic);

    /**
     * Finds a topic by identity.
     *
     * @param id persistent identity
     * @return the topic, or empty when it does not exist
     */
    Optional<Topic> findById(Long id);

    /** @return all registered topics */
    List<Topic> findAll();
}
