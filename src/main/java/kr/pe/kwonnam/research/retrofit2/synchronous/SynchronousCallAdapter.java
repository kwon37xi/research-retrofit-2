package kr.pe.kwonnam.research.retrofit2.synchronous;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;

import java.lang.reflect.Type;

public class SynchronousCallAdapter implements CallAdapter<Object, Object> {
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

    public static CallAdapter<Object, Object> create(Type returnType, SynchronousCallFailedResponseHandler synchronousCallFailedResponseHandler) {
        return new SynchronousCallAdapter(returnType, synchronousCallFailedResponseHandler);
    }
}
