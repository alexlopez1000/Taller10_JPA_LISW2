package com.example.questionbank.domain.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A person that interacts with the question bank.
 *
 * <p>This class is a plain domain object: it carries no persistence annotations.
 * Roles are part of the business language of the system, and the complementary
 * data lives in the {@link UserProfile} association.</p>
 */
public class User {

    private Long id;
    private String fullName;
    private String email;
    private final Set<Role> roles = new HashSet<>();
    private UserProfile profile;

    /** Creates an empty user; used by repositories and mappers. */
    public User() {
    }

    /**
     * Creates a user with its basic identity.
     *
     * @param id       persistent identity, {@code null} before saving
     * @param fullName person full name
     * @param email    unique contact email
     * @param roles    roles assigned to the person
     */
    public User(Long id, String fullName, String email, Set<Role> roles) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    /** @return persistent identity assigned by the repository */
    public Long getId() {
        return id;
    }

    /** @param id identifier assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return person full name */
    public String getFullName() {
        return fullName;
    }

    /** @return unique contact email */
    public String getEmail() {
        return email;
    }

    /** @return read-only view of the roles held by this user */
    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    /**
     * Checks whether the user holds one of the given roles.
     *
     * @param required roles accepted
     * @return {@code true} when the user holds at least one of them
     */
    public boolean hasAnyRole(Role... required) {
        for (Role role : required) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /** @return complementary profile data, or {@code null} when absent */
    public UserProfile getProfile() {
        return profile;
    }

    /** @param profile complementary profile data */
    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }
}
