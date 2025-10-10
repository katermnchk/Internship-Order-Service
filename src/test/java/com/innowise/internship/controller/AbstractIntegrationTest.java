package com.innowise.internship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.innowise.internship.repository.OrderDao;
import com.innowise.internship.repository.OrderItemDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@SpringBootTest(
    classes = com.innowise.internship.InnowiseOrdersServiceApplication.class,
    properties = {
        "AUTH_SERVICE_VALIDATE_URL=http://localhost:8089/api/v1/auth/validate",
        "USER_SERVICE_URL=http://localhost:8089/api/v1"
    }
)
@Testcontainers
@AutoConfigureMockMvc(addFilters = false)
public abstract class AbstractIntegrationTest {

  protected static WireMockServer wireMockServer = new WireMockServer(8089);

  private static final Long[] TEST_USER_IDS = {101L, 102L, 103L};

  private static String getUserServiceMockBody(Long userId) {
    return String.format("""
            {
              "data": {
                "userId": %d,
                "userName": "TestUser",
                "userSurname": "TestSurname",
                "userBirthDate": "2000-01-01",
                "userEmail": "test%d@example.com",
                "cards": []
              },
              "status": 200,
              "message": "User fetched successfully"
            }
            """, userId, userId);
  }

  static {
    startWireMockServer();
  }

  private static void startWireMockServer() {
    if (!wireMockServer.isRunning()) {
      wireMockServer.start();
    }

    wireMockServer.stubFor(get(urlEqualTo("/api/v1/auth/validate"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"valid\": true}")));

    for (Long userId : TEST_USER_IDS) {
      wireMockServer.stubFor(get(urlPathEqualTo("/api/v1/users/" + userId))
          .willReturn(aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(getUserServiceMockBody(userId))));
    }

    wireMockServer.stubFor(get(urlPathMatching("/api/v1/users[0-9]+"))
        .willReturn(aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"status\":404,\"message\":\"User not found in tests\"}")));
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("AUTH_SERVICE_VALIDATE_URL", () -> "http://localhost:8089/api/v1/auth/validate");
  }

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected OrderDao orderDao;

  @Autowired
  protected OrderItemDao orderItemDao;

  @Autowired
  protected JdbcTemplate jdbcTemplate;

  @ServiceConnection
  public static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
      new PostgreSQLContainer<>("postgres:15")
          .withDatabaseName("testdb")
          .withUsername(System.getenv().getOrDefault("DB_USER", "test"))
          .withPassword(System.getenv().getOrDefault("DB_PASSWORD", "test"));
}
