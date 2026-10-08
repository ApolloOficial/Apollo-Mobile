package org.apollo.mobile.chat;

import static org.junit.Assert.assertEquals;

import org.apollo.mobile.chat.error.ChatError;
import org.apollo.mobile.chat.error.ChatErrorMapper;
import org.junit.Test;

public class ChatErrorMapperTest {
    @Test
    public void mapsAuthenticationAndAuthorizationErrors() {
        assertEquals(ChatError.Type.SESSION_EXPIRED, ChatErrorMapper.fromStatus(401));
        assertEquals(ChatError.Type.FORBIDDEN, ChatErrorMapper.fromStatus(403));
    }

    @Test
    public void mapsClientErrors() {
        assertEquals(ChatError.Type.INVALID_REQUEST, ChatErrorMapper.fromStatus(400));
        assertEquals(ChatError.Type.INVALID_REQUEST, ChatErrorMapper.fromStatus(422));
        assertEquals(ChatError.Type.RATE_LIMITED, ChatErrorMapper.fromStatus(429));
    }

    @Test
    public void mapsGatewayAndServerErrors() {
        assertEquals(ChatError.Type.UNAVAILABLE, ChatErrorMapper.fromStatus(502));
        assertEquals(ChatError.Type.UNAVAILABLE, ChatErrorMapper.fromStatus(503));
        assertEquals(ChatError.Type.UNAVAILABLE, ChatErrorMapper.fromStatus(504));
        assertEquals(ChatError.Type.SERVER, ChatErrorMapper.fromStatus(500));
    }

    @Test
    public void mapsUnexpectedStatusAsInvalidResponse() {
        assertEquals(ChatError.Type.INVALID_RESPONSE, ChatErrorMapper.fromStatus(404));
    }
}
