package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.auth.error.AuthError;
import org.apollo.mobile.auth.gateway.AuthenticationGateway;
import org.apollo.mobile.auth.gateway.OtpMethod;
import org.apollo.mobile.auth.gateway.OtpSentResponse;
import org.apollo.mobile.session.SessionManagerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Escolha de onde receber o código (e-mail ou SMS). Serve ao login com 2FA
 * e à recuperação de senha; o extra MODE diz qual dos dois é.
 */
public final class ForgotPasswordMethodActivity extends AppCompatActivity {

    private CheckBox emailCheck;
    private CheckBox smsCheck;
    private View continueButton;
    private OtpMethod emailMethod;
    private OtpMethod smsMethod;
    private String challengeId;
    private String mode;
    private AuthenticationGateway gateway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_method);

        challengeId = getIntent().getStringExtra(AuthExtras.CHALLENGE_ID);
        mode = getIntent().getStringExtra(AuthExtras.MODE);
        gateway = SessionManagerFactory.createGateway(getApplicationContext());

        emailCheck = findViewById(R.id.rbEmail);
        smsCheck = findViewById(R.id.cbSms);
        continueButton = findViewById(R.id.btnContinue);

        bindMethods(readMethods());

        // Escolha única: marcar um desmarca o outro.
        emailCheck.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                smsCheck.setChecked(false);
            }
        });
        smsCheck.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                emailCheck.setChecked(false);
            }
        });

        continueButton.setOnClickListener(view -> sendCode());
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    @SuppressWarnings("unchecked")
    private List<OtpMethod> readMethods() {
        Object extra = getIntent().getSerializableExtra(AuthExtras.METHODS);
        return extra instanceof ArrayList ? (ArrayList<OtpMethod>) extra : new ArrayList<>();
    }

    private void bindMethods(List<OtpMethod> methods) {
        for (OtpMethod method : methods) {
            if (method.isEmail() && emailMethod == null) {
                emailMethod = method;
            } else if (method.isSms() && smsMethod == null) {
                smsMethod = method;
            }
        }

        findViewById(R.id.rowEmail).setVisibility(emailMethod != null ? View.VISIBLE : View.GONE);
        findViewById(R.id.rowSms).setVisibility(smsMethod != null ? View.VISIBLE : View.GONE);
        if (emailMethod != null) {
            ((TextView) findViewById(R.id.tvEmailDestination)).setText(emailMethod.getDestination());
        }
        if (smsMethod != null) {
            ((TextView) findViewById(R.id.tvSmsDestination)).setText(smsMethod.getDestination());
        }

        // Só uma opção disponível: já vem marcada.
        if (emailMethod != null && smsMethod == null) {
            emailCheck.setChecked(true);
        } else if (smsMethod != null && emailMethod == null) {
            smsCheck.setChecked(true);
        }
    }

    private void sendCode() {
        OtpMethod chosen = emailCheck.isChecked() ? emailMethod : smsCheck.isChecked() ? smsMethod : null;
        if (chosen == null || challengeId == null) {
            Toast.makeText(this, R.string.otp_choose_method, Toast.LENGTH_SHORT).show();
            return;
        }

        continueButton.setEnabled(false);
        gateway.sendOtp(challengeId, chosen.getMethod(), new AuthenticationGateway.Callback<OtpSentResponse>() {
            @Override
            public void onSuccess(OtpSentResponse response) {
                continueButton.setEnabled(true);
                String destination = response.getDestination() != null
                        ? response.getDestination() : chosen.getDestination();
                Intent intent = new Intent(ForgotPasswordMethodActivity.this, OtpVerificationActivity.class)
                        .putExtra(AuthExtras.MODE, mode)
                        .putExtra(AuthExtras.CHALLENGE_ID, challengeId)
                        .putExtra(AuthExtras.METHOD, chosen.getMethod())
                        .putExtra(AuthExtras.DESTINATION, destination)
                        .putExtra(AuthExtras.RESEND_SECONDS, response.getResendInSeconds())
                        .putExtra(AuthExtras.METHODS, new ArrayList<OtpMethod>(readMethods()));
                startActivity(intent);
            }

            @Override
            public void onFailure(AuthError error) {
                continueButton.setEnabled(true);
                Toast.makeText(ForgotPasswordMethodActivity.this, AuthMessages.generic(error),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
