package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA record for academic topics; many questions may share one topic. */
@Entity
@Table(name = "topics")
public class TopicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    /** Required no-argument constructor for JPA. */
    public TopicRecord() {
    }

    /**
     * Creates a topic record.
     *
     * @param name        unique topic name
     * @param description short description of the knowledge area
     */
    public TopicRecord(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return unique topic name */
    public String getName() {
        return name;
    }

    /** @return short description of the knowledge area */
    public String getDescription() {
        return description;
    }
}
