package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.UserRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.UserProfileRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Concrete JPA implementation of the domain user contract. Saving a user
 * cascades to its roles (element collection) and to its profile (one-to-one
 * owned by the profile).
 */
@Repository
public class UserRepositoryJpa implements UserRepository {

    private final SpringUserDataRepository data;

    /** Creates the adapter. @param data Spring Data repository */
    public UserRepositoryJpa(SpringUserDataRepository data) {
        this.data = data;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserRecord record = new UserRecord(user.getFullName(), user.getEmail(), user.getRoles());
        if (user.getProfile() != null) {
            record.setProfile(new UserProfileRecord(user.getProfile().getAcademicProgram(),
                user.getProfile().getPhone(), record));
        }
        UserRecord saved = data.save(record);
        user.setId(saved.getId());
        if (user.getProfile() != null && saved.getProfile() != null) {
            user.getProfile().setId(saved.getProfile().getId());
        }
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return data.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return data.findAll().stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return data.findByEmail(email).map(DomainMapper::toDomain);
    }
}
