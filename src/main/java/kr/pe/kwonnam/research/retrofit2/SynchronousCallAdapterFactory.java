package kr.pe.kwonnam.research.retrofit2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.pe.kwonnam.research.retrofit2.client.ApiRequestException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 기본 Async 인 retrofit 대신, 동기식으로 처리하고, 응답 객체를 {@link retrofit2.Call} 등으로 감싸지 않고
 * 곧바로 지정가능한 {@link retrofit2.CallAdapter.Factory}.
 */
@Slf4j
public class SynchronousCallAdapterFactory extends CallAdapter.Factory {

    private static final List<Class<?>> EXCLUDE_CLASSES = List.of(Call.class, CompletableFuture.class);

    private final ObjectMapper errorResponseObjectMapper;

    public SynchronousCallAdapterFactory() {
        errorResponseObjectMapper = new ObjectMapper();
    }


    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {

        if (EXCLUDE_CLASSES.contains(returnType)) {
            log.debug("returnType {} is excluded from synchronousCallAdapter.", returnType);
            return null;
        }

        if (returnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) returnType;
            if (EXCLUDE_CLASSES.contains(parameterizedType.getRawType())) {
                log.debug("parameterized returnType {} is excluded from synchronousCallAdapter.", returnType);
                return null;
            }
        }

        return new SynchronousCallAdapter(returnType);
    }

    public static SynchronousCallAdapterFactory create() {
        return new SynchronousCallAdapterFactory();
    }

    private class SynchronousCallAdapter implements CallAdapter<Object, Object> {
        private final Type returnType;

        public SynchronousCallAdapter(Type returnType) {
            this.returnType = returnType;
        }

        @Override
        public Type responseType() {
            return returnType;
        }

        @Override
        public Object adapt(Call<Object> call) {
            try {
                Response<Object> response = call.execute();

                if (response.isSuccessful()) {
                    return response.body();
                }
                return handleErrorResponse(response);
            } catch (ApiRequestException customEx) {
                throw customEx;
            } catch (Exception ex) {
                throw new IllegalStateException("retrofit synchronous call failed. - " + ex.getMessage(), ex);
            }
        }

        private Object handleErrorResponse(Response<Object> response) throws IOException {
            int responseStatusCode = response.code();
            MediaType errorMediaType = response.errorBody().contentType();
            String errorBodyString = response.errorBody().string();

            log.debug("failure status code : {}, contentType: {} type : {} , subtype : {}, errorBody : {}",
                responseStatusCode, errorMediaType, errorMediaType.type(), errorMediaType.subtype(), errorBodyString);

            if (errorMediaType.subtype().equals("json")) {
                JsonNode jsonNode = errorResponseObjectMapper.reader().readTree(errorBodyString);
                String errorCode = jsonNode.get("errorCode").asText();
                String errorMessage = jsonNode.get("errorMessage").asText();

                throw new ApiRequestException(responseStatusCode, errorCode, errorMessage);
            }
            throw new ApiRequestException(responseStatusCode, "UNKNOWN", errorBodyString);
        }
    }
}
