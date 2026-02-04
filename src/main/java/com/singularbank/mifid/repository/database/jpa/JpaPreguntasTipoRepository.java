package com.singularbank.mifid.repository.database.jpa;

import com.singularbank.mifid.repository.database.model.PreguntasTipoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPreguntasTipoRepository extends JpaRepository<PreguntasTipoEntity, Short> {
}