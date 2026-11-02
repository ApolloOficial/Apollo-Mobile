package org.apollo.mobile.push;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public final class PushBootstrap {
    private static final int REQUEST_NOTIFICATIONS = 4417;

    private PushBootstrap() {
    }

    public static void start(Activity activity) {
        Context app = activity.getApplicationContext();
        try {
            PushNotifier.ensureChannel(app);
            askPermission(activity, app);
            PushTokenRegistrar.sync(app);
        } catch (RuntimeException ignored) {
        }
    }

    private static void askPermission(Activity activity, Context app) {
        if (Build.VERSION.SDK_INT < 33) {
            return;
        }
        if (ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PushTokenStore store = new PushTokenStore(app);
        if (store.permissionAsked()) {
            return;
        }
        store.markPermissionAsked();
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_NOTIFICATIONS
        );
    }
}
