package org.apollo.mobile.settings.gateway;

public final class PhoneChangeBody {
    private final String newPhone;

    public PhoneChangeBody(String newPhone) {
        this.newPhone = newPhone;
    }

    public String getNewPhone() {
        return newPhone;
    }
}
