package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringCompetencyDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Concrete JPA implementation of the domain competency contract. */
@Repository
public class CompetencyRepositoryJpa implements CompetencyRepository {

    private final SpringCompetencyDataRepository data;

    /** Creates the adapter. @param data Spring Data repository */
    public CompetencyRepositoryJpa(SpringCompetencyDataRepository data) {
        this.data = data;
    }

    @Override
    @Transactional
    public Competency save(Competency competency) {
        CompetencyRecord saved = data.save(new CompetencyRecord(competency.getCode(), competency.getName(),
            competency.getDescription()));
        competency.setId(saved.getId());
        return competency;
    }

    @Override
    public Optional<Competency> findById(Long id) {
        return data.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    public List<Competency> findAll() {
        return data.findAll().stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    public Optional<Competency> findByCode(String code) {
        return data.findByCode(code).map(DomainMapper::toDomain);
    }
}
