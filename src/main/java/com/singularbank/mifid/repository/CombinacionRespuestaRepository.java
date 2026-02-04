package com.singularbank.mifid.repository;

public interface CombinacionRespuestaRepository {

  Integer findByFamilyCode(String familia, boolean conveniente);

  Integer findByProfileName(String profileName);
}
