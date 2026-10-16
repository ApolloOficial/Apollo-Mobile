package org.apollo.mobile.maintenance.gateway;

import android.content.Context;

import org.apollo.mobile.api.ApiClient;
import org.apollo.mobile.maintenance.api.ServiceOrderApiService;
import org.apollo.mobile.settings.api.SettingsApiService;

public final class ServiceOrderGatewayFactory {
    private ServiceOrderGatewayFactory() {
    }

    public static ServiceOrderGateway create(Context context) {
        return new RetrofitServiceOrderGateway(
                ApiClient.createService(context, ServiceOrderApiService.class),
                ApiClient.createService(context, SettingsApiService.class)
        );
    }
}
