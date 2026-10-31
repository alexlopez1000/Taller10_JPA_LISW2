package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data adapter used internally by the user repository implementation. */
public interface SpringUserDataRepository extends JpaRepository<UserRecord, Long> {

    /**
     * Finds a user by its unique email.
     *
     * @param email contact email
     * @return the user, or empty when it does not exist
     */
    Optional<UserRecord> findByEmail(String email);
}
