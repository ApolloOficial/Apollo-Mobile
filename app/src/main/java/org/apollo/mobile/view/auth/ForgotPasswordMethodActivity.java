package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.view.auth.ForgotPasswordStartActivity;
import org.apollo.mobile.view.auth.OtpVerificationActivity;
import org.apollo.mobile.R;

public final class ForgotPasswordMethodActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_method);

        String identifier = getIntent().getStringExtra(ForgotPasswordStartActivity.EXTRA_IDENTIFIER);
        if (identifier != null && identifier.contains("@")) {
            ((RadioButton) findViewById(R.id.rbEmail)).setText("Enviar via E-mail\n" + identifier);
        }
        findViewById(R.id.btnContinue).setOnClickListener(view -> {
            Intent intent = new Intent(this, OtpVerificationActivity.class)
                    .putExtra(ForgotPasswordStartActivity.EXTRA_IDENTIFIER, identifier);
            startActivity(intent);
        });
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }
}
