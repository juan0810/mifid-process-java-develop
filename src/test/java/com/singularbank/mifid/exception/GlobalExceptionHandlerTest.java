package com.singularbank.mifid.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.singularbank.mifid.controller.helpers.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @InjectMocks
  private GlobalExceptionHandler globalExceptionHandler;

  @Mock
  private WebRequest webRequest;

  @Test
  void handleHttpMessageNotReadableException_WithUnrecognizedField_ShouldReturn400() {
    // Given
    String errorMessage = "JSON parse error: Unrecognized field \"city\" (class com.singularbank.onboarding.dto.CustomerRequestDTO), not marked as ignorable";
    HttpInputMessage inputMessage = mock(HttpInputMessage.class);
    HttpMessageNotReadableException exception = new HttpMessageNotReadableException(errorMessage,
        inputMessage);
    when(webRequest.getDescription(false)).thenReturn("uri=/api/customers");

    // When
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadableException(
        exception, webRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals(400, errorResponse.getStatus());
    assertEquals("Bad Request", errorResponse.getError());
    assertTrue(errorResponse.getMessage().contains("Unrecognized field"));
    assertEquals("/api/customers", errorResponse.getPath());
    assertNotNull(errorResponse.getTimestamp());
  }

  @Test
  void handleHttpMessageNotReadableException_WithNoContent_ShouldReturn400() {
    // Given
    String errorMessage = "No content to map due to end-of-input";
    HttpInputMessage inputMessage = mock(HttpInputMessage.class);
    HttpMessageNotReadableException exception = new HttpMessageNotReadableException(errorMessage,
        inputMessage);
    when(webRequest.getDescription(false)).thenReturn("uri=/api/customers");

    // When
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadableException(
        exception, webRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Request body is required", errorResponse.getMessage());
  }

  @Test
  void handleHttpMessageNotReadableException_WithInvalidEnumValue_ShouldReturn400WithValidValues() {
    // Given
    InvalidFormatException cause = mock(InvalidFormatException.class);
    doReturn(TestEnum.class).when(cause).getTargetType();
    when(cause.getValue()).thenReturn("INVALID_VALUE");

    HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
        "Cannot deserialize", cause, null);
    when(webRequest.getDescription(false)).thenReturn("uri=/api/test");

    // When
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadableException(
        exception, webRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertTrue(errorResponse.getMessage().contains("Invalid value 'INVALID_VALUE'"));
    assertTrue(errorResponse.getMessage().contains("Valid values:"));
    assertTrue(errorResponse.getMessage().contains("VALUE_A"));
    assertTrue(errorResponse.getMessage().contains("VALUE_B"));
  }

  @Test
  void handleHttpMessageNotReadableException_WithGenericError_ShouldReturn400() {
    // Given
    String errorMessage = "Some other JSON parsing error";
    HttpInputMessage inputMessage = mock(HttpInputMessage.class);
    HttpMessageNotReadableException exception = new HttpMessageNotReadableException(errorMessage,
        inputMessage);
    when(webRequest.getDescription(false)).thenReturn("uri=/api/customers");

    // When
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadableException(
        exception, webRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals(400, errorResponse.getStatus());
    assertEquals("Bad Request", errorResponse.getError());
    assertEquals("Invalid request body format", errorResponse.getMessage());
    assertEquals("/api/customers", errorResponse.getPath());
    assertNotNull(errorResponse.getTimestamp());
  }

  @Test
  void handleHttpMessageNotReadableException_WithNullMessage_ShouldReturn400() {
    // Given
    HttpInputMessage inputMessage = mock(HttpInputMessage.class);
    HttpMessageNotReadableException exception = new HttpMessageNotReadableException(null,
        inputMessage);
    when(webRequest.getDescription(false)).thenReturn("uri=/api/customers");

    // When
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadableException(
        exception, webRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Invalid request body", errorResponse.getMessage());
  }

  @Test
  void handleBadRequestException_ShouldReturn400() {
    // Given
    BadRequestException exception = new BadRequestException("Invalid request data");
    when(webRequest.getDescription(false)).thenReturn("uri=/api/customers");

    // When
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadRequestException(
        exception, webRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals(400, errorResponse.getStatus());
    assertEquals("Bad Request", errorResponse.getError());
    assertEquals("Invalid request data", errorResponse.getMessage());
    assertEquals("/api/customers", errorResponse.getPath());
    assertNotNull(errorResponse.getTimestamp());
  }

  @Test
  void handleResourceNotFoundException_ShouldReturn404() {
    // Given
    ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");
    when(webRequest.getDescription(false)).thenReturn("uri=/api/resource/123");

    // When
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(
        exception, webRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals(404, errorResponse.getStatus());
    assertEquals("Not Found", errorResponse.getError());
    assertEquals("Resource not found", errorResponse.getMessage());
    assertEquals("/api/resource/123", errorResponse.getPath());
    assertNotNull(errorResponse.getTimestamp());
  }

  @Test
  void handleCustomerAlreadyExistsException_ShouldReturn409() {
    // Given
    CustomerAlreadyExistsException exception = new CustomerAlreadyExistsException(
        "Customer already exists");
    when(webRequest.getDescription(false)).thenReturn("uri=/api/customers");

    // When
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleCustomerAlreadyExistsException(
        exception, webRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals(409, errorResponse.getStatus());
    assertEquals("Conflict", errorResponse.getError());
    assertEquals("Customer already exists", errorResponse.getMessage());
    assertEquals("/api/customers", errorResponse.getPath());
    assertNotNull(errorResponse.getTimestamp());
  }

  @Test
  void handleRequestTimeoutException_ShouldReturn408() {
    // Given
    RequestTimeoutException exception = new RequestTimeoutException("Request timeout");
    when(webRequest.getDescription(false)).thenReturn("uri=/api/test");

    // When
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleTimeoutException(
        exception, webRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals(408, errorResponse.getStatus());
    assertEquals("Request Timeout", errorResponse.getError());
    assertEquals("La solicitud ha excedido el tiempo máximo permitido",
        errorResponse.getMessage());
    assertEquals("/api/test", errorResponse.getPath());
    assertNotNull(errorResponse.getTimestamp());
  }

  @Test
  void handleExternalServiceException_ShouldReturn502() {
    // Given
    ExternalServiceException exception = new ExternalServiceException("Service unavailable");
    when(webRequest.getDescription(false)).thenReturn("uri=/api/external");

    // When
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleExternalServiceException(
        exception, webRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals(502, errorResponse.getStatus());
    assertEquals("Bad Gateway", errorResponse.getError());
    assertEquals("Service unavailable", errorResponse.getMessage());
    assertEquals("/api/external", errorResponse.getPath());
    assertNotNull(errorResponse.getTimestamp());
  }

  @Test
  void handleAllUncaughtException_ShouldReturn500() {
    // Given
    Exception exception = new RuntimeException("Unexpected error");
    when(webRequest.getDescription(false)).thenReturn("uri=/api/test");

    // When
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAllUncaughtException(
        exception, webRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals(500, errorResponse.getStatus());
    assertEquals("Internal Server Error", errorResponse.getError());
    assertTrue(errorResponse.getMessage().contains("Unexpected error"));
    assertEquals("/api/test", errorResponse.getPath());
    assertNotNull(errorResponse.getTimestamp());
  }

  private enum TestEnum {
    VALUE_A, VALUE_B
  }
}