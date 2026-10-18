package org.apollo.mobile.settings.gateway;

import org.apollo.mobile.settings.error.SettingsError;

import java.util.List;

public interface SettingsGateway {
    void summary(Callback<SettingsSummary> callback);

    void profile(Callback<ProfileInfo> callback);

    void photo(Callback<byte[]> callback);

    void uploadPhoto(byte[] data, String mimeType, Callback<Void> callback);

    void removePhoto(Callback<Void> callback);

    void notifications(Callback<NotificationPreferences> callback);

    void updateNotifications(NotificationPreferences change, Callback<NotificationPreferences> callback);

    void language(Callback<LanguageSettings> callback);

    void updateLanguage(String code, Callback<LanguageSettings> callback);

    void security(Callback<SecurityInfo> callback);

    void requestPhoneChange(String phoneDigits, Callback<Void> callback);

    void sessions(Callback<List<SessionInfo>> callback);

    void changePassword(String currentPassword, String newPassword, Callback<Void> callback);

    interface Callback<T> {
        void onSuccess(T result);

        void onFailure(SettingsError error);
    }
}
