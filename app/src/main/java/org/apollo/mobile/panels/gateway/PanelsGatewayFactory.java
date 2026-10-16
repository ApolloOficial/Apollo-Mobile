package org.apollo.mobile.panels.gateway;

import android.content.Context;

import org.apollo.mobile.api.ApiClient;
import org.apollo.mobile.panels.api.PanelsApiService;

public final class PanelsGatewayFactory {

    private PanelsGatewayFactory() {
    }

    public static PanelsGateway create(Context context) {
        return new RetrofitPanelsGateway(ApiClient.createService(context, PanelsApiService.class));
    }
}
