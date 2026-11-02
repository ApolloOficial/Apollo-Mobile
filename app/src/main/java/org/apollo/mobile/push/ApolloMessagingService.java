package org.apollo.mobile.push;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public final class ApolloMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        PushTokenRegistrar.register(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        RemoteMessage.Notification notification = message.getNotification();
        Map<String, String> data = message.getData();
        String title = PushText.pick(notification == null ? null : notification.getTitle(), data.get("title"));
        String body = PushText.pick(notification == null ? null : notification.getBody(), data.get("body"));
        PushNotifier.show(getApplicationContext(), title, body);
    }
}
