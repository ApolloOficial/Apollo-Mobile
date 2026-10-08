package org.apollo.mobile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.apollo.mobile.auth.api.ServerWarmUp;
import org.apollo.mobile.view.auth.StartupRouter;

public class MainActivity extends AppCompatActivity {
    private static final long SPLASH_DURATION_MS = 1200L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable openStartScreen = () -> {
        startActivity(StartupRouter.destination(MainActivity.this));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ServerWarmUp.start();

        handler.postDelayed(openStartScreen, SPLASH_DURATION_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openStartScreen);
        super.onDestroy();
    }
}
