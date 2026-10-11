package org.apollo.mobile.view.settings;

import android.os.Bundle;
import android.os.StatFs;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.settings.format.ByteSize;
import org.apollo.mobile.sync.PendingQueue;
import org.apollo.mobile.sync.PendingSyncFactory;

import java.io.File;

public final class StorageActivity extends AppCompatActivity {
    private TextView availableText;
    private TextView cacheSizeText;
    private TextView pendingTitleText;
    private TextView pendingSizeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        availableText = findViewById(R.id.tvStorageAvailable);
        cacheSizeText = findViewById(R.id.tvPlacaSize);
        pendingTitleText = findViewById(R.id.tvPendingTitle);
        pendingSizeText = findViewById(R.id.tvPendingSize);

        findViewById(R.id.tvBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnClearCache).setOnClickListener(view -> {
            clear(getCacheDir());
            clear(getExternalCacheDir());
            Toast.makeText(this, R.string.settings_storage_cleared, Toast.LENGTH_SHORT).show();
            refresh();
        });

        refresh();
    }

    private void refresh() {
        StatFs stat = new StatFs(getFilesDir().getAbsolutePath());
        availableText.setText(getString(R.string.settings_storage_total,
                ByteSize.format(stat.getAvailableBytes()),
                ByteSize.format(stat.getTotalBytes())));
        cacheSizeText.setText(ByteSize.format(size(getCacheDir()) + size(getExternalCacheDir())));
        PendingQueue queue = PendingSyncFactory.queue(getApplicationContext());
        pendingTitleText.setText(getString(R.string.settings_storage_pending_title,
                queue.pendingCount() + queue.failedCount()));
        pendingSizeText.setText(ByteSize.format(queue.totalBytes()));
    }

    private static long size(File file) {
        if (file == null || !file.exists()) {
            return 0L;
        }
        if (file.isFile()) {
            return file.length();
        }
        long total = 0L;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                total += size(child);
            }
        }
        return total;
    }

    private static void clear(File directory) {
        if (directory == null) {
            return;
        }
        File[] children = directory.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            delete(child);
        }
    }

    private static void delete(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                delete(child);
            }
        }
        file.delete();
    }
}
