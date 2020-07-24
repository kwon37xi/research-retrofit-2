package kr.pe.kwonnam.research.retrofit2.client;

import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.Call;
import retrofit2.http.*;

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

    /**
     * 의도적으로 {@link Args}에 없는 property {@code unknown}을 추가하여 Jackson 역직렬화시 오류발생 안하는 설정을 테스트한다.
     */
    @GET("/get")
    Call<GetResponse> getWithUnknownQueryParams(@Query("productName") String productName,
                                                @Query("productType") ProductType productType,
                                                @Query("quantity") int quantity,
                                                @Query("price") BigDecimal price,
                                                @Query("produceDate") LocalDate produceDate,
                                                @Query("produceTime") LocalTime produceTime,
                                                @Query("orderDateTime") LocalDateTime orderDateTime,
                                                @Query("expireYear") Year expireYear,
                                                @Query("unknown") String unknown);

    /**
     * ProductType enum 을 JSON 이 역직렬화 할 때 존재하지 않는 name 은 null로 반환하는지 여부 검사.
     * @param productType
     * @return
     */
    @GET("/get")
    Call<GetResponse> getProductTypeEnumJson(@Query("productType") String productType);

    @POST("/post")
    Call<PostResponse> postJson(@Body Args postArgs);

    @GET("/get")
    Call<JsonNode> getProductTypeWithCode(@Query("productTypeWithCode") ProductTypeWithCode productTypeWithCode);

    @GET("/delay/{delaySeconds}")
    Call<JsonNode> delay(@Path("delaySeconds") int delaySeconds);
}
