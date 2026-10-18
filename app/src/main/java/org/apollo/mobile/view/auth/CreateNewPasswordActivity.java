package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.auth.error.AuthError;
import org.apollo.mobile.auth.gateway.AuthenticationGateway;
import org.apollo.mobile.auth.policy.PasswordPolicy;
import org.apollo.mobile.session.SessionManagerFactory;

public final class CreateNewPasswordActivity extends AppCompatActivity {
    private View resetButton;
    private String resetToken;
    private AuthenticationGateway gateway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_password);
        org.apollo.mobile.view.common.SystemBars.apply(this);

        resetToken = getIntent().getStringExtra(AuthExtras.RESET_TOKEN);
        gateway = SessionManagerFactory.createGateway(getApplicationContext());
        resetButton = findViewById(R.id.btnResetPassword);

        PasswordVisibilityToggle.attach(findViewById(R.id.etNewPassword));
        PasswordVisibilityToggle.attach(findViewById(R.id.etConfirmPassword));

        EditText newField = findViewById(R.id.etNewPassword);
        EditText confirmField = findViewById(R.id.etConfirmPassword);
        TextView requirements = findViewById(R.id.tvRequirements);
        AuthForm.bind(resetButton, () -> {
            String value = newField.getText().toString();
            AuthForm.requirements(requirements, value);
            return org.apollo.mobile.auth.policy.PasswordPolicy.isValid(value)
                    && value.equals(confirmField.getText().toString());
        }, newField, confirmField);
        resetButton.setOnClickListener(view -> resetPassword());
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void resetPassword() {
        EditText newPasswordInput = findViewById(R.id.etNewPassword);
        EditText confirmPasswordInput = findViewById(R.id.etConfirmPassword);

        String password = newPasswordInput.getText().toString();
        String confirmation = confirmPasswordInput.getText().toString();

        if (!password.equals(confirmation)) {
            org.apollo.mobile.view.common.AppToast.info(this, R.string.recovery_password_mismatch);
            return;
        }
        if (!PasswordPolicy.isValid(password)) {
            org.apollo.mobile.view.common.AppToast.error(this, R.string.recovery_password_invalid);
            return;
        }
        if (resetToken == null || resetToken.isEmpty()) {
            org.apollo.mobile.view.common.AppToast.error(this, R.string.recovery_reset_rejected);
            return;
        }

        resetButton.setEnabled(false);
        gateway.resetPassword(resetToken, password, new AuthenticationGateway.Callback<Void>() {
            @Override
            public void onSuccess(Void response) {
                org.apollo.mobile.view.common.AppToast.success(CreateNewPasswordActivity.this, R.string.recovery_password_success);

                Intent intent = new Intent(CreateNewPasswordActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(AuthError error) {
                resetButton.setEnabled(true);
                org.apollo.mobile.view.common.AppToast.error(CreateNewPasswordActivity.this, AuthMessages.generic(error));
            }
        });
    }
}
