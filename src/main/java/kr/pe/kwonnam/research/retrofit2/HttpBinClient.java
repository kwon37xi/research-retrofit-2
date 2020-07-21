package kr.pe.kwonnam.research.retrofit2;

import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

import java.util.Map;

/**
 * https://httpbin.org/
 */
public interface HttpBinClient {
    @GET("/get")

    Call<JsonNode> getSimpleGet(@Header("Custom-Header") String customHeader);
}
