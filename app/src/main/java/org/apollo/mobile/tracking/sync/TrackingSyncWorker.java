package org.apollo.mobile.tracking.sync;

import org.apollo.mobile.tracking.gateway.TrackingRepository;
import org.apollo.mobile.tracking.storage.TrackingStore;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.concurrent.TimeUnit;

public final class TrackingSyncWorker extends Worker {
    public TrackingSyncWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    private static Constraints constraints() {
        return new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    }

    public static void schedule(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(TrackingSyncWorker.class)
                .setConstraints(constraints())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork("apollo_tracking_now",
                ExistingWorkPolicy.KEEP, request);
    }

    public static void periodic(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(TrackingSyncWorker.class,
                15, TimeUnit.MINUTES).setConstraints(constraints())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("apollo_tracking_periodic",
                ExistingPeriodicWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        try (TrackingStore store = new TrackingStore(getApplicationContext())) {
            TrackingRepository repository = new TrackingRepository(getApplicationContext(), store);
            for (int batch = 0; batch < 5 && !isStopped(); batch++) {
                TrackingRepository.Outcome outcome = repository.syncOneBatch();
                if (outcome == TrackingRepository.Outcome.DONE
                        || outcome == TrackingRepository.Outcome.AUTH_REQUIRED) return Result.success();
                if (outcome == TrackingRepository.Outcome.RETRY) return Result.retry();
            }
            return Result.retry();
        } catch (Exception e) {
            android.util.Log.w("ApolloTracking", "Sync retry: " + e.getClass().getSimpleName());
            return Result.retry();
        }
    }
}
