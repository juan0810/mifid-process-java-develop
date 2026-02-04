package com.singularbank.mifid.service.status;

import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.entity.StateTest;
import java.time.LocalDateTime;

public sealed interface StatusAction permits
    StatusAction.SignAction,
    StatusAction.CancelAction,
    StatusAction.NoAction {

  void apply(RespuestaCliente respuesta, LocalDateTime timestamp);

  record SignAction() implements StatusAction {

    @Override
    public void apply(RespuestaCliente respuesta, LocalDateTime timestamp) {
      respuesta.setFechaFirma(timestamp);
      respuesta.setFechaCaducidad(timestamp.plusYears(3).toLocalDate());
    }
  }

  record CancelAction() implements StatusAction {

    @Override
    public void apply(RespuestaCliente respuesta, LocalDateTime timestamp) {
      respuesta.setFechaAnulacion(timestamp);
    }
  }

  record NoAction() implements StatusAction {

    @Override
    public void apply(RespuestaCliente respuesta, LocalDateTime timestamp) {
      // No action required for DRAFT, PENDING, and EXPIRED states
    }
  }

  static StatusAction forState(StateTest state) {
    return switch (state) {
      case SIGNED -> new SignAction();
      case CANCELLED -> new CancelAction();
      case DRAFT, PENDING, EXPIRED -> new NoAction();
    };
  }
}