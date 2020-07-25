package kr.pe.kwonnam.research.retrofit2.client;

import kr.pe.kwonnam.research.retrofit2.RetrofitClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.Delay;
import org.mockserver.model.MediaType;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
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
                // 응답 content-type 은 "application-json" 이지만, charset 을 "UTF-8"로 고정하려면, ContentType 에 문자셋을 지정하면 안된다.
                response()
                    .withStatusCode(400)
                    .withDelay(Delay.milliseconds(10))
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\n" +
                        "    \"errorCode\": \"YOUBAD\",\n" +
                        "    \"errorMessage\" : \"400 에러 메시지를 고의로 발생시켰습니다.\"\n" +
                        "}", StandardCharsets.UTF_8)
            );
    }

    @BeforeEach
    void setUp() {
        mockServerRetrofitClient = new RetrofitClientBuilder()
            .baseUrl("http://localhost:" + port)
            .connectionTimeout(Duration.ofMillis(500))
            .readTimeout(Duration.ofMillis(1000))
            .writeTimeout(Duration.ofMillis(1000))
            .build(MockServerRetrofitClient.class);
    }

    @Test
    @DisplayName("400 오류 코드 반환시 예외가 발생해야 한다.")
    void error400(SoftAssertions softly) {
        ApiRequestException customEx = catchThrowableOfType(() -> {
            mockServerRetrofitClient.error400("hello world");
        }, ApiRequestException.class);


        softly.assertThat(customEx.getErrorCode()).as("errorCode 는 json의 errorCode 필드 값이어야한다.").isEqualTo("YOUBAD");
        softly.assertThat(customEx.getMessage()).as("예외 메시지는 json의 errorMessage 필드 값이어야한다.").isEqualTo("400 에러 메시지를 고의로 발생시켰습니다.");
    }
}