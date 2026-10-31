package com.example.questionbank.domain.model;

/**
 * Academic competency evaluated by questions (for example "Software design"
 * or "Problem analysis" in the Saber Pro framework).
 *
 * <p>A question may contribute to several competencies and a competency is
 * measured by many questions; the strength of that contribution is stored in
 * the {@link QuestionCompetency} association.</p>
 */
public class Competency {

    private Long id;
    private String code;
    private String name;
    private String description;

    /** Creates an empty competency; used by repositories and mappers. */
    public Competency() {
    }

    /**
     * Creates a competency.
     *
     * @param id          persistent identity, {@code null} before saving
     * @param code        unique short code, for example "COMP-DISENO"
     * @param name        human readable name
     * @param description what the competency measures
     */
    public Competency(Long id, String code, String name, String description) {
        this.id = id;
        this.code = code;
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

    /** @return unique short code */
    public String getCode() {
        return code;
    }

    /** @return human readable name */
    public String getName() {
        return name;
    }

    /** @return what the competency measures */
    public String getDescription() {
        return description;
    }
}
