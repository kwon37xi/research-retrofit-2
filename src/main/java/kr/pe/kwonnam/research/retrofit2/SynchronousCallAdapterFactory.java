package kr.pe.kwonnam.research.retrofit2;

import lombok.extern.slf4j.Slf4j;
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

        // TODO 메소드 Annotation 으로 응답 retry 설정할 수 있게 처리
        return new SynchronousCallAdapter(returnType, new DefaultSynchronousCallFailedResponseHandler());
    }

    public static SynchronousCallAdapterFactory create() {
        return new SynchronousCallAdapterFactory();
    }

    // todo failedResponseHandler 한번만 생성해서 주입하게 처리
    private class SynchronousCallAdapter implements CallAdapter<Object, Object> {
        private final Type returnType;
        private final SynchronousCallFailedResponseHandler synchronousCallFailedResponseHandler;

        public SynchronousCallAdapter(Type returnType, SynchronousCallFailedResponseHandler synchronousCallFailedResponseHandler) {
            this.returnType = returnType;
            this.synchronousCallFailedResponseHandler = synchronousCallFailedResponseHandler;
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
                throw synchronousCallFailedResponseHandler.handleErrorResponse(response);
            } catch (RuntimeException customEx) {
                throw customEx;
            } catch (Exception ex) {
                throw new IllegalStateException("retrofit synchronous call failed. - " + ex.getMessage(), ex);
            }
        }

    }

    // todo 요청 하나하나마다 새로 생성되는 객체인지 확인.
    private class SynchronousCallRetryAdapter implements CallAdapter<Object, Object> {

        private final Type returnType;
        private final SynchronousCallFailedResponseHandler synchronousCallFailedResponseHandler;
        private final int maxTryCount;

        private SynchronousCallRetryAdapter(Type returnType, SynchronousCallFailedResponseHandler synchronousCallFailedResponseHandler, int maxTryCount) {
            this.returnType = returnType;
            this.synchronousCallFailedResponseHandler = synchronousCallFailedResponseHandler;
            this.maxTryCount = maxTryCount;
        }

        @Override
        public Type responseType() {
            return returnType;
        }

        @Override
        public Object adapt(Call<Object> call) {
            int currentTryCount = 0;

            Call<Object> currentCall = call;
            while (true) {

                if (currentTryCount > 1) {
                    log.debug("current call is executed. clone and execute again. currentTryCount : {}", currentTryCount);
                    currentCall = call.clone();
                }

                try {
                    Response<Object> response = currentCall.execute();
                    if (response.isSuccessful()) {
                        return response.body();
                    }

                    int responseStatusCode = response.code();
                    boolean is5xx = responseStatusCode >= 500 && responseStatusCode < 600;
                    if (is5xx && currentTryCount < maxTryCount) {
                        log.info("request is unsuccessfull with status code : " + responseStatusCode + " and  currentTryCount: " + currentTryCount);
                        continue;
                    }

                    throw synchronousCallFailedResponseHandler.handleErrorResponse(response);
                } catch (IOException e) {
                    log.error("call execution failed. current retry count: {}, message: {}", currentTryCount, e.getMessage(), e);
                    if (currentTryCount == maxTryCount) {
                        throw new IllegalStateException("HTTP call execution failed. tryCount: " + currentTryCount + ".", e);
                    }
                    continue;
                }
            }
        }
    }
}
