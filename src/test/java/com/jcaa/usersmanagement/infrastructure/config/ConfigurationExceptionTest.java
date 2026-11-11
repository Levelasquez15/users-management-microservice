package com.jcaa.usersmanagement.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConfigurationException")
class ConfigurationExceptionTest {

  @Test
  @DisplayName("becauseLoadFailed() preserva la causa raiz de configuracion")
  void shouldPreserveCauseOnLoadFailure() {
    final IllegalStateException cause = new IllegalStateException("missing property");

    final ConfigurationException exception = ConfigurationException.becauseLoadFailed(cause);

    assertEquals("Failed to load the application configuration.", exception.getMessage());
    assertSame(cause, exception.getCause());
  }
}
