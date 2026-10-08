package org.apollo.mobile.view.chat;

import androidx.annotation.StringRes;

import org.apollo.mobile.R;
import org.apollo.mobile.chat.error.ChatError;

final class ChatMessages {
    private ChatMessages() {
    }

    @StringRes
    static int error(ChatError error) {
        switch (error.getType()) {
            case NETWORK:
                return R.string.chat_error_network;
            case TIMEOUT:
                return R.string.chat_error_timeout;
            case RATE_LIMITED:
                return R.string.chat_error_rate_limited;
            case FORBIDDEN:
                return R.string.chat_error_forbidden;
            case INVALID_REQUEST:
                return R.string.chat_error_invalid;
            case UNAVAILABLE:
                return R.string.chat_error_unavailable;
            default:
                return R.string.chat_error_server;
        }
    }
}
