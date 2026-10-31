package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.User;

import java.util.List;
import java.util.Optional;

/** Technology-independent persistence contract for users. */
public interface UserRepository {

    /**
     * Stores a user together with its roles and profile.
     *
     * @param user user to store
     * @return stored user with its persistent identity
     */
    User save(User user);

    /**
     * Finds a user by identity.
     *
     * @param id persistent identity
     * @return the user, or empty when it does not exist
     */
    Optional<User> findById(Long id);

    /** @return all registered users */
    List<User> findAll();

    /**
     * Finds a user by its unique email.
     *
     * @param email contact email
     * @return the user, or empty when it does not exist
     */
    Optional<User> findByEmail(String email);
}
