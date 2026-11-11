package com.jcaa.usersmanagement.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.jcaa.usersmanagement.infrastructure.adapter.email.BrevoConfig;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("BrevoSpringConfig")
class BrevoSpringConfigTest {

  @Test
  @DisplayName("construye BrevoConfig y RestClient con las propiedades configuradas")
  void shouldBuildBrevoConfigAndRestClientBeans() {
    final BrevoSpringConfig springConfig = new BrevoSpringConfig();

    final BrevoConfig brevoConfig =
        springConfig.brevoConfig(
            "https://api.brevo.com",
            "xkeysib-test-spring-key",
            "sender@example.com",
            "Gestion de Usuarios",
            Duration.ofSeconds(2),
            Duration.ofSeconds(5),
            3,
            Duration.ofMillis(500),
            2.0,
            50,
            10,
            5,
            Duration.ofSeconds(30));

    final RestClient restClient = springConfig.brevoRestClient(brevoConfig);

    assertEquals("https://api.brevo.com", brevoConfig.baseUrl());
    assertEquals("sender@example.com", brevoConfig.fromAddress());
    assertEquals(3, brevoConfig.retryMaxAttempts());
    assertNotNull(restClient);
  }
}
