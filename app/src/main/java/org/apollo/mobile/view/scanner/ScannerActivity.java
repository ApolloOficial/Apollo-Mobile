package org.apollo.mobile.view.scanner;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.apollo.mobile.R;
import org.apollo.mobile.panels.ScanCode;
import org.apollo.mobile.panels.error.PanelsError;
import org.apollo.mobile.panels.gateway.PanelItem;
import org.apollo.mobile.panels.gateway.PanelsGateway;
import org.apollo.mobile.panels.gateway.PanelsGatewayFactory;
import org.apollo.mobile.view.common.Motion;
import org.apollo.mobile.view.panels.PanelSheet;
import org.apollo.mobile.view.panels.PanelsUi;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScannerActivity extends AppCompatActivity {

    private final AtomicBoolean busy = new AtomicBoolean(false);
    private PanelsGateway gateway;
    private ExecutorService analysisExecutor;
    private BarcodeScanner scanner;
    private Camera camera;
    private final ActivityResultLauncher<String> cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(this, R.string.scanner_permission_denied, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        org.apollo.mobile.view.common.SystemBars.applyDark(this);
        gateway = PanelsGatewayFactory.create(getApplicationContext());
        analysisExecutor = Executors.newSingleThreadExecutor();
        scanner = BarcodeScanning.getClient(new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_CODE_93,
                        Barcode.FORMAT_ITF,
                        Barcode.FORMAT_CODABAR,
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_DATA_MATRIX)
                .build());
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
                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                analysis.setAnalyzer(analysisExecutor, this::analyze);
                provider.unbindAll();
                camera = provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);
            } catch (Exception ignored) {
                camera = null;
                Toast.makeText(this, R.string.scanner_camera_error, Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyze(ImageProxy proxy) {
        if (busy.get() || proxy.getImage() == null) {
            proxy.close();
            return;
        }
        InputImage image = InputImage.fromMediaImage(proxy.getImage(), proxy.getImageInfo().getRotationDegrees());
        scanner.process(image)
                .addOnSuccessListener(this::onCodes)
                .addOnCompleteListener(task -> proxy.close());
    }

    private void onCodes(List<Barcode> codes) {
        for (Barcode code : codes) {
            String value = ScanCode.normalize(code.getRawValue());
            if (value != null && busy.compareAndSet(false, true)) {
                runOnUiThread(() -> lookup(value, false));
                return;
            }
        }
    }

    private void lookup(String code, boolean allowSerial) {
        gateway.findByBarcode(code, new PanelsGateway.Callback<PanelItem>() {
            @Override
            public void onSuccess(PanelItem panel) {
                showPanel(panel);
            }

            @Override
            public void onFailure(PanelsError error) {
                if (allowSerial && error.getType() == PanelsError.Type.NOT_FOUND) {
                    lookupSerial(code);
                } else {
                    fail(code, error);
                }
            }
        });
    }

    private void lookupSerial(String code) {
        gateway.findBySerial(code, new PanelsGateway.Callback<PanelItem>() {
            @Override
            public void onSuccess(PanelItem panel) {
                if (panel == null) {
                    fail(code, new PanelsError(PanelsError.Type.NOT_FOUND));
                } else {
                    showPanel(panel);
                }
            }

            @Override
            public void onFailure(PanelsError error) {
                fail(code, error);
            }
        });
    }

    private void showPanel(PanelItem panel) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        PanelSheet.show(this, panel, panel.getStringCode(), () -> busy.set(false));
    }

    private void fail(String code, PanelsError error) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (error.getType() == PanelsError.Type.NOT_FOUND) {
            Toast.makeText(this, getString(R.string.scanner_not_found, code), Toast.LENGTH_LONG).show();
        } else if (PanelsUi.handleFailure(this, error)) {
            return;
        }
        findViewById(R.id.previewView).postDelayed(() -> busy.set(false), 1500);
    }

    private void showManualCodeSheet() {
        if (!busy.compareAndSet(false, true)) {
            return;
        }
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
        boolean[] submitted = {false};
        search.setOnClickListener(v -> {
            String value = ScanCode.normalize(input.getText().toString());
            if (value != null) {
                submitted[0] = true;
                dialog.dismiss();
                lookup(value, true);
            }
        });
        dialog.setOnDismissListener(d -> {
            if (!submitted[0]) {
                busy.set(false);
            }
        });
        Motion.pressable(search);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.close();
        analysisExecutor.shutdown();
    }
}
