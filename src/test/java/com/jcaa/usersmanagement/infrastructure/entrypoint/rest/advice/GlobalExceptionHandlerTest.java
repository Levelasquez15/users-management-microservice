package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.domain.exception.InvalidCredentialsException;
import com.jcaa.usersmanagement.domain.exception.InvalidUserEmailException;
import com.jcaa.usersmanagement.domain.exception.UserAlreadyExistsException;
import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.exception.PersistenceException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.ApiErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  @DisplayName("maneja UserNotFoundException con 404")
  void shouldHandleUserNotFound() {
    final ApiErrorResponse response =
        handler.handleUserNotFound(UserNotFoundException.becauseIdWasNotFound("0001"));
    assertEquals(404, response.status());
  }

  @Test
  @DisplayName("maneja UserAlreadyExistsException con 409")
  void shouldHandleUserAlreadyExists() {
    final ApiErrorResponse response =
        handler.handleUserAlreadyExists(
            UserAlreadyExistsException.becauseEmailAlreadyExists("ada@example.com"));
    assertEquals(409, response.status());
  }

  @Test
  @DisplayName("maneja InvalidCredentialsException con 401")
  void shouldHandleInvalidCredentials() {
    final ApiErrorResponse response =
        handler.handleInvalidCredentials(InvalidCredentialsException.becauseCredentialsAreInvalid());
    assertEquals(401, response.status());
  }

  @Test
  @DisplayName("maneja ConstraintViolationException con 400")
  @SuppressWarnings("unchecked")
  void shouldHandleConstraintViolation() {
    final ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    final Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("must not be blank");

    final ApiErrorResponse response =
        handler.handleConstraintViolation(new ConstraintViolationException(Set.of(violation)));

    assertEquals(400, response.status());
    assertTrue(response.message().contains("email: must not be blank"));
  }

  @Test
  @DisplayName("maneja MethodArgumentNotValidException con 400")
  void shouldHandleMethodArgumentNotValid() throws NoSuchMethodException {
    final BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "request");
    bindingResult.addError(new FieldError("request", "name", "must not be blank"));
    final MethodParameter parameter =
        new MethodParameter(Object.class.getMethod("toString"), -1);
    final MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(parameter, bindingResult);

    final ApiErrorResponse response = handler.handleMethodArgumentNotValid(exception);

    assertEquals(400, response.status());
    assertTrue(response.message().contains("name: must not be blank"));
  }

  @Test
  @DisplayName("maneja DomainException, PersistenceException y errores generales")
  void shouldHandleDomainPersistenceAndGeneralErrors() {
    final ApiErrorResponse domain =
        handler.handleDomain(InvalidUserEmailException.becauseValueIsEmpty());
    final ApiErrorResponse persistence =
        handler.handlePersistence(mock(PersistenceException.class));
    final ApiErrorResponse general = handler.handleGeneral(new RuntimeException("boom"));

    assertEquals(400, domain.status());
    assertEquals(500, persistence.status());
    assertEquals(500, general.status());
  }
}
