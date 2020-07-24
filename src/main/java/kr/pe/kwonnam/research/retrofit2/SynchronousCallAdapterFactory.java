package kr.pe.kwonnam.research.retrofit2;

import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;

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

    private static final List<Class<?>> EXLUDE_CLASSES = List.of(Call.class, CompletableFuture.class);

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {

        if (EXLUDE_CLASSES.contains(returnType)) {
            log.debug("returnType {} is excluded from synchronousCallAdapter.", returnType);
            return null;
        }

        if (returnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) returnType;
            if (EXLUDE_CLASSES.contains(parameterizedType.getRawType())) {
                log.debug("parameterized returnType {} is excluded from synchronousCallAdapter.", returnType);
                return null;
            }
        }

        return new CallAdapter<>() {
            @Override
            public Type responseType() {
                return returnType;
            }

            @Override
            public Object adapt(Call<Object> call) {
                try {
                    Response<Object> response = call.execute();
                    return response.body();
                } catch (Exception ex) {
                    throw new IllegalStateException("retrofit synchronous call failed. - " + ex.getMessage(), ex);
                }
            }
        };
    }

    public static SynchronousCallAdapterFactory create() {
        return new SynchronousCallAdapterFactory();
    }
}
