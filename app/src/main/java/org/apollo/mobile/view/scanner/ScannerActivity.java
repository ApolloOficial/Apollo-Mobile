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
import org.apollo.mobile.maintenance.OsPanelMatcher;
import org.apollo.mobile.maintenance.OsVerificationStore;
import org.apollo.mobile.maintenance.gateway.ServiceOrderError;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGateway;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGatewayFactory;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Details;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Panel;
import org.apollo.mobile.view.common.AppToast;
import org.apollo.mobile.view.maintenance.os.MaintenanceIdentifiedActivity;
import org.apollo.mobile.view.maintenance.os.OsDetailsActivity;
import org.apollo.mobile.panels.ScanCode;
import org.apollo.mobile.panels.error.PanelsError;
import org.apollo.mobile.panels.gateway.PanelItem;
import org.apollo.mobile.panels.gateway.PanelsGateway;
import org.apollo.mobile.panels.gateway.PanelsGatewayFactory;
import org.apollo.mobile.view.common.Motion;
import org.apollo.mobile.view.panels.PanelsUi;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScannerActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = OsDetailsActivity.EXTRA_ORDER_ID;

    private final AtomicBoolean busy = new AtomicBoolean(false);

    private long orderId = -1L;
    private Details orderDetails;
    private OsVerificationStore verificationStore;
    private final ActivityResultLauncher<android.content.Intent> identifyPanel = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setResult(RESULT_OK);
                    finish();
                    return;
                }
                busy.set(false);
            });

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
        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1L);
        if (isOrderMode()) {
            verificationStore = new OsVerificationStore(this);
            ((TextView) findViewById(R.id.tvScanTitle)).setText("Escaneie as placas da OS" + orderId);
            ((TextView) findViewById(R.id.tvScanSubtitle)).setText("Carregando placas...");
            busy.set(true);
            loadOrder();
        }
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
                if (isOrderMode()) {
                    runOnUiThread(() -> identifyByCode(value));
                } else {
                    runOnUiThread(() -> lookup(value, false));
                }
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
        ScannerOverlayView overlay = findViewById(R.id.scannerOverlay);
        overlay.showSuccess(() -> {
            if (isFinishing() || isDestroyed()) return;
            ScannedPanelRouter.open(this, panel);
        });
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
        if (isOrderMode()) {
            input.setHint("Final do número de série (mín. " + OsPanelMatcher.MIN_SUFFIX + ")");
        }
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
            if (value == null) {
                return;
            }
            if (isOrderMode()) {
                if (OsPanelMatcher.canonical(value).length() < OsPanelMatcher.MIN_SUFFIX) {
                    input.setError("Digite pelo menos " + OsPanelMatcher.MIN_SUFFIX + " caracteres.");
                    return;
                }
                submitted[0] = true;
                dialog.dismiss();
                identifyBySerialEnd(value);
                return;
            }
            submitted[0] = true;
            dialog.dismiss();
            lookup(value, true);
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
    protected void onResume() {
        super.onResume();
        ScannerOverlayView overlay = findViewById(R.id.scannerOverlay);
        if (overlay != null) overlay.reset();
        if (!isOrderMode() || orderDetails != null) busy.set(false);
    }

    private boolean isOrderMode() {
        return orderId > 0;
    }

    private void loadOrder() {
        ServiceOrderGatewayFactory.create(getApplicationContext()).details(orderId,
                new ServiceOrderGateway.Callback<Details>() {
                    @Override
                    public void onSuccess(Details value) {
                        if (isFinishing() || isDestroyed()) return;
                        orderDetails = value;
                        updateOrderProgress();
                        busy.set(false);
                    }

                    @Override
                    public void onFailure(ServiceOrderError error) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(ScannerActivity.this,
                                "Não foi possível carregar as placas da OS.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    private void updateOrderProgress() {
        if (orderDetails == null) return;
        int total = orderDetails.getTargetPanels().size();
        int done = 0;
        java.util.Map<Long, OsVerificationStore.Result> results = verificationStore.results(orderId);
        for (Panel panel : orderDetails.getTargetPanels()) {
            if (results.containsKey(panel.getId())) done++;
        }
        ((TextView) findViewById(R.id.tvScanSubtitle)).setText(
                done + " de " + total + (total == 1 ? " placa verificada" : " placas verificadas"));
    }

    private void identifyByCode(String code) {
        if (orderDetails == null) {
            busy.set(false);
            return;
        }
        Panel panel = OsPanelMatcher.byCode(orderDetails.getTargetPanels(), code);
        if (panel != null) {
            startIfNeeded(panel);
            return;
        }
        boolean sameString = OsPanelMatcher.byCode(orderDetails.getPanels(), code) != null;
        Toast.makeText(this, sameString
                ? "Essa não é a placa desta OS. Procure a placa " + targetSerialHint() + "."
                : "A placa " + code + " não faz parte desta OS.", Toast.LENGTH_LONG).show();
        findViewById(R.id.previewView).postDelayed(() -> busy.set(false), 1500);
    }

    private String targetSerialHint() {
        List<Panel> targets = orderDetails.getTargetPanels();
        return targets.size() == 1 ? targets.get(0).getSerialNumber() : "indicada";
    }

    private void identifyBySerialEnd(String typed) {
        if (orderDetails == null) {
            busy.set(false);
            return;
        }
        List<Panel> candidates = OsPanelMatcher.bySerialEnd(orderDetails.getTargetPanels(), typed);
        if (candidates.isEmpty()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Nenhuma placa encontrada")
                    .setMessage("Pode ser erro de digitação, placa de outra string ou placa não cadastrada.")
                    .setPositiveButton("Tentar de novo", (d, w) -> busy.set(false))
                    .setNegativeButton("Seguir sem identificar", (d, w) -> confirmUnidentified())
                    .setOnCancelListener(d -> busy.set(false))
                    .show();
            return;
        }
        String[] labels = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            Panel panel = candidates.get(i);
            int position = orderDetails.positionOf(panel);
            labels[i] = panel.getSerialNumber() + (position > 0 ? "  ·  posição " + position : "");
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(candidates.size() == 1 ? "É esta placa?" : "Compare com a etiqueta")
                .setItems(labels, (d, which) -> startIfNeeded(candidates.get(which)))
                .setNegativeButton("Cancelar", (d, w) -> busy.set(false))
                .setOnCancelListener(d -> busy.set(false))
                .show();
    }

    private void confirmUnidentified() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Seguir sem identificar a placa?")
                .setMessage("A OS será concluída no nível da string e a ressalva vai para o laudo.")
                .setPositiveButton("Seguir", (d, w) -> startThen(() -> {
                    verificationStore.markUnidentified(orderId, true);
                    setResult(RESULT_OK);
                    finish();
                }))
                .setNegativeButton("Voltar", (d, w) -> busy.set(false))
                .setOnCancelListener(d -> busy.set(false))
                .show();
    }

    private void startIfNeeded(Panel panel) {
        ScannerOverlayView overlay = findViewById(R.id.scannerOverlay);
        overlay.showSuccess(() -> {
            if (isFinishing() || isDestroyed()) return;
            overlay.reset();
            openIdentified(panel);
        });
    }

    private void startThen(Runnable next) {
        if (!"ABERTA".equals(orderDetails.getOrder().getMaintenanceStatus())) {
            next.run();
            return;
        }
        findViewById(R.id.progressScan).setVisibility(View.VISIBLE);
        ServiceOrderGatewayFactory.create(getApplicationContext()).start(orderId,
                new ServiceOrderGateway.Callback<org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order>() {
                    @Override
                    public void onSuccess(org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order value) {
                        if (isFinishing() || isDestroyed()) return;
                        findViewById(R.id.progressScan).setVisibility(View.GONE);
                        loadOrderSilently();
                        AppToast.success(ScannerActivity.this, "Manutenção iniciada");
                        next.run();
                    }

                    @Override
                    public void onFailure(ServiceOrderError error) {
                        if (isFinishing() || isDestroyed()) return;
                        findViewById(R.id.progressScan).setVisibility(View.GONE);
                        Toast.makeText(ScannerActivity.this,
                                "Não foi possível iniciar a manutenção.", Toast.LENGTH_LONG).show();
                        findViewById(R.id.previewView).postDelayed(() -> busy.set(false), 1500);
                    }
                });
    }

    private void loadOrderSilently() {
        ServiceOrderGatewayFactory.create(getApplicationContext()).details(orderId,
                new ServiceOrderGateway.Callback<Details>() {
                    @Override
                    public void onSuccess(Details value) {
                        if (!isFinishing() && !isDestroyed()) orderDetails = value;
                    }

                    @Override
                    public void onFailure(ServiceOrderError error) {
                    }
                });
    }

    private void openIdentified(Panel panel) {
        android.content.Intent intent = new android.content.Intent(this, MaintenanceIdentifiedActivity.class);
        intent.putExtra(OsDetailsActivity.EXTRA_ORDER_ID, orderId);
        intent.putExtra(MaintenanceIdentifiedActivity.EXTRA_PANEL_ID, panel.getId());
        intent.putExtra(MaintenanceIdentifiedActivity.EXTRA_SERIAL, panel.getSerialNumber());
        intent.putExtra(MaintenanceIdentifiedActivity.EXTRA_STRING,
                safe(panel.getInverterCode()) + " / " + safe(panel.getStringCode()));
        intent.putExtra(MaintenanceIdentifiedActivity.EXTRA_POSITION, orderDetails.positionOf(panel));
        intent.putExtra(MaintenanceIdentifiedActivity.EXTRA_STATUS, panel.getOperatingStats());
        intent.putExtra(MaintenanceIdentifiedActivity.EXTRA_OPEN,
                "ABERTA".equals(orderDetails.getOrder().getMaintenanceStatus()));
        identifyPanel.launch(intent);
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.close();
        analysisExecutor.shutdown();
    }
}
