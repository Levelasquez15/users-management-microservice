package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UserResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserManagementCli")
class UserManagementCliTest {

  @Test
  @DisplayName("ejecuta el bucle del menu manejando opciones invalidas, validas, errores y salida")
  @SuppressWarnings("unchecked")
  void shouldRunCliMenuLoopAndHandleErrors() {
    final UserController controller = mock(UserController.class);
    final ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("email invalid");

    when(controller.listAllUsers())
        .thenReturn(List.of(new UserResponse("1", "Ada", "ada@example.com", "ADMIN", "ACTIVE")))
        .thenThrow(new ConstraintViolationException(Set.of(violation)))
        .thenThrow(new IllegalStateException("unexpected failure"));

    final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    final ConsoleIO console =
        new ConsoleIO(
            new Scanner(
                new ByteArrayInputStream("99\n1\n1\n1\n0\n".getBytes(StandardCharsets.UTF_8))),
            new PrintStream(outBytes, true, StandardCharsets.UTF_8));

    new UserManagementCli(controller, console).start();

    final String output = outBytes.toString(StandardCharsets.UTF_8);
    assertTrue(output.contains("Users Management System"));
    assertTrue(output.contains("Invalid option."));
    assertTrue(output.contains("Validation errors:"));
    assertTrue(output.contains("email invalid"));
    assertTrue(output.contains("Unexpected error: unexpected failure"));
    assertTrue(output.contains("Goodbye!"));
  }
}
