package kr.pe.kwonnam.research.retrofit2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.pe.kwonnam.research.retrofit2.client.ApiRequestException;
import kr.pe.kwonnam.research.retrofit2.synchronous.SynchronousCallFailedResponseHandler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;

@Slf4j
public class DefaultSynchronousCallFailedResponseHandler implements SynchronousCallFailedResponseHandler {
    private final ObjectMapper failedResponseObjectMapper;

    public DefaultSynchronousCallFailedResponseHandler() {
        failedResponseObjectMapper = new ObjectMapper();
    }

    @Override
    public RuntimeException handleErrorResponse(Response<Object> response) {
        int responseStatusCode = response.code();
        ResponseBody errorBody = response.errorBody();
        MediaType errorMediaType = errorBody.contentType();
        String errorBodyString = null;
        try {
             errorBodyString = errorBody.string();

            log.debug("failure status code : {}, contentType: {} type : {} , subtype : {}, errorBody : {}",
                responseStatusCode, errorMediaType, errorMediaType.type(), errorMediaType.subtype(), errorBodyString);

            if (errorMediaType.subtype().equals("json")) {
                JsonNode jsonNode = failedResponseObjectMapper.reader().readTree(errorBodyString);
                String errorCode = jsonNode.get("errorCode").asText();
                String errorMessage = jsonNode.get("errorMessage").asText();

                return new ApiRequestException(responseStatusCode, errorCode, errorMessage);
            }
            return new ApiRequestException(responseStatusCode, "UNKNOWN", errorBodyString);
        } catch (IOException ex) {
            throw new IllegalStateException("Parsing error response failed. errorBody: " + errorBodyString, ex);
        }
    }
}
