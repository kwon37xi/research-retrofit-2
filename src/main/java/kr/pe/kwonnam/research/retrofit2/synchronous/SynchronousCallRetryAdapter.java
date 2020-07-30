package kr.pe.kwonnam.research.retrofit2.synchronous;

import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SynchronousCallRetryAdapter implements CallAdapter<Object, Object> {

    private final Type returnType;
    private final SynchronousCallFailedResponseHandler synchronousCallFailedResponseHandler;
    private final Retry retry;

    private SynchronousCallRetryAdapter(Type returnType, SynchronousCallFailedResponseHandler synchronousCallFailedResponseHandler, Retry retry) {
        this.returnType = returnType;
        this.synchronousCallFailedResponseHandler = synchronousCallFailedResponseHandler;
        this.retry = retry;
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
            currentTryCount++;
            if (currentTryCount > 1) {
                backOff();
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
                if (is5xx && currentTryCount < retry.maxTryCount()) {
                    log.info("request is unsuccessfull with status code : " + responseStatusCode + " and  currentTryCount: " + currentTryCount);
                    continue;
                }

                throw synchronousCallFailedResponseHandler.handleErrorResponse(response);
            } catch (IOException e) {
                log.error("call execution failed. current retry count: {}, message: {}", currentTryCount, e.getMessage(), e);
                if (currentTryCount == retry.maxTryCount()) {
                    String exceptionMessage = String.format("retrofit synchronous call failed. - %s, try count : %s.",
                        e.getMessage(), currentTryCount);

                    throw new IllegalStateException(exceptionMessage, e);
                }
                continue;
            }
        }
    }

    private void backOff() {
        if (retry.backOffMillis() <= 0) {
            return;
        }

        try {
            log.debug("backOff for {} milliseconds.", retry.backOffMillis());
            TimeUnit.MICROSECONDS.sleep(retry.backOffMillis());
        } catch (InterruptedException e) {
            log.error("Thread interrupted while backoff.", e);
            Thread.currentThread().interrupt();
        }
    }

    public static CallAdapter<Object, Object> create(Type returnType, SynchronousCallFailedResponseHandler synchronousCallFailedResponseHandler, Retry retry) {
        return new SynchronousCallRetryAdapter(returnType, synchronousCallFailedResponseHandler, retry);
    }
}
