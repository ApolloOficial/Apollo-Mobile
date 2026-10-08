package org.apollo.mobile.chat.error;

public final class ChatErrorMapper {
    private ChatErrorMapper() {
    }

    public static ChatError.Type fromStatus(int statusCode) {
        switch (statusCode) {
            case 401:
                return ChatError.Type.SESSION_EXPIRED;
            case 403:
                return ChatError.Type.FORBIDDEN;
            case 400:
            case 422:
                return ChatError.Type.INVALID_REQUEST;
            case 429:
                return ChatError.Type.RATE_LIMITED;
            case 502:
            case 503:
            case 504:
                return ChatError.Type.UNAVAILABLE;
            default:
                return statusCode >= 500 ? ChatError.Type.SERVER : ChatError.Type.INVALID_RESPONSE;
        }
    }
}
