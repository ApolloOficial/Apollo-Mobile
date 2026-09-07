package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.auth.error.AuthError;
import org.apollo.mobile.session.SessionManager;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.session.UserSession;

public final class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private TextView emailError;
    private TextView passwordError;
    private View loginButton;
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

        sessionManager = SessionManagerFactory.create(getApplicationContext());

        loginButton.setOnClickListener(view -> submitLogin());

        findViewById(R.id.tvForgotPassword).setOnClickListener(
                view -> startActivity(
                        new Intent(LoginActivity.this, ForgotPasswordStartActivity.class)
                )
        );
    }

    private void submitLogin() {
        clearErrors();
        loginButton.setEnabled(false);

        sessionManager.login(
                emailInput.getText().toString(),
                passwordInput.getText().toString(),
                new SessionManager.LoginCallback() {
                    @Override
                    public void onSuccess(UserSession session) {
                        passwordInput.setText("");
                        loginButton.setEnabled(true);

                        /*
                         * A Home ainda não existe nessa branch. A navegação por role será conectada depois,
                         * junto com TechnicianHomeActivity e AccessDeniedActivity.
                         */
                        Toast.makeText(
                                LoginActivity.this,
                                R.string.login_success,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onFailure(AuthError error) {
                        loginButton.setEnabled(true);
                        showError(error);
                    }
                }
        );
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