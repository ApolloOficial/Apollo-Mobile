package org.apollo.mobile.view.settings;

import android.content.Intent;
import org.apollo.mobile.view.common.Motion;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.BuildConfig;
import org.apollo.mobile.R;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.sync.PendingQueue;
import org.apollo.mobile.sync.PendingSyncFactory;
import org.apollo.mobile.sync.PendingSyncRunner;
import org.apollo.mobile.sync.SyncReport;
import org.apollo.mobile.settings.error.SettingsError;
import org.apollo.mobile.settings.gateway.SettingsGateway;
import org.apollo.mobile.settings.gateway.SettingsGatewayFactory;
import org.apollo.mobile.settings.gateway.SettingsSummary;
import org.apollo.mobile.view.common.BottomNavigation;

public final class SettingsActivity extends AppCompatActivity {
    private SettingsGateway gateway;
    private ImageView photoView;
    private TextView nameText;
    private TextView emailText;
    private TextView roleText;
    private TextView syncStatusText;
    private TextView syncButton;
    private View syncDot;
    private PendingQueue pendingQueue;
    private PendingSyncRunner syncRunner;
    private boolean hasPhoto;

    private final ActivityResultLauncher<String> photoPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onPhotoPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Motion.enterScreen(this);

        gateway = SettingsGatewayFactory.create(getApplicationContext());
        photoView = findViewById(R.id.img_profile);
        nameText = findViewById(R.id.txt_user_name);
        emailText = findViewById(R.id.txt_user_email);
        roleText = findViewById(R.id.txt_user_role);
        syncStatusText = findViewById(R.id.tv_sync_status);
        syncButton = findViewById(R.id.btn_sync);
        syncDot = findViewById(R.id.view_sync_dot);
        pendingQueue = PendingSyncFactory.queue(getApplicationContext());
        syncRunner = PendingSyncFactory.runner(getApplicationContext());

        bindRow(R.id.t1, ProfileActivity.class);
        bindRow(R.id.t2, SecurityActivity.class);
        bindRow(R.id.t3, NotificationsSettingsActivity.class);
        bindRow(R.id.t4, LanguageActivity.class);
        bindRow(R.id.t6, StorageActivity.class);
        rowOf(R.id.t8).setOnClickListener(view -> showAbout());
        rowOf(R.id.t9).setOnClickListener(view -> confirmLogout());

        syncButton.setOnClickListener(view -> syncNow());
        findViewById(R.id.btn_edit_photo).setOnClickListener(view -> showPhotoMenu());
        photoView.setOnClickListener(view -> showPhotoMenu());

        // Footer + chatbot (aba Configurações ativa)
        BottomNavigation.setup(this, BottomNavigation.Tab.SETTINGS);
    }

    @Override
    public void finish() {
        super.finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.nav_fade_in, R.anim.nav_fade_out);
        } else {
            overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
        refreshSync();
    }

    private void refreshSync() {
        int pending = pendingQueue.pendingCount();
        int failed = pendingQueue.failedCount();
        String text = pending > 0
                ? getResources().getQuantityString(R.plurals.settings_sync_pending, pending, pending)
                : getString(R.string.settings_sync_none);
        if (failed > 0) {
            text = getString(R.string.settings_sync_status_join, text,
                    getString(R.string.settings_sync_failed_suffix, failed));
        }
        syncStatusText.setText(text);
        boolean attention = pending > 0 || failed > 0;
        syncDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getColor(attention ? R.color.alert_one : R.color.sucess_one)));
    }

    private void syncNow() {
        if (syncRunner.isRunning()) {
            return;
        }
        if (pendingQueue.pendingCount() == 0) {
            Toast.makeText(this, R.string.settings_sync_nothing, Toast.LENGTH_SHORT).show();
            load();
            return;
        }
        syncButton.setEnabled(false);
        syncStatusText.setText(R.string.settings_sync_running);
        syncRunner.run(report -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            syncButton.setEnabled(true);
            refreshSync();
            onSyncFinished(report);
        });
    }

    private void onSyncFinished(SyncReport report) {
        if (report.getStop() == SyncReport.Stop.UNAUTHORIZED) {
            SettingsUi.goToLogin(this);
            return;
        }
        if (report.getStop() == SyncReport.Stop.NETWORK) {
            Toast.makeText(this, R.string.settings_sync_offline, Toast.LENGTH_LONG).show();
            return;
        }
        if (report.getRejected() > 0) {
            Toast.makeText(this, getString(R.string.settings_sync_with_errors, report.getRejected()),
                    Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, R.string.settings_sync_done, Toast.LENGTH_SHORT).show();
    }

    private View rowOf(int titleId) {
        return (View) findViewById(titleId).getParent();
    }

    private void bindRow(int titleId, Class<?> target) {
        rowOf(titleId).setOnClickListener(view -> startActivity(new Intent(this, target)));
    }

    private void load() {
        gateway.summary(new SettingsGateway.Callback<SettingsSummary>() {
            @Override
            public void onSuccess(SettingsSummary summary) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                nameText.setText(blankTo(summary.getFullName(), getString(R.string.settings_default_name)));
                emailText.setText(blankTo(summary.getEmail(), ""));
                roleText.setText(blankTo(summary.getRoleLabel(), ""));
                hasPhoto = summary.hasPhoto();
                loadPhoto();
            }

            @Override
            public void onFailure(SettingsError error) {
                SettingsUi.handleFailure(SettingsActivity.this, error);
            }
        });
    }

    private void loadPhoto() {
        if (!hasPhoto) {
            photoView.setImageResource(R.drawable.profile_picture);
            return;
        }
        gateway.photo(new SettingsGateway.Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Bitmap bitmap = SettingsPhotos.decode(data);
                if (bitmap != null) {
                    photoView.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onFailure(SettingsError error) {
                if (error.getType() == SettingsError.Type.SESSION_EXPIRED) {
                    SettingsUi.handleFailure(SettingsActivity.this, error);
                }
            }
        });
    }

    private void showPhotoMenu() {
        SettingsPhotos.showMenu(this, hasPhoto, () -> photoPicker.launch("image/*"), this::removePhoto);
    }

    private void onPhotoPicked(Uri uri) {
        if (uri == null) {
            return;
        }
        SettingsPhotos.prepareAsync(getContentResolver(), uri, data -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (data == null) {
                Toast.makeText(this, R.string.settings_photo_invalid, Toast.LENGTH_LONG).show();
                return;
            }
            gateway.uploadPhoto(data, SettingsPhotos.MIME_JPEG, new SettingsGateway.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(SettingsActivity.this, R.string.settings_photo_updated, Toast.LENGTH_SHORT).show();
                    hasPhoto = true;
                    loadPhoto();
                }

                @Override
                public void onFailure(SettingsError error) {
                    SettingsUi.handleFailure(SettingsActivity.this, error);
                }
            });
        });
    }

    private void removePhoto() {
        gateway.removePhoto(new SettingsGateway.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(SettingsActivity.this, R.string.settings_photo_removed, Toast.LENGTH_SHORT).show();
                hasPhoto = false;
                loadPhoto();
            }

            @Override
            public void onFailure(SettingsError error) {
                SettingsUi.handleFailure(SettingsActivity.this, error);
            }
        });
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_about_title)
                .setMessage(getString(R.string.settings_about_message, BuildConfig.VERSION_NAME))
                .setPositiveButton(R.string.settings_ok, null)
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_logout_title)
                .setMessage(R.string.settings_logout_message)
                .setNegativeButton(R.string.settings_cancel, null)
                .setPositiveButton(R.string.settings_logout_confirm, (dialog, which) -> {
                    SessionManagerFactory.create(getApplicationContext()).logout();
                    SettingsUi.goToLogin(this);
                })
                .show();
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
