package org.apollo.mobile.api;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

final class IdempotencyKeyInterceptor implements Interceptor {

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        boolean hasKey = request.header(IdempotencyPolicy.HEADER) != null;
        if (!IdempotencyPolicy.shouldAttach(request.method(), request.url().encodedPath(), hasKey)) {
            return chain.proceed(request);
        }
        Request keyed = request.newBuilder()
                .header(IdempotencyPolicy.HEADER, UUID.randomUUID().toString())
                .build();
        return chain.proceed(keyed);
    }
}
