package org.apollo.mobile.home.gateway;

import android.util.Log;

import org.apollo.mobile.home.api.HomeApiService;
import org.apollo.mobile.home.error.HomeError;

import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Response;

public final class RetrofitHomeGateway implements HomeGateway {
    private static final String TAG = "ApolloHome";

    private final HomeApiService homeApiService;

    public RetrofitHomeGateway(HomeApiService homeApiService) {
        this.homeApiService = homeApiService;
    }

    @Override
    public void getHome(Callback callback) {
        try {
            homeApiService.getHome()
                    .enqueue(new retrofit2.Callback<HomeResponse>() {
                        @Override
                        public void onResponse(
                                Call<HomeResponse> call,
                                Response<HomeResponse> response
                        ) {
                            if (response.isSuccessful()) {
                                HomeResponse body = response.body();

                                if (body == null) {
                                    callback.onFailure(
                                            new HomeError(
                                                    HomeError.Type.INVALID_RESPONSE
                                            )
                                    );
                                    return;
                                }

                                callback.onSuccess(body);
                                return;
                            }

                            callback.onFailure(
                                    mapHttpError(response.code())
                            );
                        }

                        @Override
                        public void onFailure(
                                Call<HomeResponse> call,
                                Throwable throwable
                        ) {
                            Log.w(
                                    TAG,
                                    "Falha ao carregar a Home",
                                    throwable
                            );

                            HomeError.Type type =
                                    throwable instanceof SocketTimeoutException
                                            ? HomeError.Type.TIMEOUT
                                            : HomeError.Type.NETWORK;

                            callback.onFailure(new HomeError(type));
                        }
                    });
        } catch (RuntimeException exception) {
            Log.w(TAG, "Falha ao iniciar chamada da Home", exception);

            callback.onFailure(
                    new HomeError(HomeError.Type.NETWORK)
            );
        }
    }

    private HomeError mapHttpError(int statusCode) {
        if (statusCode == 401 || statusCode == 403) {
            return new HomeError(
                    HomeError.Type.UNAUTHORIZED
            );
        }

        if (statusCode >= 500) {
            return new HomeError(
                    HomeError.Type.SERVER
            );
        }

        return new HomeError(
                HomeError.Type.INVALID_RESPONSE
        );
    }
}