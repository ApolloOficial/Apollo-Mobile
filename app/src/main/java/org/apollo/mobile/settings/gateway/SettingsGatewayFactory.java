package org.apollo.mobile.settings.gateway;

import android.content.Context;

import org.apollo.mobile.api.ApiClient;
import org.apollo.mobile.settings.api.SettingsApiService;

public final class SettingsGatewayFactory {
    private SettingsGatewayFactory() {
    }

    public static SettingsGateway create(Context context) {
        SettingsApiService service = ApiClient.createService(
                context.getApplicationContext(), SettingsApiService.class);
        return new RetrofitSettingsGateway(service);
    }
}
