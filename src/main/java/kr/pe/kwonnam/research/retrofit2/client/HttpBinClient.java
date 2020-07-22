package kr.pe.kwonnam.research.retrofit2.client;

import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;

/**
 * https://httpbin.org/
 */
public interface HttpBinClient {
    String HTTPBIN_BASE_URL = "https://httpbin.org";

    @GET("/get")
    Call<JsonNode> getSimpleGet(@Header("Custom-Header") String customHeader);

    /**
     * 요청 Query 파라미터가 올바로 인코딩 되는지, 응답으로 넘어온 JSON 이 올바른 객체로 역직렬화 되는지 확인한다.
     */
    @GET("/get")
    Call<GetResponse> getWithQueryParams(@Query("productName") String productName,
                                         @Query("productType") ProductType productType,
                                         @Query("quantity") int quantity,
                                         @Query("price") BigDecimal price,
                                         @Query("produceDate") LocalDate produceDate,
                                         @Query("produceTime") LocalTime produceTime,
                                         @Query("orderDateTime") LocalDateTime orderDateTime,
                                         @Query("expireYear") Year expireYear);
}
