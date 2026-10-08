package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.auth.error.AuthError;
import org.apollo.mobile.auth.gateway.AuthenticationGateway;
import org.apollo.mobile.auth.gateway.OtpMethod;
import org.apollo.mobile.auth.gateway.OtpSentResponse;
import org.apollo.mobile.auth.gateway.OtpVerifyResponse;
import org.apollo.mobile.session.SessionManager;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.session.UserSession;

import java.util.ArrayList;

public final class OtpVerificationActivity extends AppCompatActivity {
    private static final int CODE_LENGTH = 6;

    private final EditText[] boxes = new EditText[CODE_LENGTH];
    private TextView resendText;
    private View continueButton;
    private SessionManager sessionManager;
    private AuthenticationGateway gateway;
    private CountDownTimer resendTimer;

    private String mode;
    private String challengeId;
    private String method;
    private int resendSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        mode = getIntent().getStringExtra(AuthExtras.MODE);
        challengeId = getIntent().getStringExtra(AuthExtras.CHALLENGE_ID);
        method = getIntent().getStringExtra(AuthExtras.METHOD);
        resendSeconds = getIntent().getIntExtra(AuthExtras.RESEND_SECONDS, 30);
        sessionManager = SessionManagerFactory.create(getApplicationContext());
        gateway = SessionManagerFactory.createGateway(getApplicationContext());

        String destination = getIntent().getStringExtra(AuthExtras.DESTINATION);
        if (destination != null) {
            ((TextView) findViewById(R.id.tvSubtitle)).setText(destination);
        }

        resendText = findViewById(R.id.tvResend);
        continueButton = findViewById(R.id.btnContinue);
        bindBoxes();

        continueButton.setOnClickListener(view -> verify());
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.tvChangeMethod).setOnClickListener(view -> finish());
        resendText.setOnClickListener(view -> resend());

        startResendTimer(resendSeconds);
    }

    @Override
    protected void onDestroy() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        super.onDestroy();
    }

    private void bindBoxes() {
        ViewGroup container = findViewById(R.id.llOtpContainer);
        for (int i = 0; i < CODE_LENGTH; i++) {
            final int index = i;
            EditText box = (EditText) container.getChildAt(i);
            boxes[i] = box;
            box.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < CODE_LENGTH - 1) {
                        boxes[index + 1].requestFocus();
                    }
                }
            });
            box.setOnKeyListener((view, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN
                        && box.getText().length() == 0 && index > 0) {
                    boxes[index - 1].setText("");
                    boxes[index - 1].requestFocus();
                    return true;
                }
                return false;
            });
        }
        boxes[0].requestFocus();
    }

    private String readCode() {
        StringBuilder code = new StringBuilder();
        for (EditText box : boxes) {
            code.append(box.getText().toString().trim());
        }
        return code.toString();
    }

    private void clearCode() {
        for (EditText box : boxes) {
            box.setText("");
        }
        boxes[0].requestFocus();
    }

    private void verify() {
        String code = readCode();
        if (code.length() != CODE_LENGTH || challengeId == null) {
            Toast.makeText(this, R.string.otp_incomplete, Toast.LENGTH_SHORT).show();
            return;
        }

        continueButton.setEnabled(false);
        if (AuthExtras.MODE_LOGIN.equals(mode)) {
            sessionManager.completeOtpLogin(challengeId, code, new SessionManager.LoginCallback() {
                @Override
                public void onSuccess(UserSession session) {
                    continueButton.setEnabled(true);
                    PostLoginRouter.route(OtpVerificationActivity.this, sessionManager, session);
                }

                @Override
                public void onOtpRequired(String newChallengeId, ArrayList<OtpMethod> methods) {
                    continueButton.setEnabled(true);
                }

                @Override
                public void onFailure(AuthError error) {
                    handleVerifyFailure(error);
                }
            });
            return;
        }

        gateway.verifyOtp(challengeId, code, new AuthenticationGateway.Callback<OtpVerifyResponse>() {
            @Override
            public void onSuccess(OtpVerifyResponse response) {
                continueButton.setEnabled(true);
                if (response.getResetToken() == null || response.getResetToken().isEmpty()) {
                    Toast.makeText(OtpVerificationActivity.this, R.string.login_unavailable,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                startActivity(new Intent(OtpVerificationActivity.this, CreateNewPasswordActivity.class)
                        .putExtra(AuthExtras.RESET_TOKEN, response.getResetToken()));
            }

            @Override
            public void onFailure(AuthError error) {
                handleVerifyFailure(error);
            }
        });
    }

    private void handleVerifyFailure(AuthError error) {
        continueButton.setEnabled(true);
        Toast.makeText(this, AuthMessages.generic(error), Toast.LENGTH_LONG).show();

        switch (error.getType()) {
            case OTP_INVALID:
                clearCode();
                return;
            case OTP_EXPIRED:
            case OTP_TOO_MANY_ATTEMPTS:
            case LOCKED:
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return;
            default:
        }
    }

    private void resend() {
        if (challengeId == null || method == null) {
            return;
        }
        resendText.setEnabled(false);
        gateway.sendOtp(challengeId, method, new AuthenticationGateway.Callback<OtpSentResponse>() {
            @Override
            public void onSuccess(OtpSentResponse response) {
                clearCode();
                startResendTimer(response.getResendInSeconds() > 0 ? response.getResendInSeconds() : 30);
                Toast.makeText(OtpVerificationActivity.this, R.string.otp_resent, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(AuthError error) {
                resendText.setEnabled(true);
                Toast.makeText(OtpVerificationActivity.this, AuthMessages.generic(error),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startResendTimer(int seconds) {
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        if (seconds <= 0) {
            showResendReady();
            return;
        }
        resendText.setEnabled(false);
        resendTimer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long remaining = (millisUntilFinished + 999L) / 1000L;
                resendText.setText(getString(R.string.otp_resend_countdown, remaining));
            }

            @Override
            public void onFinish() {
                showResendReady();
            }
        }.start();
    }

    private void showResendReady() {
        resendText.setText(R.string.otp_resend_ready);
        resendText.setEnabled(true);
    }
}
