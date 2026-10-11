package org.apollo.mobile.settings.error;

public final class SettingsErrorMapper {
    private SettingsErrorMapper() {
    }

    public static SettingsError.Type fromStatus(int statusCode) {
        switch (statusCode) {
            case 401:
                return SettingsError.Type.SESSION_EXPIRED;
            case 403:
                return SettingsError.Type.FORBIDDEN;
            case 404:
                return SettingsError.Type.NOT_FOUND;
            case 400:
            case 409:
            case 413:
            case 415:
            case 422:
                return SettingsError.Type.INVALID_REQUEST;
            case 502:
            case 503:
            case 504:
                return SettingsError.Type.UNAVAILABLE;
            default:
                return statusCode >= 500 ? SettingsError.Type.SERVER : SettingsError.Type.INVALID_RESPONSE;
        }
    }
}
