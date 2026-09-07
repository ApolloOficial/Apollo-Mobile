package org.apollo.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.apollo.mobile.view.auth.WelcomeActivity;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 1200L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable openWelcomeScreen = () -> {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.postDelayed(openWelcomeScreen, SPLASH_DURATION_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openWelcomeScreen);
        super.onDestroy();
    }
}