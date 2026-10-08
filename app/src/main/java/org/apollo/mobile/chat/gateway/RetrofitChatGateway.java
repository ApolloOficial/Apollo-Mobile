package org.apollo.mobile.chat.gateway;

import android.util.Log;

import org.apollo.mobile.chat.api.ChatApiService;
import org.apollo.mobile.chat.error.ChatError;
import org.apollo.mobile.chat.error.ChatErrorMapper;

import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Response;

public final class RetrofitChatGateway implements ChatGateway {
    private static final String TAG = "ApolloChat";

    private final ChatApiService chatApiService;

    public RetrofitChatGateway(ChatApiService chatApiService) {
        this.chatApiService = chatApiService;
    }

    @Override
    public void send(String sessionId, String message, Callback<ChatReply> callback) {
        enqueue(() -> chatApiService.send(new ChatMessageRequest(sessionId, message)), callback, false);
    }

    @Override
    public void close(String sessionId, Callback<Void> callback) {
        enqueue(() -> chatApiService.close(sessionId), callback, true);
    }

    private interface CallFactory<T> {
        Call<T> create();
    }

    private <T> void enqueue(CallFactory<T> factory, Callback<T> callback, boolean emptyBodyAllowed) {
        try {
            factory.create().enqueue(new retrofit2.Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null || emptyBodyAllowed) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onFailure(new ChatError(ChatError.Type.INVALID_RESPONSE));
                        }
                        return;
                    }
                    callback.onFailure(new ChatError(ChatErrorMapper.fromStatus(response.code())));
                }

                @Override
                public void onFailure(Call<T> call, Throwable throwable) {
                    Log.w(TAG, "Falha na chamada " + call.request().url().encodedPath(), throwable);
                    ChatError.Type type = throwable instanceof SocketTimeoutException
                            ? ChatError.Type.TIMEOUT
                            : ChatError.Type.NETWORK;
                    callback.onFailure(new ChatError(type));
                }
            });
        } catch (RuntimeException exception) {
            callback.onFailure(new ChatError(ChatError.Type.NETWORK));
        }
    }
}
