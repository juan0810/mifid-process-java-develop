package com.singularbank.mifid.repository;

import com.singularbank.mifid.entity.Version;
import java.util.Optional;

public interface VersionRepository {

  Optional<Integer> findActiveVersionIdByServicioNombre(
      String nombreServicio);

  Version findVersionesActivasById(Short idVersion, String servicio);
}