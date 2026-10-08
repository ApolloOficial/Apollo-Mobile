package org.apollo.mobile.chat.gateway;

import java.util.Collections;
import java.util.List;

public final class ChatReply {
    private String sessionId;
    private String answer;
    private String status;
    private String route;
    private List<String> agents;
    private List<ChatSource> sources;
    private String securityAlert;
    private String blockReason;

    public String getSessionId() {
        return sessionId;
    }

    public String getAnswer() {
        return answer == null ? "" : answer;
    }

    public String getStatus() {
        return status;
    }

    public String getRoute() {
        return route;
    }

    public List<String> getAgents() {
        return agents == null ? Collections.emptyList() : agents;
    }

    public List<ChatSource> getSources() {
        return sources == null ? Collections.emptyList() : sources;
    }

    public String getSecurityAlert() {
        return securityAlert;
    }

    public String getBlockReason() {
        return blockReason;
    }
}
