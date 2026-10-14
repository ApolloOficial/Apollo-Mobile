package org.apollo.mobile.view.scanner;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.apollo.mobile.R;

public class ScannerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        
        // Botão voltar padrão
        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }
}
