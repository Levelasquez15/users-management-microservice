package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.domain.exception.InvalidCredentialsException;
import com.jcaa.usersmanagement.domain.exception.UserAlreadyExistsException;
import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.UserResponsePrinter;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.CreateUserRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.LoginRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UpdateUserRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UserResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CLI Operation Handlers")
class CliHandlersTest {

  private static final UserResponse SAMPLE =
      new UserResponse("1", "Ada", "ada@example.com", "ADMIN", "ACTIVE");

  @Test
  @DisplayName("ejecutan rutas exitosas y manejo de excepciones para todos los handlers")
  void shouldExecuteHappyAndErrorPathsForAllHandlers() {
    final UserController controller = mock(UserController.class);
    final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    final ConsoleIO console =
        consoleFor(
            String.join(
                "\n",
                // FindUserById ok + not found
                "1", "missing",
                // CreateUser ok + conflict
                "1", "Ada", "ada@example.com", "Secret123!", "ADMIN",
                "1", "Ada", "ada@example.com", "Secret123!", "ADMIN",
                // UpdateUser blank password + explicit password + not found
                "1", "Ada", "ada@example.com", "", "ADMIN", "ACTIVE",
                "missing", "Ada", "ada@example.com", "NewPass123!", "ADMIN", "ACTIVE",
                // DeleteUser ok + not found
                "1", "missing",
                // Login ok + invalid
                "ada@example.com", "Secret123!",
                "ada@example.com", "bad",
                ""),
            outBytes);
    final UserResponsePrinter printer = new UserResponsePrinter(console);

    when(controller.listAllUsers()).thenReturn(List.of(SAMPLE));
    new ListUsersHandler(controller, printer).handle();
    verify(controller).listAllUsers();

    when(controller.findUserById("1")).thenReturn(SAMPLE);
    when(controller.findUserById("missing"))
        .thenThrow(UserNotFoundException.becauseIdWasNotFound("missing"));
    final FindUserByIdHandler findHandler = new FindUserByIdHandler(controller, console, printer);
    findHandler.handle();
    findHandler.handle();

    final CreateUserRequest createReq =
        new CreateUserRequest("1", "Ada", "ada@example.com", "Secret123!", "ADMIN");
    when(controller.createUser(createReq))
        .thenReturn(SAMPLE)
        .thenThrow(UserAlreadyExistsException.becauseEmailAlreadyExists("ada@example.com"));
    final CreateUserHandler createHandler = new CreateUserHandler(controller, console, printer);
    createHandler.handle();
    createHandler.handle();

    when(controller.updateUser(
            new UpdateUserRequest("1", "Ada", "ada@example.com", null, "ADMIN", "ACTIVE")))
        .thenReturn(SAMPLE);
    when(controller.updateUser(
            new UpdateUserRequest(
                "missing", "Ada", "ada@example.com", "NewPass123!", "ADMIN", "ACTIVE")))
        .thenThrow(UserNotFoundException.becauseIdWasNotFound("missing"));
    final UpdateUserHandler updateHandler = new UpdateUserHandler(controller, console, printer);
    updateHandler.handle();
    updateHandler.handle();

    doNothing().when(controller).deleteUser("1");
    doThrow(UserNotFoundException.becauseIdWasNotFound("missing"))
        .when(controller)
        .deleteUser("missing");
    final DeleteUserHandler deleteHandler = new DeleteUserHandler(controller, console);
    deleteHandler.handle();
    deleteHandler.handle();

    when(controller.login(new LoginRequest("ada@example.com", "Secret123!"))).thenReturn(SAMPLE);
    when(controller.login(new LoginRequest("ada@example.com", "bad")))
        .thenThrow(InvalidCredentialsException.becauseCredentialsAreInvalid());
    final LoginHandler loginHandler = new LoginHandler(controller, console, printer);
    loginHandler.handle();
    loginHandler.handle();

    final String output = outBytes.toString(StandardCharsets.UTF_8);
    assertTrue(output.contains("User created successfully."));
    assertTrue(output.contains("User updated successfully."));
    assertTrue(output.contains("User deleted successfully."));
    assertTrue(output.contains("Login successful. Welcome!"));
    assertTrue(output.contains("Not found:"));
  }

  private static ConsoleIO consoleFor(final String input, final ByteArrayOutputStream outBytes) {
    return new ConsoleIO(
        new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
        new PrintStream(outBytes, true, StandardCharsets.UTF_8));
  }
}
