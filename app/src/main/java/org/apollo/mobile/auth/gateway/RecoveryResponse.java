package org.apollo.mobile.auth.gateway;

import java.util.Collections;
import java.util.List;

public final class RecoveryResponse {
    private String challengeId;
    private List<OtpMethod> methods;

    public RecoveryResponse() {
    }

    public String getChallengeId() {
        return challengeId;
    }

    public List<OtpMethod> getMethods() {
        return methods == null ? Collections.emptyList() : methods;
    }
}
