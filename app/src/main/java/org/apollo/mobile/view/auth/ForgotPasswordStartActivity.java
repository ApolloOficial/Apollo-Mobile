package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.auth.error.AuthError;
import org.apollo.mobile.auth.gateway.AuthenticationGateway;
import org.apollo.mobile.auth.gateway.OtpMethod;
import org.apollo.mobile.auth.gateway.RecoveryResponse;
import org.apollo.mobile.session.SessionManagerFactory;

import java.util.ArrayList;

public final class ForgotPasswordStartActivity extends AppCompatActivity {
    public static final String EXTRA_IDENTIFIER = "identifier";

    private EditText identifierInput;
    private TextView inputError;
    private View continueButton;
    private AuthenticationGateway gateway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_start);

        identifierInput = findViewById(R.id.etIdentifier);
        inputError = findViewById(R.id.tvInputError);
        continueButton = findViewById(R.id.btnContinue);
        gateway = SessionManagerFactory.createGateway(getApplicationContext());

        continueButton.setOnClickListener(view -> continueRecovery());
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void continueRecovery() {
        String email = identifierInput.getText().toString().trim();

        if (email.isEmpty()) {
            showError(R.string.recovery_identifier_required);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(R.string.recovery_email_invalid);
            return;
        }

        inputError.setVisibility(View.GONE);
        continueButton.setEnabled(false);

        gateway.startRecovery(email, new AuthenticationGateway.Callback<RecoveryResponse>() {
            @Override
            public void onSuccess(RecoveryResponse response) {
                continueButton.setEnabled(true);
                if (response.getChallengeId() == null || response.getMethods().isEmpty()) {
                    showError(R.string.recovery_no_methods);
                    return;
                }
                Intent intent = new Intent(ForgotPasswordStartActivity.this, ForgotPasswordMethodActivity.class)
                        .putExtra(AuthExtras.MODE, AuthExtras.MODE_RECOVERY)
                        .putExtra(EXTRA_IDENTIFIER, email)
                        .putExtra(AuthExtras.CHALLENGE_ID, response.getChallengeId())
                        .putExtra(AuthExtras.METHODS, new ArrayList<OtpMethod>(response.getMethods()));
                startActivity(intent);
            }

            @Override
            public void onFailure(AuthError error) {
                continueButton.setEnabled(true);
                showError(AuthMessages.generic(error));
            }
        });
    }

    private void showError(int messageRes) {
        inputError.setText(messageRes);
        inputError.setVisibility(View.VISIBLE);
    }
}
