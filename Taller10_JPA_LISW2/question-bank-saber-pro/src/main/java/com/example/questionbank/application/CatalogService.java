package com.example.questionbank.application;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.model.UserProfile;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Use cases for the support catalogs: users with roles and profiles, topics
 * and competencies. These operations populate the data needed by the main
 * question use cases.
 */
@Service
@Transactional
public class CatalogService {

    private final UserRepository users;
    private final TopicRepository topics;
    private final CompetencyRepository competencies;

    /**
     * Creates the service with its repository contracts.
     *
     * @param users        user contract
     * @param topics       topic contract
     * @param competencies competency contract
     */
    public CatalogService(UserRepository users, TopicRepository topics, CompetencyRepository competencies) {
        this.users = users;
        this.topics = topics;
        this.competencies = competencies;
    }

    /**
     * Registers a user with its roles and complementary profile.
     *
     * @param fullName person full name
     * @param email    unique email
     * @param roles    roles of the user
     * @param program  academic program for the profile
     * @param phone    contact phone for the profile
     * @return the stored user
     */
    public User createUser(String fullName, String email, Set<Role> roles, String program, String phone) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("A user needs at least one role.");
        }
        User user = new User(null, fullName, email, new HashSet<>(roles));
        user.setProfile(new UserProfile(null, program, phone));
        return users.save(user);
    }

    /**
     * Registers an academic topic.
     *
     * @param name        unique topic name
     * @param description short description
     * @return the stored topic
     */
    public Topic createTopic(String name, String description) {
        return topics.save(new Topic(null, name, description));
    }

    /**
     * Registers an academic competency.
     *
     * @param code        unique short code
     * @param name        human readable name
     * @param description what the competency measures
     * @return the stored competency
     */
    public Competency createCompetency(String code, String name, String description) {
        return competencies.save(new Competency(null, code, name, description));
    }

    /** @return all registered users */
    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return users.findAll();
    }

    /** @return all registered topics */
    @Transactional(readOnly = true)
    public List<Topic> listTopics() {
        return topics.findAll();
    }

    /** @return all registered competencies */
    @Transactional(readOnly = true)
    public List<Competency> listCompetencies() {
        return competencies.findAll();
    }
}
