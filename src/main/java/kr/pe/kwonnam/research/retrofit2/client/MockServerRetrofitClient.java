package kr.pe.kwonnam.research.retrofit2.client;

import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * MockServer 를 대상으로한 요청 Client
 */

public interface MockServerRetrofitClient {
    @GET("/error/400")
    JsonNode error400(@Query("message") String message);

    @POST("/error/500")
    JsonNode error500();
}
