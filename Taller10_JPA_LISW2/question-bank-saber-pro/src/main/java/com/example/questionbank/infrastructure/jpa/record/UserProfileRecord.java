package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * JPA record for the complementary profile of a user.
 *
 * <p>This is the owning side of the second one-to-one association of the
 * model: the foreign key {@code user_id} lives here, is not null and is
 * unique, which guarantees a strict 1-to-1 correspondence between users and
 * profiles.</p>
 */
@Entity
@Table(name = "user_profiles")
public class UserProfileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academic_program", length = 120)
    private String academicProgram;

    @Column(length = 30)
    private String phone;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserRecord user;

    /** Required no-argument constructor for JPA. */
    protected UserProfileRecord() {
    }

    /**
     * Creates a profile record.
     *
     * @param academicProgram academic program of the person
     * @param phone           contact phone
     * @param user            owning user record
     */
    public UserProfileRecord(String academicProgram, String phone, UserRecord user) {
        this.academicProgram = academicProgram;
        this.phone = phone;
        this.user = user;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return academic program of the person */
    public String getAcademicProgram() {
        return academicProgram;
    }

    /** @return contact phone */
    public String getPhone() {
        return phone;
    }

    /** @return owning user record */
    public UserRecord getUser() {
        return user;
    }
}
