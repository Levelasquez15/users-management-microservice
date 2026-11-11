package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UserResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserResponsePrinter")
class UserResponsePrinterTest {

  @Test
  @DisplayName("imprime usuario individual, lista no vacia y mensaje cuando la lista esta vacia")
  void shouldPrintUsersAndEmptyListMessage() {
    final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    final ConsoleIO console =
        new ConsoleIO(
            new Scanner(new ByteArrayInputStream(new byte[0])),
            new PrintStream(outBytes, true, StandardCharsets.UTF_8));
    final UserResponsePrinter printer = new UserResponsePrinter(console);
    final UserResponse user =
        new UserResponse("1", "Ada", "ada@example.com", "ADMIN", "ACTIVE");

    printer.printList(List.of());
    printer.printList(List.of(user));

    final String output = outBytes.toString(StandardCharsets.UTF_8);
    assertTrue(output.contains("No users found."));
    assertTrue(output.contains("Total: 1 user(s)"));
    assertTrue(output.contains("ada@example.com"));
  }
}
