package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA record for users of the question bank.
 *
 * <p>Roles are stored as an element collection over the {@code user_roles}
 * table: a simpler alternative to a full many-to-many when the catalog of
 * roles has no extra attributes. The profile is a one-to-one association in
 * which the owning side is the profile itself (foreign key {@code user_id}
 * with a unique constraint in {@code user_profiles}).</p>
 */
@Entity
@Table(name = "users")
public class UserRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserProfileRecord profile;

    /** Required no-argument constructor for JPA. */
    public UserRecord() {
    }

    /**
     * Creates a user record.
     *
     * @param fullName person full name
     * @param email    unique contact email
     * @param roles    roles of the user
     */
    public UserRecord(String fullName, String email, Set<Role> roles) {
        this.fullName = fullName;
        this.email = email;
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return person full name */
    public String getFullName() {
        return fullName;
    }

    /** @return unique contact email */
    public String getEmail() {
        return email;
    }

    /** @return roles stored for this user */
    public Set<Role> getRoles() {
        return roles;
    }

    /** @return profile record owned by this user, or {@code null} */
    public UserProfileRecord getProfile() {
        return profile;
    }

    /** @param profile profile record owned by this user */
    public void setProfile(UserProfileRecord profile) {
        this.profile = profile;
    }
}
