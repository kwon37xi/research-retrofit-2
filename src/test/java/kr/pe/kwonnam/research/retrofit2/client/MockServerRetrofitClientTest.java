package kr.pe.kwonnam.research.retrofit2.client;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.mockserver.matchers.Times;
import org.mockserver.model.Delay;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
@DisplayName("MockServer 기반 테스트. httpbin 이 지원하지 못하는 부분들")
@ExtendWith({SoftAssertionsExtension.class, MockServerExtension.class})
class MockServerRetrofitClientTest {
    public static final Duration TEST_READ_WRITE_TIMEOUT_DURATION = Duration.ofMillis(1000);
    public static final Duration TEST_CONNECTION_TIMEOUT_DURATION = Duration.ofMillis(500);

    private final ClientAndServer mockServer;
    private final int port;
    private MockServerRetrofitClient mockServerRetrofitClient;

    MockServerRetrofitClientTest(ClientAndServer mockServer) {
        this.mockServer = mockServer;
        port = mockServer.getLocalPort();
    }

    @BeforeEach
    void setUp() {
        mockServer.reset();

        mockServerRetrofitClient = new RetrofitClientBuilder()
            .baseUrl("http://localhost:" + port)
            .connectionTimeout(TEST_CONNECTION_TIMEOUT_DURATION)
            .readTimeout(TEST_READ_WRITE_TIMEOUT_DURATION)
            .writeTimeout(TEST_READ_WRITE_TIMEOUT_DURATION)
            .build(MockServerRetrofitClient.class);
    }

    @Test
    @DisplayName("400 오류 코드와 함께 오류 정보 JSON 반환시 예외가 발생하고 JSON 예외 내용이 들어있어야 한다.")
    void error400(SoftAssertions softly) {
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

        ApiRequestException apiEx = catchThrowableOfType(() -> {
            mockServerRetrofitClient.error400("hello world");
        }, ApiRequestException.class);


        softly.assertThat(apiEx.getErrorCode()).as("errorCode 는 json의 errorCode 필드 값이어야한다.").isEqualTo("YOUBAD");
        softly.assertThat(apiEx.getMessage()).as("예외 메시지는 json의 errorMessage 필드 값이어야한다.").isEqualTo("400 에러 메시지를 고의로 발생시켰습니다.");
    }

    @Test
    @DisplayName("400 오류 코드는 Retry를 하지 않고 첫번째 호출에 즉시 오류 정보 JSON 반환시 예외가 발생하고 JSON 예외 내용이 들어있어야 한다.")
    void error400WithRetry(SoftAssertions softly) {
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

        ApiRequestException apiEx = catchThrowableOfType(() -> {
            mockServerRetrofitClient.error400WithRetry("hello world");
        }, ApiRequestException.class);


        softly.assertThat(apiEx.getErrorCode()).as("errorCode 는 json의 errorCode 필드 값이어야한다.").isEqualTo("YOUBAD");
        softly.assertThat(apiEx.getMessage()).as("예외 메시지는 json의 errorMessage 필드 값이어야한다.").isEqualTo("400 에러 메시지를 고의로 발생시켰습니다.");

        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/error/400"),
            VerificationTimes.exactly(1)
        );

    }

    @Test
    @DisplayName("500 오류 코드와 함께 HTML 로 오류 정보 반환시, 예외가 발생하고, 예외 HTML이 내용으로 들어있어야 한다.")
    void error500(SoftAssertions softly) {
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

    @Test
    @DisplayName("@Retry 끝까지 실패 - maxTryCount=5, backOffMillis=50 으로 5회 재시도 후에, 500 응답을 예외로 변경하여 반한한다.")
    void error500WithRetry(SoftAssertions softly) {
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

        var apiEx = catchThrowableOfType(() -> {
            mockServerRetrofitClient.error500WithRetry();
        }, ApiRequestException.class);


        softly.assertThat(apiEx.getErrorCode()).as("errorCode 는 UNKNOWN이다.")
            .isEqualTo("UNKNOWN");

        softly.assertThat(apiEx.getMessage()).as("HTML 로 전송된 내용을 저장하고 있어야한다.")
            .startsWith("<html>")
            .contains("에러가 발생하였습니다.")
            .endsWith("</html>");

        mockServer.verify(
            request()
                .withMethod("POST")
                .withPath("/error/500"),
            VerificationTimes.exactly(5)
        );
    }


    @Test
    @DisplayName("IOException 발생시에 재시도 없이 IllegalStateException 으로 변환해준다.")
    void readTimeout(SoftAssertions softly) {
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath("/read-timeout")
        ).respond(
            response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{}")
                .withDelay(Delay.seconds(TEST_READ_WRITE_TIMEOUT_DURATION.getSeconds() + 1))
        );

        var apiEx = catchThrowableOfType(() -> {
            mockServerRetrofitClient.readTimeout();
        }, IllegalStateException.class);

        softly.assertThat(apiEx).hasMessage("retrofit synchronous call failed. - timeout");
        mockServer.verify(
            request()
                .withMethod("POST")
                .withPath("/read-timeout"),
            VerificationTimes.exactly(1)
        );
    }

    @Test
    @DisplayName("IOException 발생시에 @Retry 설정만큼 재시도하고 그래도 실패하면 IllegalStateException 으로 변환해준다.")
    void readTimeoutWithRetry(SoftAssertions softly) {
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath("/read-timeout")
        ).respond(
            response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{}")
                .withDelay(Delay.seconds(TEST_READ_WRITE_TIMEOUT_DURATION.getSeconds() + 1))
        );

        var apiEx = catchThrowableOfType(() -> {
            mockServerRetrofitClient.readTimeoutWithRetry();
        }, IllegalStateException.class);

        softly.assertThat(apiEx).hasMessage("retrofit synchronous call failed. - timeout, try count : 4.");

        mockServer.verify(
            request()
                .withMethod("POST")
                .withPath("/read-timeout"),
            VerificationTimes.exactly(4)
        );
    }

    @Test
    @DisplayName("getSimple 2번 실패하고 @Retry 설정의 maxTryCount인 최종 3번째에 성공할 경우 성공으로 반환한다.")
    void getSimpleWithRetryFinallySuccess(SoftAssertions softly) {
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/get-simple"),
            Times.exactly(2)
        ).respond(
            response()
                .withStatusCode(503)
                .withContentType(MediaType.TEXT_HTML_UTF_8)
                .withBody("<html>5xx ERROR!</html>")
        );
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/get-simple"),
            Times.exactly(1)
        ).respond(
            response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{\n" +
                    "    \"greetings\": \"안녕 세상아!\",\n" +
                    "    \"age\" : 17\n" +
                    "}", StandardCharsets.UTF_8)
        );

        JsonNode jsonNode = mockServerRetrofitClient.getSimpleWithRetry();

        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/get-simple"),
            VerificationTimes.exactly(3)
        );

        softly.assertThat(jsonNode.get("greetings").asText()).isEqualTo("안녕 세상아!");
        softly.assertThat(jsonNode.get("age").asInt()).isEqualTo(17);
    }


    @Test
    @DisplayName("IOException 발생시에 2번 실패하고 @Retry 설정의 maxTryCount보다 작은3번째에 성공할 경우 성공으로 반환한다.")
    void readTimeoutWithRetryFinallySuccess(SoftAssertions softly) {
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath("/read-timeout"),
            Times.exactly(2)
        ).respond(
            response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{}")
                .withDelay(Delay.seconds(TEST_READ_WRITE_TIMEOUT_DURATION.getSeconds() + 1))
        );

        mockServer.when(
            request()
                .withMethod("POST")
                .withPath("/read-timeout"),
            Times.exactly(2)
        ).respond(
            response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{\n" +
                    "    \"greetings\": \"안녕 세상아! ReadTimeout이 안났구나!\",\n" +
                    "    \"age\" : 44\n" +
                    "}", StandardCharsets.UTF_8)
                .withDelay(Delay.milliseconds(10))
        );

        JsonNode jsonNode = mockServerRetrofitClient.readTimeoutWithRetry();

        mockServer.verify(
            request()
                .withMethod("POST")
                .withPath("/read-timeout"),
            VerificationTimes.exactly(3)
        );

        softly.assertThat(jsonNode.get("greetings").asText()).isEqualTo("안녕 세상아! ReadTimeout이 안났구나!");
        softly.assertThat(jsonNode.get("age").asInt()).isEqualTo(44);
    }
}