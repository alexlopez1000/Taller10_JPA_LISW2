package com.example.questionbank.domain.model;

/** Decision taken by a reviewer over a question under review. */
public enum ReviewDecision {
    /** The reviewer accepts the question. */
    APPROVED,
    /** The reviewer rejects the question; it goes back to DRAFT for correction. */
    REJECTED
}
