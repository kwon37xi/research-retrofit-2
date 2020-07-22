package kr.pe.kwonnam.research.retrofit2.client;

import com.fasterxml.jackson.databind.JsonNode;
import kr.pe.kwonnam.research.retrofit2.RetrofitHttpBinClientBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import retrofit2.Call;

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

    @Test
    @DisplayName("@Query 파라미터로 넘긴 각종 객체가 타입에 맞게 인코딩되고, 응답 JSON이 타입에 맞게 역직렬화 되는지 확인한다.")
    @SneakyThrows
    void getWithQueryParams() {
        var getResponseCall = httpBinQueryParamClient.getWithQueryParams("Retrofit 시즌2",
            ProductType.CAR,
            123,
            new BigDecimal("99000.87"),
            LocalDate.of(2020, 3, 27),
            LocalTime.of(8, 17),
            LocalDateTime.of(2020, 7, 22, 14, 12, 31, 789),
            Year.of(2030));

        GetResponse getResponse = getResponseCall.execute().body();
        log.info("getResponseCall : {}", getResponse);

        var args = getResponse.getArgs();
        assertThat(args.getProductName()).isEqualTo("Retrofit 시즌2");
        assertThat(args.getProductType()).isEqualTo(ProductType.CAR);
        assertThat(args.getQuantity()).isEqualTo(123);
        assertThat(args.getPrice()).isEqualTo(new BigDecimal("99000.87"));
        assertThat(args.getProduceDate()).isEqualTo(LocalDate.of(2020, 3, 27));
        assertThat(args.getProduceTime()).isEqualTo(LocalTime.of(8, 17));
        assertThat(args.getOrderDateTime()).isEqualTo("2020-07-22T14:12:31.000000789");
        assertThat(args.getOrderDateTime()).isEqualTo(LocalDateTime.of(2020, 7, 22, 14, 12, 31, 789));
        assertThat(args.getExpireYear()).isEqualTo(Year.of(2030));
    }


    @Test
    @DisplayName("Args 클래스에 존재하지 않는 프라퍼티 응답을 받아도 역직렬화시에 오류가 발생하지 않게 Jackson 설정이 돼 있어야한다.")
    @SneakyThrows
    void getWithQueryParamsIgnoreUnknown() {
        var getResponseCall = httpBinQueryParamClient.getWithUnknownQueryParams("Unknown property 무시하기 테스트",
            ProductType.CAR,
            123,
            new BigDecimal("99000.87"),
            LocalDate.of(2020, 3, 27),
            LocalTime.of(8, 17),
            LocalDateTime.of(2020, 7, 22, 14, 12, 31, 789),
            Year.of(2030),
            "unknown property!");

        GetResponse getResponse = getResponseCall.execute().body();
        log.info("getResponseCall : {}", getResponse);

        var args = getResponse.getArgs();
        assertThat(args.getProductName()).isEqualTo("Unknown property 무시하기 테스트");
    }

    @Test
    @DisplayName("올바른 ProductType enum을 Jackson 이 역직렬화 한다.")
    @SneakyThrows
    void getProductTypeEnumJson() {
        var productTypeCall = httpBinQueryParamClient.getProductTypeEnumJson(ProductType.LAPTOP.name());
        ProductType productType = productTypeCall.execute().body().getArgs().getProductType();
        assertThat(productType).isEqualTo(ProductType.LAPTOP);
    }

    @Test
    @DisplayName("올바르지 않은 ProductType enum을 Jackson 이 null로 역직렬화 한다.")
    @SneakyThrows
    void getProductTypeEnumJsonUnknownEnum() {
        var productTypeCall = httpBinQueryParamClient.getProductTypeEnumJson("unknown");
        ProductType productType = productTypeCall.execute().body().getArgs().getProductType();

        assertThat(productType).isNull();
    }
}


// todo : 응답 JSON 에 필드가 있지만 응답 Dto 클래스에 해당 필드 없는 경우 무시 Test