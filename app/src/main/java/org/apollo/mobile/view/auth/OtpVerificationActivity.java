package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;

public final class OtpVerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        String identifier = getIntent().getStringExtra(ForgotPasswordStartActivity.EXTRA_IDENTIFIER);
        if (identifier != null) {
            ((TextView) findViewById(R.id.tvSubtitle)).setText(identifier);
        }
        findViewById(R.id.btnContinue).setOnClickListener(
                view -> startActivity(new Intent(this, CreateNewPasswordActivity.class))
        );
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }
}
