package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.auth.error.AuthError;
import org.apollo.mobile.auth.gateway.OtpMethod;
import org.apollo.mobile.session.RememberMeStore;
import org.apollo.mobile.session.SessionManager;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.session.UserSession;

import java.util.ArrayList;

public final class LoginActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private TextView emailError;
    private TextView passwordError;
    private Button loginButton;
    private TextView loginStatus;
    private CheckBox rememberMeCheck;
    private RememberMeStore rememberMeStore;
    private boolean loading;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);
        emailError = findViewById(R.id.tvEmailError);
        passwordError = findViewById(R.id.tvPasswordError);
        loginButton = findViewById(R.id.btnLogin);
        loginStatus = findViewById(R.id.tvLoginStatus);
        rememberMeCheck = findViewById(R.id.cbRememberMe);

        rememberMeStore = new RememberMeStore(getApplicationContext());
        if (rememberMeStore.isRemembered()) {
            emailInput.setText(rememberMeStore.getEmail());
            rememberMeCheck.setChecked(true);
        }
        PasswordVisibilityToggle.attach(passwordInput);

        sessionManager = SessionManagerFactory.create(getApplicationContext());

        loginButton.setOnClickListener(view -> submitLogin());

        findViewById(R.id.tvForgotPassword).setOnClickListener(
                view -> startActivity(
                        new Intent(LoginActivity.this, ForgotPasswordStartActivity.class)
                )
        );
    }

    private void submitLogin() {
        if (loading) {
            return;
        }
        clearErrors();
        saveRememberChoice();
        setLoading(true);

        sessionManager.login(
                emailInput.getText().toString(),
                passwordInput.getText().toString(),
                new SessionManager.LoginCallback() {
                    @Override
                    public void onSuccess(UserSession session) {
                        passwordInput.setText("");
                        setLoading(false);
                        PostLoginRouter.route(LoginActivity.this, sessionManager, session);
                    }

                    @Override
                    public void onOtpRequired(String challengeId, ArrayList<OtpMethod> methods) {
                        passwordInput.setText("");
                        setLoading(false);
                        Intent intent = new Intent(LoginActivity.this, ForgotPasswordMethodActivity.class)
                                .putExtra(AuthExtras.MODE, AuthExtras.MODE_LOGIN)
                                .putExtra(AuthExtras.CHALLENGE_ID, challengeId)
                                .putExtra(AuthExtras.METHODS, methods);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(AuthError error) {
                        setLoading(false);
                        showError(error);
                    }
                }
        );
    }

    private void saveRememberChoice() {
        String email = emailInput.getText().toString().trim();
        if (rememberMeCheck.isChecked() && !email.isEmpty()) {
            rememberMeStore.remember(email);
        } else {
            rememberMeStore.forget();
        }
    }

    private void setLoading(boolean value) {
        loading = value;
        loginButton.setEnabled(!value);
        loginButton.setAlpha(value ? 0.6f : 1f);
        loginButton.setText(value ? R.string.login_connecting : R.string.login_button);
        loginStatus.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void clearErrors() {
        emailError.setVisibility(View.GONE);
        passwordError.setVisibility(View.GONE);
    }

    private void showError(AuthError error) {
        switch (error.getType()) {
            case VALIDATION:
                emailError.setText(R.string.login_required_fields);
                passwordError.setText(R.string.login_required_fields);
                emailError.setVisibility(View.VISIBLE);
                passwordError.setVisibility(View.VISIBLE);
                return;

            case INVALID_CREDENTIALS:
                emailError.setText(R.string.login_invalid_credentials);
                passwordError.setText(R.string.login_invalid_credentials);
                emailError.setVisibility(View.VISIBLE);
                passwordError.setVisibility(View.VISIBLE);
                return;

            case LOCKED:
                passwordError.setText(R.string.login_locked);
                break;

            case TIMEOUT:
                passwordError.setText(R.string.login_timeout);
                break;

            case NETWORK:
                passwordError.setText(R.string.login_network_error);
                break;

            default:
                passwordError.setText(R.string.login_unavailable);
                break;
        }

        passwordError.setVisibility(View.VISIBLE);
    }
}
