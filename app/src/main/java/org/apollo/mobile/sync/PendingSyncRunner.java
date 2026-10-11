package org.apollo.mobile.sync;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PendingSyncRunner {
    public interface Listener {
        void onFinished(SyncReport report);
    }

    private final PendingSync sync;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean running = new AtomicBoolean(false);

    public PendingSyncRunner(PendingSync sync) {
        this.sync = sync;
    }

    public boolean isRunning() {
        return running.get();
    }

    public void run(Listener listener) {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        executor.execute(() -> {
            SyncReport report;
            try {
                report = sync.run();
            } finally {
                running.set(false);
            }
            mainHandler.post(() -> listener.onFinished(report));
        });
    }
}
