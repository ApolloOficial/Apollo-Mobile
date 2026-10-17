package org.apollo.mobile.view.scanner;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.apollo.mobile.R;
import org.apollo.mobile.view.common.Motion;

public class ScannerActivity extends AppCompatActivity {

    private Camera camera;
    private final ActivityResultLauncher<String> cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        org.apollo.mobile.view.common.SystemBars.applyDark(this);
        Motion.stagger(80, findViewById(R.id.btnBack), findViewById(R.id.tvScanTitle),
                findViewById(R.id.tvScanSubtitle), findViewById(R.id.tvManualCode));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.ivFlash).setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            v.setAlpha(v.isSelected() ? 1f : 0.85f);
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                camera.getCameraControl().enableTorch(v.isSelected());
            }
            Motion.bounce(v);
        });

        TextView manual = findViewById(R.id.tvManualCode);
        manual.setPaintFlags(manual.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        manual.setOnClickListener(v -> showManualCodeSheet());

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermission.launch(android.Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        PreviewView previewView = findViewById(R.id.previewView);
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                provider.unbindAll();
                camera = provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview);
            } catch (Exception ignored) {
                camera = null;
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void showManualCodeSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.sheet_manual_code, null, false);
        dialog.setContentView(sheet);
        View parent = (View) sheet.getParent();
        if (parent != null) parent.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        EditText input = sheet.findViewById(R.id.etManualCode);
        View search = sheet.findViewById(R.id.btnSearchCode);
        sheet.findViewById(R.id.btnCloseSheet).setOnClickListener(v -> dialog.dismiss());
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        search.setOnClickListener(v -> dialog.dismiss());
        Motion.pressable(search);
        dialog.show();
    }
}
