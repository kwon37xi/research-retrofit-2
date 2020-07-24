package kr.pe.kwonnam.research.retrofit2.client;

import com.fasterxml.jackson.databind.JsonNode;
import kr.pe.kwonnam.research.retrofit2.RetrofitHttpBinClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.Delay;
import org.mockserver.model.MediaType;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
@DisplayName("MockServer 기반 테스트. httpbin 이 지원하지 못하는 부분들")
@ExtendWith({SoftAssertionsExtension.class, MockServerExtension.class})
class MockServerRetrofitClientTest {
    private final ClientAndServer mockServer;
    private final int port;
    private MockServerRetrofitClient mockServerRetrofitClient;

    MockServerRetrofitClientTest(ClientAndServer mockServer) {
        this.mockServer = mockServer;
        port = mockServer.getLocalPort();

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/error/400")
                .withQueryStringParameter("message", "hello world")
        )
            .respond(
                response()
                    .withDelay(Delay.milliseconds(10))
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\n" +
                        "    \"errorCode\": \"YOUBAD\",\n" +
                        "    \"errorMessage\" : \"400 에러 메시지를 고의로 발생시켰습니다.\"\n" +
                        "}")
            );
    }

    @BeforeEach
    void setUp() {
        mockServerRetrofitClient = new RetrofitHttpBinClientBuilder()
            .baseUrl("http://localhost:" + port)
            .connectionTimeout(Duration.ofMillis(500))
            .readTimeout(Duration.ofMillis(1000))
            .writeTimeout(Duration.ofMillis(1000))
            .build(MockServerRetrofitClient.class);
    }

    @Test
    void error400() {
        JsonNode jsonNode = mockServerRetrofitClient.error400("hello world");

        String errorCode = jsonNode.get("errorCode").asText();
        String errorMessage = jsonNode.get("errorMessage").asText();

        assertThat(errorCode).isEqualTo("");
    }
}