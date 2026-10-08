package org.apollo.mobile.chat.api;

import org.apollo.mobile.chat.gateway.ChatMessageRequest;
import org.apollo.mobile.chat.gateway.ChatReply;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApiService {
    @POST("api/v1/chat/messages")
    Call<ChatReply> send(@Body ChatMessageRequest request);

    @POST("api/v1/chat/sessions/{sessionId}/close")
    Call<Void> close(@Path("sessionId") String sessionId);
}
