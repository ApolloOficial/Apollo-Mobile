package org.apollo.mobile.chat.gateway;

import org.apollo.mobile.chat.error.ChatError;

public interface ChatGateway {
    void send(String sessionId, String message, Callback<ChatReply> callback);

    void close(String sessionId, Callback<Void> callback);

    interface Callback<T> {
        void onSuccess(T result);

        void onFailure(ChatError error);
    }
}
