package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;

public final class ForgotPasswordStartActivity extends AppCompatActivity {

    public static final String EXTRA_IDENTIFIER = "identifier";

    private EditText identifierInput;
    private TextView inputError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_start);

        identifierInput = findViewById(R.id.etIdentifier);
        inputError = findViewById(R.id.tvInputError);

        findViewById(R.id.btnContinue).setOnClickListener(
                view -> continueRecovery()
        );

        findViewById(R.id.btnBack).setOnClickListener(
                view -> finish()
        );
    }

    private void continueRecovery() {
        String identifier = identifierInput.getText().toString().trim();

        if (identifier.isEmpty()) {
            inputError.setText(R.string.recovery_identifier_required);
            inputError.setVisibility(View.VISIBLE);
            return;
        }

        inputError.setVisibility(View.GONE);

        Intent intent = new Intent(
                ForgotPasswordStartActivity.this,
                ForgotPasswordMethodActivity.class
        );
        intent.putExtra(EXTRA_IDENTIFIER, identifier);
        startActivity(intent);
    }
}