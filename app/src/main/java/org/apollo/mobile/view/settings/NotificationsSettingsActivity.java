package org.apollo.mobile.view.settings;

import android.os.Bundle;
import org.apollo.mobile.view.common.Motion;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import org.apollo.mobile.R;
import org.apollo.mobile.settings.error.SettingsError;
import org.apollo.mobile.settings.gateway.NotificationPreferences;
import org.apollo.mobile.settings.gateway.SettingsGateway;
import org.apollo.mobile.settings.gateway.SettingsGatewayFactory;

public final class NotificationsSettingsActivity extends AppCompatActivity {
    private SettingsGateway gateway;
    private SwitchCompat predictiveSwitch;
    private SwitchCompat panelSwitch;
    private SwitchCompat emailSwitch;
    private boolean binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_notifications);
        Motion.enterScreen(this);

        gateway = SettingsGatewayFactory.create(getApplicationContext());
        predictiveSwitch = findViewById(R.id.switch_1);
        panelSwitch = findViewById(R.id.switch_2);
        emailSwitch = findViewById(R.id.switch_3);

        findViewById(R.id.tvBack).setOnClickListener(view -> finish());
        setSwitchesEnabled(false);

        predictiveSwitch.setOnCheckedChangeListener((button, checked) ->
                change(button, NotificationPreferences.onlyPredictiveMaintenance(checked)));
        panelSwitch.setOnCheckedChangeListener((button, checked) ->
                change(button, NotificationPreferences.onlyPanelAlert(checked)));
        emailSwitch.setOnCheckedChangeListener((button, checked) ->
                change(button, NotificationPreferences.onlyEmailSummary(checked)));

        load();
    }

    private void load() {
        gateway.notifications(new SettingsGateway.Callback<NotificationPreferences>() {
            @Override
            public void onSuccess(NotificationPreferences preferences) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                bind(preferences);
                setSwitchesEnabled(true);
            }

            @Override
            public void onFailure(SettingsError error) {
                SettingsUi.handleFailure(NotificationsSettingsActivity.this, error);
            }
        });
    }

    private void bind(NotificationPreferences preferences) {
        binding = true;
        predictiveSwitch.setChecked(preferences.isPredictiveMaintenance());
        panelSwitch.setChecked(preferences.isPanelAlert());
        emailSwitch.setChecked(preferences.isEmailSummary());
        binding = false;
    }

    private void setSwitchesEnabled(boolean enabled) {
        predictiveSwitch.setEnabled(enabled);
        panelSwitch.setEnabled(enabled);
        emailSwitch.setEnabled(enabled);
    }

    private void change(CompoundButton button, NotificationPreferences change) {
        if (binding) {
            return;
        }
        setSwitchesEnabled(false);
        gateway.updateNotifications(change, new SettingsGateway.Callback<NotificationPreferences>() {
            @Override
            public void onSuccess(NotificationPreferences preferences) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                bind(preferences);
                setSwitchesEnabled(true);
            }

            @Override
            public void onFailure(SettingsError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                binding = true;
                button.setChecked(!button.isChecked());
                binding = false;
                setSwitchesEnabled(true);
                SettingsUi.handleFailure(NotificationsSettingsActivity.this, error);
            }
        });
    }
}
