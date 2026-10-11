package org.apollo.mobile.sync;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public final class RetrofitPendingSender implements PendingSender {
    private static final String TAG = "ApolloSync";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final SyncApiService service;

    public RetrofitPendingSender(SyncApiService service) {
        this.service = service;
    }

    @Override
    public SendResult send(PendingAction action) {
        try {
            RequestBody body = RequestBody.create(action.getBody().getBytes(java.nio.charset.StandardCharsets.UTF_8), JSON);
            Call<ResponseBody> call;
            switch (action.getMethod()) {
                case "PUT":
                    call = service.put(action.getPath(), body);
                    break;
                case "PATCH":
                    call = service.patch(action.getPath(), body);
                    break;
                default:
                    call = service.post(action.getPath(), body);
                    break;
            }
            Response<ResponseBody> response = call.execute();
            if (response.errorBody() != null) {
                response.errorBody().close();
            }
            return SyncResultMapper.fromStatus(response.code());
        } catch (IOException exception) {
            Log.w(TAG, "Falha ao enviar " + action.getPath(), exception);
            return SendResult.RETRY;
        } catch (RuntimeException exception) {
            Log.w(TAG, "Pendencia invalida " + action.getPath(), exception);
            return SendResult.REJECTED;
        }
    }
}
