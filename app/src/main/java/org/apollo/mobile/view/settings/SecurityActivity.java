package org.apollo.mobile.view.settings;

import android.content.Intent;
import org.apollo.mobile.view.common.Motion;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.settings.error.SettingsError;
import org.apollo.mobile.settings.format.ElapsedTime;
import org.apollo.mobile.settings.format.PhoneInput;
import org.apollo.mobile.settings.gateway.SecurityInfo;
import org.apollo.mobile.settings.gateway.SessionInfo;
import org.apollo.mobile.settings.gateway.SettingsGateway;
import org.apollo.mobile.settings.gateway.SettingsGatewayFactory;
import org.apollo.mobile.view.auth.ForgotPasswordStartActivity;

import java.time.LocalDateTime;
import java.util.List;

public final class SecurityActivity extends AppCompatActivity {
    private SettingsGateway gateway;
    private TextView phoneText;
    private TextView passwordText;
    private TextView deviceText;
    private View changePhoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);
        Motion.enterScreen(this);

        gateway = SettingsGatewayFactory.create(getApplicationContext());
        phoneText = findViewById(R.id.tv_phone_value);
        passwordText = findViewById(R.id.tv_password_value);
        deviceText = findViewById(R.id.tv_device_value);
        changePhoneButton = findViewById(R.id.btn_change_phone);
        if (changePhoneButton instanceof android.widget.TextView) {
            android.widget.TextView link = (android.widget.TextView) changePhoneButton;
            link.setPaintFlags(link.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        }

        findViewById(R.id.tvBack).setOnClickListener(view -> finish());
        changePhoneButton.setOnClickListener(view -> showPhoneDialog());
        ((View) findViewById(R.id.tv_password_title).getParent()).setOnClickListener(
                view -> startActivity(new Intent(this, ForgotPasswordStartActivity.class)));

        loadSecurity();
        loadSessions();
    }

    private void loadSecurity() {
        gateway.security(new SettingsGateway.Callback<SecurityInfo>() {
            @Override
            public void onSuccess(SecurityInfo info) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                bindPhone(info);
                bindPassword(info);
            }

            @Override
            public void onFailure(SettingsError error) {
                SettingsUi.handleFailure(SecurityActivity.this, error);
            }
        });
    }

    private void bindPhone(SecurityInfo info) {
        String phone = info.getMaskedPhone();
        String base = phone == null || phone.trim().isEmpty()
                ? getString(R.string.settings_phone_none)
                : phone.trim();
        if (info.isPhoneChangePending()) {
            phoneText.setText(base + "\n" + getString(R.string.settings_phone_pending));
            changePhoneButton.setEnabled(false);
            changePhoneButton.setAlpha(0.4f);
        } else {
            phoneText.setText(base);
            changePhoneButton.setEnabled(true);
            changePhoneButton.setAlpha(1f);
        }
    }

    private void bindPassword(SecurityInfo info) {
        ElapsedTime elapsed = ElapsedTime.between(info.getPasswordChangedAt(), LocalDateTime.now());
        String text = SettingsUi.elapsedText(this, elapsed,
                R.plurals.settings_password_changed_minutes,
                R.plurals.settings_password_changed_hours,
                R.plurals.settings_password_changed_days,
                R.plurals.settings_password_changed_months,
                R.plurals.settings_password_changed_years,
                R.string.settings_password_changed_now);
        passwordText.setText(text == null ? getString(R.string.settings_password_unknown) : text);
    }

    private void loadSessions() {
        gateway.sessions(new SettingsGateway.Callback<List<SessionInfo>>() {
            @Override
            public void onSuccess(List<SessionInfo> sessions) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                bindDevice(sessions);
            }

            @Override
            public void onFailure(SettingsError error) {
                SettingsUi.handleFailure(SecurityActivity.this, error);
            }
        });
    }

    private void bindDevice(List<SessionInfo> sessions) {
        SessionInfo chosen = null;
        if (sessions != null) {
            for (SessionInfo session : sessions) {
                if (session.isCurrent()) {
                    chosen = session;
                    break;
                }
            }
            if (chosen == null && !sessions.isEmpty()) {
                chosen = sessions.get(0);
            }
        }
        if (chosen == null) {
            deviceText.setText(R.string.settings_device_unknown);
            return;
        }
        String name = chosen.getDeviceName() == null || chosen.getDeviceName().trim().isEmpty()
                ? getString(R.string.settings_value_empty)
                : chosen.getDeviceName().trim();
        String seen = chosen.isCurrent()
                ? getString(R.string.settings_device_active_now)
                : SettingsUi.elapsedText(this,
                        ElapsedTime.between(chosen.getLastSeenAt(), LocalDateTime.now()),
                        R.plurals.settings_seen_minutes,
                        R.plurals.settings_seen_hours,
                        R.plurals.settings_seen_days,
                        R.plurals.settings_seen_months,
                        R.plurals.settings_seen_years,
                        R.string.settings_device_active_now);
        deviceText.setText(seen == null
                ? name
                : getString(R.string.settings_device_value, name, seen));
    }

    private void showPhoneDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setHint(R.string.settings_phone_dialog_hint);
        FrameLayout container = new FrameLayout(this);
        int margin = Math.round(24 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, margin / 2, margin, 0);
        container.addView(input, params);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.settings_phone_dialog_title)
                .setView(container)
                .setNegativeButton(R.string.settings_cancel, null)
                .setPositiveButton(R.string.settings_phone_send, null)
                .create();
        dialog.setOnShowListener(shown -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String raw = input.getText().toString();
            if (!PhoneInput.isValid(raw)) {
                Toast.makeText(this, R.string.settings_phone_invalid, Toast.LENGTH_LONG).show();
                return;
            }
            dialog.dismiss();
            sendPhoneChange(PhoneInput.digits(raw));
        }));
        dialog.show();
    }

    private void sendPhoneChange(String digits) {
        gateway.requestPhoneChange(digits, new SettingsGateway.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(SecurityActivity.this, R.string.settings_phone_requested, Toast.LENGTH_LONG).show();
                loadSecurity();
            }

            @Override
            public void onFailure(SettingsError error) {
                SettingsUi.handleFailure(SecurityActivity.this, error);
            }
        });
    }
}
