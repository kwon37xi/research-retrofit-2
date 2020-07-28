package kr.pe.kwonnam.research.retrofit2.synchronous;

import retrofit2.Response;

public interface SynchronousCallFailedResponseHandler {

    RuntimeException handleErrorResponse(Response<Object> response);
}
