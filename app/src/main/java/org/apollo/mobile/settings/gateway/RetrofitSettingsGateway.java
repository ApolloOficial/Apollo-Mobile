package org.apollo.mobile.settings.gateway;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.apollo.mobile.settings.api.SettingsApiService;
import org.apollo.mobile.settings.error.SettingsError;
import org.apollo.mobile.settings.error.SettingsErrorMapper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public final class RetrofitSettingsGateway implements SettingsGateway {
    private static final String TAG = "ApolloSettings";

    private final SettingsApiService service;
    private final ExecutorService photoExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public RetrofitSettingsGateway(SettingsApiService service) {
        this.service = service;
    }

    @Override
    public void summary(Callback<SettingsSummary> callback) {
        enqueue(() -> service.summary(), callback, false);
    }

    @Override
    public void profile(Callback<ProfileInfo> callback) {
        enqueue(() -> service.profile(), callback, false);
    }

    @Override
    public void photo(Callback<byte[]> callback) {
        photoExecutor.execute(() -> {
            SettingsError failure = null;
            byte[] data = null;
            try {
                Response<ResponseBody> response = service.photo().execute();
                if (response.isSuccessful() && response.body() != null) {
                    data = response.body().bytes();
                } else if (response.isSuccessful()) {
                    failure = new SettingsError(SettingsError.Type.INVALID_RESPONSE);
                } else {
                    failure = new SettingsError(SettingsErrorMapper.fromStatus(response.code()));
                }
            } catch (SocketTimeoutException exception) {
                failure = new SettingsError(SettingsError.Type.TIMEOUT);
            } catch (IOException | RuntimeException exception) {
                Log.w(TAG, "Falha ao baixar a foto", exception);
                failure = new SettingsError(SettingsError.Type.NETWORK);
            }
            final byte[] resultData = data;
            final SettingsError resultFailure = failure;
            mainHandler.post(() -> {
                if (resultFailure != null) {
                    callback.onFailure(resultFailure);
                } else {
                    callback.onSuccess(resultData);
                }
            });
        });
    }

    @Override
    public void uploadPhoto(byte[] data, String mimeType, Callback<Void> callback) {
        enqueue(() -> {
            MediaType mediaType = MediaType.parse(mimeType);
            RequestBody body = RequestBody.create(data, mediaType);
            String extension = "image/png".equals(mimeType) ? "png" : "jpg";
            return service.uploadPhoto(MultipartBody.Part.createFormData("file", "photo." + extension, body));
        }, callback, true);
    }

    @Override
    public void removePhoto(Callback<Void> callback) {
        enqueue(() -> service.removePhoto(), callback, true);
    }

    @Override
    public void notifications(Callback<NotificationPreferences> callback) {
        enqueue(() -> service.notifications(), callback, false);
    }

    @Override
    public void updateNotifications(NotificationPreferences change, Callback<NotificationPreferences> callback) {
        enqueue(() -> service.updateNotifications(change), callback, false);
    }

    @Override
    public void language(Callback<LanguageSettings> callback) {
        enqueue(() -> service.language(), callback, false);
    }

    @Override
    public void updateLanguage(String code, Callback<LanguageSettings> callback) {
        enqueue(() -> service.updateLanguage(new LanguageUpdate(code)), callback, false);
    }

    @Override
    public void security(Callback<SecurityInfo> callback) {
        enqueue(() -> service.security(), callback, false);
    }

    @Override
    public void requestPhoneChange(String phoneDigits, Callback<Void> callback) {
        enqueue(() -> service.requestPhoneChange(new PhoneChangeBody(phoneDigits)), callback, true);
    }

    @Override
    public void sessions(Callback<List<SessionInfo>> callback) {
        enqueue(() -> service.sessions(), callback, false);
    }

    private interface CallFactory<T> {
        Call<T> create();
    }

    private <T> void enqueue(CallFactory<T> factory, Callback<T> callback, boolean emptyBodyAllowed) {
        try {
            factory.create().enqueue(new retrofit2.Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null || emptyBodyAllowed) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onFailure(new SettingsError(SettingsError.Type.INVALID_RESPONSE));
                        }
                        return;
                    }
                    callback.onFailure(new SettingsError(SettingsErrorMapper.fromStatus(response.code())));
                }

                @Override
                public void onFailure(Call<T> call, Throwable throwable) {
                    Log.w(TAG, "Falha na chamada " + call.request().url().encodedPath(), throwable);
                    SettingsError.Type type = throwable instanceof SocketTimeoutException
                            ? SettingsError.Type.TIMEOUT
                            : SettingsError.Type.NETWORK;
                    callback.onFailure(new SettingsError(type));
                }
            });
        } catch (RuntimeException exception) {
            callback.onFailure(new SettingsError(SettingsError.Type.NETWORK));
        }
    }
}
