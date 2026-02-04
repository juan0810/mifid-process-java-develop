package com.singularbank.mifid.repository.database;

import com.singularbank.mifid.entity.Version;
import com.singularbank.mifid.repository.VersionRepository;
import com.singularbank.mifid.repository.database.jpa.JpaVersionRepository;
import com.singularbank.mifid.repository.database.mapper.VersionEntityMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VersionRepositoryImpl implements VersionRepository {

  private final JpaVersionRepository jpaVersionRepository;
  private final VersionEntityMapper mapper;

  @Override
  public Version findVersionesActivasById(Short idVersion, String servicio) {
    return mapper.toDomain(jpaVersionRepository.findVersionesActivasById(idVersion, servicio));

  }

  @Override
  public Optional<Integer> findActiveVersionIdByServicioNombre(String nombreServicio) {
    return jpaVersionRepository.findActiveVersionIdByServicioNombre(nombreServicio);
  }
}