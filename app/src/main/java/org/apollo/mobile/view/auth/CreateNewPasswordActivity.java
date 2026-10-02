package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

        resetToken = getIntent().getStringExtra(AuthExtras.RESET_TOKEN);
        gateway = SessionManagerFactory.createGateway(getApplicationContext());
        resetButton = findViewById(R.id.btnResetPassword);

        resetButton.setOnClickListener(view -> resetPassword());
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void resetPassword() {
        EditText newPasswordInput = findViewById(R.id.etNewPassword);
        EditText confirmPasswordInput = findViewById(R.id.etConfirmPassword);

        String password = newPasswordInput.getText().toString();
        String confirmation = confirmPasswordInput.getText().toString();

        if (!password.equals(confirmation)) {
            Toast.makeText(this, R.string.recovery_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!PasswordPolicy.isValid(password)) {
            Toast.makeText(this, R.string.recovery_password_invalid, Toast.LENGTH_LONG).show();
            return;
        }
        if (resetToken == null || resetToken.isEmpty()) {
            Toast.makeText(this, R.string.recovery_reset_rejected, Toast.LENGTH_LONG).show();
            return;
        }

        resetButton.setEnabled(false);
        gateway.resetPassword(resetToken, password, new AuthenticationGateway.Callback<Void>() {
            @Override
            public void onSuccess(Void response) {
                Toast.makeText(CreateNewPasswordActivity.this, R.string.recovery_password_success,
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(CreateNewPasswordActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(AuthError error) {
                resetButton.setEnabled(true);
                Toast.makeText(CreateNewPasswordActivity.this, AuthMessages.generic(error),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
