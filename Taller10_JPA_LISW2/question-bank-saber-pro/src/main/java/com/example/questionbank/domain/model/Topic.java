package com.example.questionbank.domain.model;

/**
 * Academic topic that organizes questions inside a knowledge area.
 *
 * <p>Plain domain object; many questions may belong to one topic through the
 * many-to-one association modeled in the persistence layer.</p>
 */
public class Topic {

    private Long id;
    private String name;
    private String description;

    /** Creates an empty topic; used by repositories and mappers. */
    public Topic() {
    }

    /**
     * Creates a topic.
     *
     * @param id          persistent identity, {@code null} before saving
     * @param name        unique topic name
     * @param description short description of the knowledge area
     */
    public Topic(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /** @return persistent identity assigned by the repository */
    public Long getId() {
        return id;
    }

    /** @param id identifier assigned by the repository */
    public void setId(Long id) {
        this.id = id;
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
