package org.apollo.mobile.chat.gateway;

import android.content.Context;

import org.apollo.mobile.api.ApiClient;
import org.apollo.mobile.chat.api.ChatApiService;

public final class ChatGatewayFactory {
    private static final long CHAT_TIMEOUT_SECONDS = 100;

    private ChatGatewayFactory() {
    }

    public static ChatGateway create(Context context) {
        ChatApiService service = ApiClient.createService(
                context.getApplicationContext(), ChatApiService.class, CHAT_TIMEOUT_SECONDS);
        return new RetrofitChatGateway(service);
    }
}
