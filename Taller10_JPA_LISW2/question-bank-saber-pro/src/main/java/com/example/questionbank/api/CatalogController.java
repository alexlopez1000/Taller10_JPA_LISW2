package com.example.questionbank.api;

import com.example.questionbank.application.CatalogService;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * REST endpoints for the support catalogs: users (with roles and profile),
 * topics and competencies. These resources feed the main question use cases.
 */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService service;

    /** Creates the controller. @param service catalog use cases */
    public CatalogController(CatalogService service) {
        this.service = service;
    }

    /** Request body for creating a user. */
    public record UserRequest(String fullName, String email, Set<Role> roles, String program, String phone) {
    }

    /** Response for a user. */
    public record UserResponse(Long id, String fullName, String email, Set<Role> roles, String program) {

        /** Builds the response from a domain user. */
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRoles(),
                user.getProfile() != null ? user.getProfile().getAcademicProgram() : null);
        }
    }

    /** Request body for creating a topic. */
    public record TopicRequest(String name, String description) {
    }

    /** Request body for creating a competency. */
    public record CompetencyRequest(String code, String name, String description) {
    }

    /** Creates a user with roles and profile. */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody UserRequest request) {
        return UserResponse.from(service.createUser(request.fullName(), request.email(), request.roles(),
            request.program(), request.phone()));
    }

    /** Lists users. */
    @GetMapping("/users")
    public List<UserResponse> users() {
        return service.listUsers().stream().map(UserResponse::from).toList();
    }

    /** Creates a topic. */
    @PostMapping("/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public Topic createTopic(@RequestBody TopicRequest request) {
        return service.createTopic(request.name(), request.description());
    }

    /** Lists topics. */
    @GetMapping("/topics")
    public List<Topic> topics() {
        return service.listTopics();
    }

    /** Creates a competency. */
    @PostMapping("/competencies")
    @ResponseStatus(HttpStatus.CREATED)
    public Competency createCompetency(@RequestBody CompetencyRequest request) {
        return service.createCompetency(request.code(), request.name(), request.description());
    }

    /** Lists competencies. */
    @GetMapping("/competencies")
    public List<Competency> competencies() {
        return service.listCompetencies();
    }
}
