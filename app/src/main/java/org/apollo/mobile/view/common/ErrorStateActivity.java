package org.apollo.mobile.view.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.view.chat.ChatbotActivity;

public final class ErrorStateActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 4210;
    private static final String EXTRA_TIMEOUT = "error_timeout";
    private static long lastShownAt;
    private static Runnable pendingRetry;

    public static void open(Activity activity, boolean timeout) {
        open(activity, timeout, null);
    }

    public static void open(Activity activity, boolean timeout, Runnable retry) {
        pendingRetry = retry;
        long now = System.currentTimeMillis();
        if (now - lastShownAt < 1500 || activity.isFinishing()) return;
        lastShownAt = now;
        Intent intent = new Intent(activity, ErrorStateActivity.class).putExtra(EXTRA_TIMEOUT, timeout);
        activity.startActivityForResult(intent, REQUEST_CODE);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean timeout = getIntent().getBooleanExtra(EXTRA_TIMEOUT, false);
        setContentView(timeout ? R.layout.activity_error_loading : R.layout.activity_error_unexpected);
        Motion.enterScreen(this);
        Motion.popIn(findViewById(R.id.ivErrorIllustration), 120);
        findViewById(R.id.btnTryAgain).setOnClickListener(v -> {
            setResult(RESULT_OK);
            Runnable retry = pendingRetry;
            pendingRetry = null;
            finish();
            if (retry != null) retry.run();
        });
        findViewById(R.id.btnSupport).setOnClickListener(v ->
                startActivity(new Intent(this, ChatbotActivity.class)));
        Motion.pressable(findViewById(R.id.btnTryAgain), findViewById(R.id.btnSupport));
    }
}
