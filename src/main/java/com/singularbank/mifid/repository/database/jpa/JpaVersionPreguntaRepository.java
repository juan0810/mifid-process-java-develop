package com.singularbank.mifid.repository.database.jpa;

import com.singularbank.mifid.repository.database.model.VersionPreguntaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaVersionPreguntaRepository extends JpaRepository<VersionPreguntaEntity, Integer> {
}
