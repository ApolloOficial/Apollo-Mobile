package org.apollo.mobile.push;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import org.apollo.mobile.MainActivity;
import org.apollo.mobile.R;

final class PushNotifier {
    static final String CHANNEL_ID = "apollo_alerts";

    private PushNotifier() {
    }

    static void ensureChannel(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.push_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(context.getString(R.string.push_channel_description));
        manager.createNotificationChannel(channel);
    }

    @SuppressLint("MissingPermission")
    static void show(Context context, String title, String body) {
        Context app = context.getApplicationContext();
        if (!PushText.hasContent(title, body)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ensureChannel(app);

        Intent open = new Intent(app, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(
                app,
                0,
                open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        String shownTitle = title != null ? title : app.getString(R.string.app_name);
        String shownBody = body != null ? body : "";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(app, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(shownTitle)
                .setContentText(shownBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(shownBody))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pending);
        NotificationManagerCompat.from(app)
                .notify((int) (System.nanoTime() & 0x7fffffff), builder.build());
    }
}
