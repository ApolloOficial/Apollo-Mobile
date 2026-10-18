package org.apollo.mobile;

import android.app.Activity;
import android.app.Application;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.apollo.mobile.view.common.ActivityTracker;
import org.apollo.mobile.view.common.AppToast;

public final class ApolloApplication extends Application {

    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean offline;

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityTracker.register(this);
        org.apollo.mobile.view.common.Tr.init(this);
        watchConnectivity();
    }

    private void watchConnectivity() {
        ConnectivityManager manager = getSystemService(ConnectivityManager.class);
        if (manager == null) return;
        try {
            manager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onLost(@NonNull Network network) {
                    main.postDelayed(() -> {
                        if (hasInternet(manager)) return;
                        if (!offline) {
                            offline = true;
                            Activity activity = ActivityTracker.current();
                            if (activity != null) AppToast.offline(activity);
                        }
                    }, 800);
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities caps) {
                    if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        main.post(() -> offline = false);
                    }
                }
            });
        } catch (RuntimeException ignored) {
        }
    }

    private static boolean hasInternet(ConnectivityManager manager) {
        Network active = manager.getActiveNetwork();
        if (active == null) return false;
        NetworkCapabilities caps = manager.getNetworkCapabilities(active);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
