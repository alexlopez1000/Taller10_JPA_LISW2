package com.example.questionbank.domain.model;

/**
 * Complementary personal data of a user: academic program and contact phone.
 *
 * <p>Modeled as a separate concept so the user identity stays small, while the
 * optional profile details remain grouped. Plain domain object, no JPA here.</p>
 */
public class UserProfile {

    private Long id;
    private String academicProgram;
    private String phone;

    /** Creates an empty profile; used by repositories and mappers. */
    public UserProfile() {
    }

    /**
     * Creates a profile with its data.
     *
     * @param id              persistent identity, {@code null} before saving
     * @param academicProgram academic program of the person
     * @param phone           contact phone number
     */
    public UserProfile(Long id, String academicProgram, String phone) {
        this.id = id;
        this.academicProgram = academicProgram;
        this.phone = phone;
    }

    /** @return persistent identity assigned by the repository */
    public Long getId() {
        return id;
    }

    /** @param id identifier assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return academic program of the person */
    public String getAcademicProgram() {
        return academicProgram;
    }

    /** @return contact phone number */
    public String getPhone() {
        return phone;
    }
}
