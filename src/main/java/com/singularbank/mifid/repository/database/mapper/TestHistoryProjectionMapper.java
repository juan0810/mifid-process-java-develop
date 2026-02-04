package com.singularbank.mifid.repository.database.mapper;

import com.singularbank.mifid.entity.HistoryTestItem;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.repository.database.projection.TestHistoryProjection;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class TestHistoryProjectionMapper {

  public HistoryTestItem toDomain(TestHistoryProjection projection) {
    TypeTest type = determineTestType(projection.getTipoTest());
    StateTest state = determineState(projection);
    String profile = extractProfile(projection, type);

    return HistoryTestItem.builder()
        .id(projection.getId())
        .type(type)
        .state(state)
        .profile(profile)
        .createdAt(projection.getFechaAlta())
        .signedAt(projection.getFechaFirma())
        .expiresAt(projection.getFechaCaducidad() != null
            ? projection.getFechaCaducidad().atStartOfDay()
            : null)
        .build();
  }

  private TypeTest determineTestType(String tipoTest) {
    return TypeTest.fromCode(tipoTest);
  }

  private StateTest determineState(TestHistoryProjection projection) {
    StateTest baseState = StateTest.fromCode(projection.getEstado());

    if (baseState == StateTest.SIGNED
        && projection.getFechaCaducidad() != null
        && projection.getFechaCaducidad().isBefore(LocalDate.now())) {
      return StateTest.EXPIRED;
    }
    return baseState;
  }

  private String extractProfile(TestHistoryProjection projection, TypeTest type) {
    if (type == TypeTest.SUITABILITY) {
      return projection.getResultadoIdoneidad();
    }
    return null;
  }
}