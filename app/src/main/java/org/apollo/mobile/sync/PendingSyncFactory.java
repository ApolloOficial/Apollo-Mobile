package org.apollo.mobile.sync;

import android.content.Context;

import org.apollo.mobile.api.ApiClient;

public final class PendingSyncFactory {
    private PendingSyncFactory() {
    }

    public static PendingQueue queue(Context context) {
        return new PendingQueue(new PrefsPendingStore(context));
    }

    public static PendingSyncRunner runner(Context context) {
        Context app = context.getApplicationContext();
        PendingQueue queue = queue(app);
        SyncApiService service = ApiClient.createService(app, SyncApiService.class);
        return new PendingSyncRunner(new PendingSync(queue, new RetrofitPendingSender(service)));
    }
}
