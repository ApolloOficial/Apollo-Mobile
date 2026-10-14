package org.apollo.mobile.view.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import org.apollo.mobile.R;
import org.apollo.mobile.view.chat.ChatbotActivity;
import org.apollo.mobile.view.home.HomeActivity;
import org.apollo.mobile.view.scanner.ScannerActivity;
import org.apollo.mobile.view.settings.SettingsActivity;

public class MaintenanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        setupBottomNavigation();
        setupHeader();
        setupFab();
    }

    private void setupHeader() {
        ImageButton ibNotifications = findViewById(R.id.ibNotifications);
        ibNotifications.setOnClickListener(v -> {
            // Logic to open notifications
        });

        ImageView ivProfile = findViewById(R.id.ivProfile);
        ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void setupFab() {
        findViewById(R.id.fabChatbot).setOnClickListener(v -> {
            startActivity(new Intent(this, ChatbotActivity.class));
        });
    }

    private void setupBottomNavigation() {
        findViewById(R.id.ivHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        findViewById(R.id.ivPanels).setOnClickListener(v -> {
            // Navigate to Panels if activity exists
        });

        findViewById(R.id.cvScanContainer).setOnClickListener(v -> {
            startActivity(new Intent(this, ScannerActivity.class));
        });

        findViewById(R.id.ivSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });
        
        // ivTools is the current section
    }
}
