package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConsoleIO")
class ConsoleIOTest {

  @Test
  @DisplayName("lee valores requeridos, opcionales y enteros manejando entradas invalidas")
  void shouldReadInputsAndWriteFormattedOutputs() {
    final String input = "  \nAda Lovelace\noptional-value\nabc\n42\n";
    final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    final ConsoleIO console =
        new ConsoleIO(
            new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
            new PrintStream(outBytes, true, StandardCharsets.UTF_8));

    assertEquals("Ada Lovelace", console.readRequired("Name: "));
    assertEquals("optional-value", console.readOptional("Opt: "));
    assertEquals(42, console.readInt("Number: "));

    console.println("hello");
    console.println();
    console.printf("%s-%d", "ok", 1);

    final String printed = outBytes.toString(StandardCharsets.UTF_8);
    assertTrue(printed.contains("Value cannot be blank."));
    assertTrue(printed.contains("Invalid input."));
    assertTrue(printed.contains("hello"));
    assertTrue(printed.contains("ok-1"));
  }
}
