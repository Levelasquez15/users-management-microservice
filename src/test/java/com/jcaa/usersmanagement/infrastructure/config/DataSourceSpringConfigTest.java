package com.jcaa.usersmanagement.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockConstruction;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("DataSourceSpringConfig")
class DataSourceSpringConfigTest {

  @Test
  @DisplayName("construye el bean HikariDataSource con la configuracion SSL indicada")
  void shouldBuildDataSourceBean() {
    final DataSourceSpringConfig springConfig = new DataSourceSpringConfig();
    ReflectionTestUtils.setField(springConfig, "dbHost", "gateway01.us-east-1.prod.aws.tidbcloud.com");
    ReflectionTestUtils.setField(springConfig, "dbPort", 4000);
    ReflectionTestUtils.setField(springConfig, "dbName", "crud_usuarios");
    ReflectionTestUtils.setField(springConfig, "dbUsername", "test.root");
    ReflectionTestUtils.setField(springConfig, "dbPassword", "secret");
    ReflectionTestUtils.setField(springConfig, "dbSslMode", "VERIFY_IDENTITY");

    final HikariConfig[] capturedConfig = new HikariConfig[1];
    try (MockedConstruction<HikariDataSource> ignored =
        mockConstruction(
            HikariDataSource.class,
            (mock, context) -> capturedConfig[0] = (HikariConfig) context.arguments().get(0))) {
      final DataSource dataSource = springConfig.dataSource();
      assertNotNull(dataSource);
      assertEquals(
          "jdbc:mysql://gateway01.us-east-1.prod.aws.tidbcloud.com:4000/crud_usuarios"
              + "?sslMode=VERIFY_IDENTITY&serverTimezone=UTC&allowPublicKeyRetrieval=true",
          capturedConfig[0].getJdbcUrl());
      assertEquals("test.root", capturedConfig[0].getUsername());
    }
  }
}
