package kr.pe.kwonnam.research.retrofit2.client;

import com.fasterxml.jackson.databind.JsonNode;
import kr.pe.kwonnam.research.retrofit2.RetrofitClientBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.assertj.core.data.TemporalUnitOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@DisplayName("HttpBinClientTest")
@ExtendWith({SoftAssertionsExtension.class})
class HttpBinClientTest {

    private HttpBinClient httpBinClient;

    @BeforeEach
    void beforeEach() {
        httpBinClient = new RetrofitClientBuilder()
            .baseUrl(HttpBinClient.HTTPBIN_BASE_URL)
            .connectionTimeout(Duration.ofMillis(500))
            .readTimeout(Duration.ofMillis(5000))
            .writeTimeout(Duration.ofMillis(5000))
            .build(HttpBinClient.class);
    }

    @Test
    void simpleGet() throws IOException {
        var expectedCustomHeader = "retrofit simpleGet test";
        JsonNode bodyJsonNode = httpBinClient.getSimpleGet(expectedCustomHeader).execute()
            .body();

        log.info("simpleGet bodyJsonNode : {}", bodyJsonNode);

        assertThat(bodyJsonNode.get("headers").get("Custom-Header").asText()).isEqualTo(expectedCustomHeader);
    }

    @Test
    @DisplayName("@Query 파라미터로 넘긴 각종 객체가 타입에 맞게 인코딩되고, 응답 JSON이 타입에 맞게 역직렬화 되는지 확인한다.")
    @SneakyThrows
    void getWithQueryParams() {
        var getResponseCall = httpBinClient.getWithQueryParams("Retrofit 시즌2",
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
        var getResponseCall = httpBinClient.getWithUnknownQueryParams("Unknown property 무시하기 테스트",
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
        var productTypeCall = httpBinClient.getProductTypeEnumJson(ProductType.LAPTOP.name());
        ProductType productType = productTypeCall.execute().body().getArgs().getProductType();
        assertThat(productType).isEqualTo(ProductType.LAPTOP);
    }

    @Test
    @DisplayName("올바르지 않은 ProductType enum을 Jackson 이 null로 역직렬화 한다.")
    @SneakyThrows
    void getProductTypeEnumJsonUnknownEnum() {
        var productTypeCall = httpBinClient.getProductTypeEnumJson("unknown");
        ProductType productType = productTypeCall.execute().body().getArgs().getProductType();

        assertThat(productType).isNull();
    }

    @Test
    @DisplayName("POST 로 JSON 을 직렬화해서 보내고 그 결과가 올바르게 들어갔는지 살펴본다.")
    @SneakyThrows
    void postJson() {

        Args postArgs = Args.builder()
            .productName("POST JSON 테스트")
            .productType(ProductType.MOBILE_PHONE)
            .quantity(98765)
            .price(new BigDecimal("123456.789"))
            .produceDate(LocalDate.of(2019, 3, 6))
            .produceTime(LocalTime.of(3, 33, 0, 123))
            .orderDateTime(LocalDateTime.of(2018, 7, 12, 13, 21, 30, 123456789))
            .expireYear(Year.of(987))
            .build();

        PostResponse postResponse = httpBinClient.postJson(postArgs).execute().body();

        var json = postResponse.getJson();
        assertThat(json).containsEntry("productName", "POST JSON 테스트");
        assertThat(json).containsEntry("productType", "MOBILE_PHONE");
        assertThat(json).containsEntry("quantity", "98765");
        assertThat(json).containsEntry("price", "123456.789");
        assertThat(json).containsEntry("produceDate", "2019-03-06");
        assertThat(json).containsEntry("produceTime", "03:33:00.000000123");
        assertThat(json).containsEntry("orderDateTime", "2018-07-12T13:21:30.123456789");
        assertThat(json).containsEntry("expireYear", "987");
    }

    @Test
    @DisplayName("EnumCodePropertyParamConverterFactory 가 작동하여 code 값으로 enum을 직렬화한다.")
    void getProductTypeWithCode() throws IOException {
        var productTypeWithCodeCall = httpBinClient.getProductTypeWithCode(ProductTypeWithCode.MOBILE_PHONE);

        var jsonNode = productTypeWithCodeCall.execute().body();
        var productTypeCode = jsonNode.get("args").get("productTypeWithCode").asText();

        assertThat(productTypeCode).isEqualTo(ProductTypeWithCode.MOBILE_PHONE.getCode());
    }

    @Test
    @DisplayName("지정된 readTimeout(5s) 보다 더 오래 delay 할 경우 readTimeout 시점에 오류를 발생시킨다.")
    void delayOver() throws IOException {
        int delaySeconds = 6;
        assertThatThrownBy(() -> httpBinClient.delay(delaySeconds).execute())
            .hasMessage("timeout");
    }

    @Test
    @DisplayName("지정된 readTimeout(5s) 보다 짧게 delay 할 경우 오류가 발생하지 않는다.")
    void delayNotOver() throws IOException {
        int delaySeconds = 4;
        JsonNode body = httpBinClient.delay(delaySeconds).execute().body();

        assertThat(body.get("url").asText()).isEqualTo("https://httpbin.org/delay/4");
    }


    @Test
    @DisplayName("동기식 호출 - 응답 타입을 Call로 감싸지 않고 직접 기술할 수 있다.")
    void syncGetWithQueryParams(SoftAssertions softly) {
        GetResponse getResponse = httpBinClient.syncGetWithQueryParams("Retrofit 시즌2 동기식 호출",
            ProductType.LAPTOP,
            1001,
            new BigDecimal("2300000"),
            LocalDate.of(2020, 7, 24),
            LocalTime.of(17, 8),
            LocalDateTime.of(2020, 7, 24, 20, 31, 48, 111),
            Year.of(2041));

        var args = getResponse.getArgs();
        softly.assertThat(args.getProductName()).as("productName이 쿼리 파라미터와 일치해야한다.")
            .isEqualTo("Retrofit 시즌2 동기식 호출");
        softly.assertThat(args.getProductType()).as("productType enum이 쿼리 파라미터와 일치해야한다.")
            .isEqualTo(ProductType.LAPTOP);
        softly.assertThat(args.getQuantity()).as("quantity가 쿼리 파라미터와 일치해야한다.")
            .isEqualTo(1001);
        softly.assertThat(args.getPrice()).isEqualTo(new BigDecimal("2300000"));
        softly.assertThat(args.getProduceDate()).isEqualTo(LocalDate.of(2020, 7, 24));
        softly.assertThat(args.getProduceTime()).isEqualTo(LocalTime.of(17, 8));
        softly.assertThat(args.getOrderDateTime()).isEqualTo("2020-07-24T20:31:48.000000111");
        softly.assertThat(args.getOrderDateTime()).isEqualTo(LocalDateTime.of(2020, 7, 24, 20, 31, 48, 111));
        softly.assertThat(args.getExpireYear()).isEqualTo(Year.of(2041));
    }

    @Test
    @DisplayName("3초 delay로 순차 실행하면 9초 이상이 걸린다.")
    void completableDelaySequentialExecution(SoftAssertions softly) throws ExecutionException, InterruptedException {
        long startTimeMillis = System.currentTimeMillis();
        httpBinClient.completableDelay(3).get();
        httpBinClient.completableDelay(3).get();
        httpBinClient.completableDelay(3).get();
        long endTimeMillis = System.currentTimeMillis();

        TemporalUnitOffset within = within(1L, ChronoUnit.SECONDS);

        assertThat(Duration.ofMillis(endTimeMillis - startTimeMillis))
            .as("9초는 무조건 넘고 대량 12초 정도 이내에 실행될것으로 기대함.")
            .isBetween(Duration.ofSeconds(9), Duration.ofSeconds(12));
    }


    @Test
    @DisplayName("3초 delay로 여러건을 동시 실행하면 3초 이상 6초 이하로 걸릴것으로 예상한다.")
    void completableDelayConcurrentExecution(SoftAssertions softly) throws ExecutionException, InterruptedException {
        long startTimeMillis = System.currentTimeMillis();
        CompletableFuture.allOf(
            httpBinClient.completableDelay(3),
            httpBinClient.completableDelay(3),
            httpBinClient.completableDelay(3),
            httpBinClient.completableDelay(3),
            httpBinClient.completableDelay(3)
        ).join();

        long endTimeMillis = System.currentTimeMillis();

        Duration elapsed = Duration.ofMillis(endTimeMillis - startTimeMillis);
        log.debug("Concurrent call elapsed : {}ms", elapsed.toMillis());
        assertThat(elapsed)
            .as("동시실행의 경우는 3초는 무조건 넘고 대량 6초 정도 이내에 실행될것으로 기대함.")
            .isBetween(Duration.ofSeconds(3), Duration.ofSeconds(6));
    }

}