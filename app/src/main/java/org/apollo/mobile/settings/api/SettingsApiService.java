package org.apollo.mobile.settings.api;

import org.apollo.mobile.settings.gateway.LanguageSettings;
import org.apollo.mobile.settings.gateway.LanguageUpdate;
import org.apollo.mobile.settings.gateway.NotificationPreferences;
import org.apollo.mobile.settings.gateway.PhoneChangeBody;
import org.apollo.mobile.settings.gateway.ProfileInfo;
import org.apollo.mobile.settings.gateway.SecurityInfo;
import org.apollo.mobile.settings.gateway.SessionInfo;
import org.apollo.mobile.settings.gateway.SettingsSummary;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface SettingsApiService {
    @GET("api/v1/settings")
    Call<SettingsSummary> summary();

    @GET("api/v1/settings/profile")
    Call<ProfileInfo> profile();

    @GET("api/v1/settings/photo")
    Call<ResponseBody> photo();

    @Multipart
    @PUT("api/v1/settings/photo")
    Call<Void> uploadPhoto(@Part MultipartBody.Part file);

    @DELETE("api/v1/settings/photo")
    Call<Void> removePhoto();

    @GET("api/v1/settings/notifications")
    Call<NotificationPreferences> notifications();

    @PATCH("api/v1/settings/notifications")
    Call<NotificationPreferences> updateNotifications(@Body NotificationPreferences body);

    @GET("api/v1/settings/language")
    Call<LanguageSettings> language();

    @PUT("api/v1/settings/language")
    Call<LanguageSettings> updateLanguage(@Body LanguageUpdate body);

    @GET("api/v1/settings/security")
    Call<SecurityInfo> security();

    @POST("api/v1/settings/security/phone-change-requests")
    Call<Void> requestPhoneChange(@Body PhoneChangeBody body);

    @retrofit2.http.PATCH("api/v1/auth/password")
    Call<Void> changePassword(@Body org.apollo.mobile.settings.gateway.ChangePasswordBody body);

    @GET("api/v1/settings/sessions")
    Call<List<SessionInfo>> sessions();
}
