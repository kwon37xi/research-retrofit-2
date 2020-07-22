package kr.pe.kwonnam.research.retrofit2.client;

import com.fasterxml.jackson.databind.JsonNode;
import kr.pe.kwonnam.research.retrofit2.RetrofitHttpBinClientBuilder;
import kr.pe.kwonnam.research.retrofit2.client.GetResponse;
import kr.pe.kwonnam.research.retrofit2.client.HttpBinClient;
import kr.pe.kwonnam.research.retrofit2.client.ProductType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("HttpBinClientTest")
class HttpBinClientTest {

    private HttpBinClient httpBinQueryParamClient;

    @BeforeAll
    static void beforeAll() {
        // okhttp 가 java.util.logging 을 사용하기 때문에 처리해줌.
//        SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)
//        SLF4JBridgeHandler.install();
    }

    @BeforeEach
    void beforeEach() {
        httpBinQueryParamClient = new RetrofitHttpBinClientBuilder()
            .createHttpBinClient(HttpBinClient.class, HttpBinClient.HTTPBIN_BASE_URL, Duration.ofMillis(1000), Duration.ofMillis(5000), Duration.ofMillis(5000));
    }

    @Test
    void simpleGet() throws IOException {
        var expectedCustomHeader = "restrofit simpleGet test";
        JsonNode bodyJsonNode = httpBinQueryParamClient.getSimpleGet(expectedCustomHeader).execute()
            .body();

        log.info("simpleGet bodyJsonNode : {}", bodyJsonNode);

        assertThat(bodyJsonNode.get("headers").get("Custom-Header").asText()).isEqualTo(expectedCustomHeader);
    }

    @SneakyThrows
    @Test
    @DisplayName("@Query 파라미터로 넘긴 각종 객체가 타입에 맞게 인코딩되고, 응답 JSON이 타입에 맞게 역직렬화 되는지 확인한다.")
    void getWithQueryParams() {
        var getResponseCall = httpBinQueryParamClient.getWithQueryParams("Retrofit 시즌2",
            ProductType.CAR,
            123,
            new BigDecimal("99000.87"),
            LocalDate.of(2020, 3, 27),
            LocalTime.of(8, 5),
            LocalDateTime.of(2020, 7, 22, 14, 12, 31, 789),
            Year.of(2030));

        GetResponse getResponse = getResponseCall.execute().body();
        log.info("getResponseCall : {}", getResponse);

        var args = getResponse.getArgs();
        assertThat(args.getProductName()).isEqualTo("Retrofit 시즌2");
        assertThat(args.getOrderDateTime()).isEqualTo("2020-07-22T14:12:31.000000789");

    }
}


// todo : 응답 JSON 에 필드가 있지만 응답 Dto 클래스에 해당 필드 없는 경우 무시 Test