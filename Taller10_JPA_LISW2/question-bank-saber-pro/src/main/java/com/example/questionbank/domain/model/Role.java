package com.example.questionbank.domain.model;

/**
 * Roles a user may hold in the question bank.
 *
 * <p>Roles are modeled as a fixed catalog of values instead of entities because
 * they do not need runtime administration or extra attributes in this scope.</p>
 */
public enum Role {
    /** Manages users, roles and system parameters. */
    ADMIN,
    /** Creates and edits questions. */
    AUTHOR,
    /** Evaluates quality and relevance of questions. */
    REVIEWER,
    /** Organizes quizzes and simulacros. */
    TEACHER,
    /** Answers questions and consults results. */
    STUDENT
}
