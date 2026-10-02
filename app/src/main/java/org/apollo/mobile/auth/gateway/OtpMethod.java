package org.apollo.mobile.auth.gateway;

import java.io.Serializable;

/** Forma de receber o código: method = "EMAIL" ou "SMS"; destination já vem mascarado pela API. */
public final class OtpMethod implements Serializable {

    public static final String EMAIL = "EMAIL";
    public static final String SMS = "SMS";

    private String method;
    private String destination;

    public OtpMethod() {
    }

    public OtpMethod(String method, String destination) {
        this.method = method;
        this.destination = destination;
    }

    public String getMethod() {
        return method;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isEmail() {
        return EMAIL.equalsIgnoreCase(method);
    }

    public boolean isSms() {
        return SMS.equalsIgnoreCase(method);
    }
}
