package kr.pe.kwonnam.research.retrofit2.synchronous;

import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 기본 Async 인 retrofit 대신, 동기식으로 처리하고, 응답 객체를 {@link retrofit2.Call} 등으로 감싸지 않고
 * 곧바로 지정가능한 {@link retrofit2.CallAdapter.Factory}.
 */
@Slf4j
public class SynchronousCallAdapterFactory extends CallAdapter.Factory {

    private static final List<Class<?>> EXCLUDE_CLASSES = List.of(Call.class, CompletableFuture.class);

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

        DefaultSynchronousCallFailedResponseHandler synchronousCallFailedResponseHandler = new DefaultSynchronousCallFailedResponseHandler();

        Retry retryAnnotation = getRetryAnnotation(annotations);

        if (retryAnnotation == null) {
            return SynchronousCallAdapter.create(returnType, synchronousCallFailedResponseHandler);
        }
        return SynchronousCallRetryAdapter.create(returnType, synchronousCallFailedResponseHandler, retryAnnotation);
    }

    private Retry getRetryAnnotation(Annotation[] annotations) {
        return (Retry) Arrays.stream(annotations)
            .filter(annotation -> annotation.annotationType() == Retry.class)
            .findFirst()
            .orElse(null);
    }

    public static SynchronousCallAdapterFactory create() {
        return new SynchronousCallAdapterFactory();
    }

}
