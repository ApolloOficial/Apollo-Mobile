package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;

public final class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.btnStart).setOnClickListener(view -> openAccessInfo());
        findViewById(R.id.tvLoginLink).setOnClickListener(view -> openLogin());
        findViewById(R.id.tvLoginLink2).setOnClickListener(view -> openLogin());
    }

    private void openAccessInfo() {
        startActivity(new Intent(this, AccessDeniedActivity.class));
    }

    private void openLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
