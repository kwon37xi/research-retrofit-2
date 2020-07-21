package kr.pe.kwonnam.research.retrofit2;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("HttpBinClientTest")
class HttpBinClientTest {

    private static HttpBinClient httpBinClient;

    @BeforeAll
    static void beforeAll() {
        httpBinClient = RetrofitHttpBinClientBuilder.createHttpBinClient();
    }

    @Test
    void simpleGet() throws IOException {
        var expectedCustomHeader = "restrofit simpleGet test";
        JsonNode bodyJsonNode = httpBinClient.getSimpleGet(expectedCustomHeader).execute()
            .body();

        log.info("simpleGet bodyJsonNode : {}", bodyJsonNode);

        assertThat(bodyJsonNode.get("headers").get("Custom-Header").asText()).isEqualTo(expectedCustomHeader);
    }
}