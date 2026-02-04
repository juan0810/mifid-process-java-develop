package com.singularbank.mifid.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.singularbank.mifid.controller.helpers.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex,
      WebRequest request) {
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.NOT_FOUND.value())
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .correlationId(getCorrelationId(request))
        .build();

    logger.warn("Resource not found: {}", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequestException(
      BadRequestException ex,
      WebRequest request) {
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .correlationId(getCorrelationId(request))
        .build();

    logger.warn("Bad request: {}", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex,
      WebRequest request) {

    String message = extractMessage(ex);

    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message(message)
        .path(request.getDescription(false).replace("uri=", ""))
        .correlationId(getCorrelationId(request))
        .build();

    logger.warn("JSON parsing error: {}", message);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex,
      WebRequest request) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .reduce("", (a, b) -> a + (a.isEmpty() ? "" : ", ") + b);

    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("Validation failed: " + message)
        .path(request.getDescription(false).replace("uri=", ""))
        .correlationId(getCorrelationId(request))
        .build();

    logger.warn("Validation failed: {}", message);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException ex,
      WebRequest request) {
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("Validation failed: " + ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .correlationId(getCorrelationId(request))
        .build();

    logger.warn("Constraint violation: {}", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
      HandlerMethodValidationException ex,
      WebRequest request) {

    var message = ex.getMessage();

    var errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message(message)
        .path(request.getDescription(false).replace("uri=", ""))
        .correlationId(getCorrelationId(request))
        .build();

    logger.warn("Handler method validation failed: {}", message);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(CustomerAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleCustomerAlreadyExistsException(
      CustomerAlreadyExistsException ex,
      WebRequest request) {
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.CONFLICT.value())
        .error(HttpStatus.CONFLICT.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .correlationId(getCorrelationId(request))
        .build();

    logger.warn("Customer already exists: {}", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex,
      WebRequest request) {

    String requiredType = Optional.ofNullable(ex.getRequiredType()).map(Class::getSimpleName)
        .orElse("unknown");

    String value = Objects.toString(ex.getValue(), "");

    String message = "Parameter '%s' with value '%s' could not be converted to type %s".formatted(
        ex.getName(), value, requiredType);

    ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message(message).path(request.getDescription(false).replace("uri=", ""))
        .correlationId(getCorrelationId(request))
        .build();

    logger.warn("Type mismatch error: {}", message);
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllUncaughtException(
      Exception ex,
      WebRequest request) {
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .message("An unexpected error occurred: " + ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .correlationId(getCorrelationId(request))
        .build();

    logger.error("Unexpected error occurred", ex);
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(RequestTimeoutException.class)
  public ResponseEntity<ErrorResponse> handleTimeoutException(
      RequestTimeoutException ex,
      WebRequest request) {
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.REQUEST_TIMEOUT.value())
        .error(HttpStatus.REQUEST_TIMEOUT.getReasonPhrase())
        .message("La solicitud ha excedido el tiempo máximo permitido")
        .path(request.getDescription(false).replace("uri=", ""))
        .correlationId(getCorrelationId(request))
        .build();

    logger.warn("Request timeout: {}", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.REQUEST_TIMEOUT);
  }

  @ExceptionHandler(ExternalServiceException.class)
  public ResponseEntity<ErrorResponse> handleExternalServiceException(
      ExternalServiceException ex,
      WebRequest request) {

    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_GATEWAY.value())
        .error(HttpStatus.BAD_GATEWAY.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .correlationId(getCorrelationId(request))
        .build();

    logger.error("External service error: {}", ex.getMessage(), ex);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
  }

  private String extractMessage(HttpMessageNotReadableException ex) {
    Throwable cause = ex.getCause();
    if (cause instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
      var validValues = Arrays
          .stream(ife.getTargetType().getEnumConstants())
          .map(Object::toString)
          .collect(Collectors.joining(", "));

      return "Invalid value '%s'. Valid values: [%s]".formatted(ife.getValue(), validValues);
    }

    String rootMessage = ex.getMessage();
    if (rootMessage == null) {
      return "Invalid request body";
    }
    if (rootMessage.contains("No content")) {
      return "Request body is required";
    }
    if (rootMessage.contains("Unrecognized field")) {
      return "Invalid request: " + rootMessage.split("\\n")[0];
    }

    return "Invalid request body format";
  }

  private String getCorrelationId(WebRequest request) {
    return request.getHeader(CORRELATION_ID_HEADER);
  }
}