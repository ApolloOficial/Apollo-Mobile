package org.apollo.mobile.home.gateway;

import org.apollo.mobile.home.error.HomeError;

public interface HomeGateway {

    void getHome(Callback callback);

    interface Callback {
        void onSuccess(HomeResponse response);

        void onFailure(HomeError error);
    }
}