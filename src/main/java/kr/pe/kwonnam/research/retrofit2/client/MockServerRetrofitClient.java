package kr.pe.kwonnam.research.retrofit2.client;

import com.fasterxml.jackson.databind.JsonNode;
import kr.pe.kwonnam.research.retrofit2.synchronous.Retry;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * MockServer 를 대상으로한 요청 Client
 */

public interface MockServerRetrofitClient {
    @GET("/get-simple")
    @Retry(maxTryCount = 3, backOffMillis = 10)
    JsonNode getSimpleWithRetry();

    @GET("/error/400")
    JsonNode error400(@Query("message") String message);

    @GET("/error/400")
    @Retry(maxTryCount = 5, backOffMillis = 10)
    JsonNode error400WithRetry(@Query("message") String message);

    @POST("/error/500")
    JsonNode error500();

    @POST("/error/500")
    @Retry(maxTryCount = 5, backOffMillis = 50)
    JsonNode error500WithRetry();

    @POST("/read-timeout")
    JsonNode readTimeout();

    @POST("/read-timeout")
    @Retry(maxTryCount = 4, backOffMillis = 7)
    JsonNode readTimeoutWithRetry();

}
