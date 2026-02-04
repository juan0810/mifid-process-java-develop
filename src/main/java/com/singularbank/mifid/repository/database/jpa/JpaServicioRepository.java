package com.singularbank.mifid.repository.database.jpa;

import com.singularbank.mifid.repository.database.model.ServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaServicioRepository extends JpaRepository<ServicioEntity, Short> {
}