package org.apollo.mobile.view.settings;

import android.content.Intent;
import org.apollo.mobile.view.common.Motion;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.settings.error.SettingsError;
import org.apollo.mobile.settings.format.ElapsedTime;
import org.apollo.mobile.settings.format.PhoneInput;
import org.apollo.mobile.settings.gateway.SecurityInfo;
import org.apollo.mobile.settings.gateway.SessionInfo;
import org.apollo.mobile.settings.gateway.SettingsGateway;
import org.apollo.mobile.settings.gateway.SettingsGatewayFactory;

import java.time.LocalDateTime;
import java.util.List;

public final class SecurityActivity extends AppCompatActivity {
    private SettingsGateway gateway;
    private TextView phoneText;
    private TextView passwordText;
    private TextView deviceText;
    private View changePhoneButton;
    private String currentPhone;

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
                view -> showPasswordSheet());

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
        currentPhone = phone;
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
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                org.apollo.mobile.view.common.AppSheets.create(this, R.layout.sheet_phone_change);
        TextView current = dialog.findViewById(R.id.tvCurrentPhone);
        current.setText(currentPhone == null || currentPhone.trim().isEmpty()
                ? getString(R.string.settings_phone_none) : currentPhone.trim());
        EditText input = dialog.findViewById(R.id.etNewPhone);
        dialog.findViewById(R.id.btnSheetClose).setOnClickListener(v -> dialog.dismiss());
        View send = dialog.findViewById(R.id.btnSendPhone);
        Motion.pressable(send);
        send.setOnClickListener(view -> {
            String raw = input.getText().toString();
            if (!PhoneInput.isValid(raw)) {
                input.setError(getString(R.string.settings_phone_invalid));
                Motion.shake(input);
                return;
            }
            dialog.dismiss();
            sendPhoneChange(PhoneInput.digits(raw));
        });
        dialog.show();
    }

    private void showPasswordSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                org.apollo.mobile.view.common.AppSheets.create(this, R.layout.sheet_change_password);
        EditText current = dialog.findViewById(R.id.etCurrentPassword);
        EditText password = dialog.findViewById(R.id.etNewPassword);
        EditText confirm = dialog.findViewById(R.id.etConfirmPassword);
        org.apollo.mobile.view.auth.PasswordVisibilityToggle.attach(password);
        org.apollo.mobile.view.auth.PasswordVisibilityToggle.attach(confirm);
        dialog.findViewById(R.id.btnSheetClose).setOnClickListener(v -> dialog.dismiss());
        TextView submit = dialog.findViewById(R.id.btnChangePassword);
        TextView rules = dialog.findViewById(R.id.tvPasswordRules);
        org.apollo.mobile.view.auth.AuthForm.bind(submit, () -> {
            String value = password.getText().toString();
            org.apollo.mobile.view.auth.AuthForm.requirements(rules, value);
            return !current.getText().toString().isEmpty()
                    && org.apollo.mobile.auth.policy.PasswordPolicy.isValid(value)
                    && value.equals(confirm.getText().toString());
        }, current, password, confirm);
        Motion.pressable(submit);
        submit.setOnClickListener(view -> {
            String currentValue = current.getText().toString();
            String newValue = password.getText().toString();
            if (currentValue.isEmpty()) {
                current.setError("Informe sua senha atual.");
                Motion.shake(current);
                return;
            }
            if (!org.apollo.mobile.auth.policy.PasswordPolicy.isValid(newValue)) {
                password.setError(getString(R.string.recovery_password_invalid));
                Motion.shake(password);
                return;
            }
            if (!newValue.equals(confirm.getText().toString())) {
                confirm.setError(getString(R.string.recovery_password_mismatch));
                Motion.shake(confirm);
                return;
            }
            submit.setEnabled(false);
            gateway.changePassword(currentValue, newValue, new SettingsGateway.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if (isFinishing() || isDestroyed()) return;
                    dialog.dismiss();
                    org.apollo.mobile.view.common.AppToast.success(SecurityActivity.this, "Senha alterada com sucesso!");
                    loadSecurity();
                }

                @Override
                public void onFailure(SettingsError error) {
                    if (isFinishing() || isDestroyed()) return;
                    submit.setEnabled(true);
                    if (error.getType() == SettingsError.Type.INVALID_REQUEST) {
                        current.setError("Senha atual incorreta ou nova senha igual à atual.");
                        org.apollo.mobile.view.common.AppToast.error(SecurityActivity.this,
                                "Não foi possível alterar a senha.\nConfira os dados e tente novamente.");
                        return;
                    }
                    dialog.dismiss();
                    SettingsUi.handleFailure(SecurityActivity.this, error);
                }
            });
        });
        dialog.show();
    }

    private void sendPhoneChange(String digits) {
        gateway.requestPhoneChange(digits, new SettingsGateway.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                org.apollo.mobile.view.common.AppToast.success(SecurityActivity.this, R.string.settings_phone_requested);
                loadSecurity();
            }

            @Override
            public void onFailure(SettingsError error) {
                SettingsUi.handleFailure(SecurityActivity.this, error);
            }
        });
    }
}
