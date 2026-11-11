package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MenuOption")
class MenuOptionTest {

  @Test
  @DisplayName("fromNumber() resuelve opciones validas y retorna vacio para opciones desconocidas")
  void shouldResolveMenuOptionsByNumber() {
    assertEquals(MenuOption.LIST_USERS, MenuOption.fromNumber(1).orElseThrow());
    assertEquals(MenuOption.EXIT, MenuOption.fromNumber(0).orElseThrow());
    assertEquals("Exit", MenuOption.EXIT.getDescription());
    assertTrue(MenuOption.fromNumber(99).isEmpty());
  }
}
