package org.apollo.mobile.home.gateway;

import android.content.Context;

import org.apollo.mobile.api.ApiClient;
import org.apollo.mobile.home.api.HomeApiService;

public final class HomeGatewayFactory {

    private HomeGatewayFactory() {
    }

    public static HomeGateway create(Context context) {
        HomeApiService service = ApiClient.createService(
                context,
                HomeApiService.class
        );

        return new RetrofitHomeGateway(service);
    }
}