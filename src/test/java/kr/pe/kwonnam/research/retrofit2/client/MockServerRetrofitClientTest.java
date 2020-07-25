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
import static org.assertj.core.api.Assertions.linesOf;
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

        mockServer.when(
            request()
                .withMethod("POST")
                .withPath("/error/500")
        ).respond(
            response()
                .withStatusCode(500)
                .withContentType(MediaType.TEXT_HTML_UTF_8)
                .withBody("<html>\n" +
                    "<head>\n" +
                    "    " +
                    "<title>error!</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "에러가 발생하였습니다.\n" +
                    "</body>\n" +
                    "</html>")
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
    @DisplayName("400 오류 코드와 함께 오류 정보 JSON 반환시 예외가 발생하고 JSON 예외 내용이 들어있어야 한다.")
    void error400(SoftAssertions softly) {
        ApiRequestException apiEx = catchThrowableOfType(() -> {
            mockServerRetrofitClient.error400("hello world");
        }, ApiRequestException.class);


        softly.assertThat(apiEx.getErrorCode()).as("errorCode 는 json의 errorCode 필드 값이어야한다.").isEqualTo("YOUBAD");
        softly.assertThat(apiEx.getMessage()).as("예외 메시지는 json의 errorMessage 필드 값이어야한다.").isEqualTo("400 에러 메시지를 고의로 발생시켰습니다.");
    }

    @Test
    @DisplayName("500 오류 코드와 함께 HTML 로 오류 정보 반환시, 예외가 발생하고, 예외 HTML이 내용으로 들어있어야 한다.")
    void error500(SoftAssertions softly) {
        var apiEx = catchThrowableOfType(() -> {
            mockServerRetrofitClient.error500();
        }, ApiRequestException.class);

        softly.assertThat(apiEx.getErrorCode()).as("errorCode 는 UNKNOWN이다.")
            .isEqualTo("UNKNOWN");

        softly.assertThat(apiEx.getMessage()).as("HTML 로 전송된 내용을 저장하고 있어야한다.")
            .startsWith("<html>")
            .contains("에러가 발생하였습니다.")
            .endsWith("</html>");
    }
}