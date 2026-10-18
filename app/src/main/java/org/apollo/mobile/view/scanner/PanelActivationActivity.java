package org.apollo.mobile.view.scanner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.panels.error.PanelsError;
import org.apollo.mobile.panels.gateway.PanelItem;
import org.apollo.mobile.panels.gateway.PanelsGateway;
import org.apollo.mobile.panels.gateway.PanelsGatewayFactory;
import org.apollo.mobile.view.common.Motion;
import org.apollo.mobile.view.panels.PanelsUi;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PanelActivationActivity extends AppCompatActivity {

    private static final String EXTRA_BARCODE = "activation_barcode";
    private static final String EXTRA_SERIAL = "activation_serial";
    private static final String EXTRA_MODEL = "activation_model";
    private static final String EXTRA_STRING_ID = "activation_string_id";
    private static final String EXTRA_STRING_CODE = "activation_string_code";
    private static final String EXTRA_INVERTER = "activation_inverter";
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PanelsGateway gateway;
    private LocalDate installationDate = LocalDate.now();
    private int position = 1;
    private int total = 1;
    private TextView confirm;

    static Intent intent(Context context, PanelItem panel) {
        String model = join(panel.getManufacturer(), panel.getModel());
        return new Intent(context, PanelActivationActivity.class)
                .putExtra(EXTRA_BARCODE, panel.getBarcode())
                .putExtra(EXTRA_SERIAL, panel.getSerialNumber())
                .putExtra(EXTRA_MODEL, model)
                .putExtra(EXTRA_STRING_ID, panel.getStringId())
                .putExtra(EXTRA_STRING_CODE, panel.getStringCode())
                .putExtra(EXTRA_INVERTER, panel.getInverterCode());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_activation);
        Motion.enterScreen(this);
        gateway = PanelsGatewayFactory.create(getApplicationContext());

        String barcode = getIntent().getStringExtra(EXTRA_BARCODE);
        if (barcode == null) {
            finish();
            return;
        }
        String inverter = getIntent().getStringExtra(EXTRA_INVERTER);
        String stringCode = getIntent().getStringExtra(EXTRA_STRING_CODE);

        LinearLayout rows = findViewById(R.id.llInfoRows);
        rows.removeAllViews();
        addRow(rows, "Número de série", getIntent().getStringExtra(EXTRA_SERIAL));
        addRow(rows, "Modelo", getIntent().getStringExtra(EXTRA_MODEL));
        addRow(rows, "Inversor / String", safe(inverter) + " / " + safe(stringCode));

        ((TextView) findViewById(R.id.tvPositionTitle)).setText(
                "Inversor " + safe(inverter) + ", String " + safe(stringCode));
        ((TextView) findViewById(R.id.tvPositionSubtitle)).setText("Carregando placas da string...");

        Motion.popIn(findViewById(R.id.ivSuccessIcon), 120);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.llInstallationDate).setOnClickListener(v -> pickDate());
        findViewById(R.id.btnMinus).setOnClickListener(v -> setPosition(position - 1));
        findViewById(R.id.btnPlus).setOnClickListener(v -> setPosition(position + 1));
        confirm = findViewById(R.id.btnConfirmActivation);
        Motion.pressable(confirm);
        confirm.setOnClickListener(v -> activate(barcode));

        showDate();
        setPosition(1);
        loadString(getIntent().getStringExtra(EXTRA_STRING_ID));
    }

    private void addRow(LinearLayout rows, String label, String value) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_info_row, rows, false);
        ((TextView) row.findViewById(R.id.tvLabel)).setText(label);
        ((TextView) row.findViewById(R.id.tvValue)).setText(safe(value));
        rows.addView(row);
    }

    private void loadString(String stringId) {
        if (stringId == null) {
            ((TextView) findViewById(R.id.tvPositionSubtitle)).setText("");
            return;
        }
        gateway.panels(stringId, new PanelsGateway.Callback<List<PanelItem>>() {
            @Override
            public void onSuccess(List<PanelItem> panels) {
                if (isFinishing() || isDestroyed()) return;
                int installed = 0;
                for (PanelItem item : panels) {
                    if (!"EM_ESTOQUE".equals(item.getOperatingStats())) installed++;
                }
                total = Math.max(1, panels.size());
                ((TextView) findViewById(R.id.tvPositionSubtitle)).setText(
                        "Essa string já tem " + installed + " das " + total + " placas instaladas");
                setPosition(Math.min(total, installed + 1));
            }

            @Override
            public void onFailure(PanelsError error) {
                if (isFinishing() || isDestroyed()) return;
                ((TextView) findViewById(R.id.tvPositionSubtitle)).setText("");
            }
        });
    }

    private void setPosition(int value) {
        position = Math.max(1, Math.min(total, value));
        ((TextView) findViewById(R.id.tvPositionValue)).setText(position + " de " + total);
        ImageView minus = findViewById(R.id.btnMinus);
        ImageView plus = findViewById(R.id.btnPlus);
        minus.setImageResource(position > 1 ? R.drawable.ic_stepper_minus : R.drawable.ic_stepper_minus_disabled);
        minus.setEnabled(position > 1);
        plus.setEnabled(position < total);
        plus.setAlpha(position < total ? 1f : 0.4f);
    }

    private void pickDate() {
        org.apollo.mobile.view.common.CalendarDialog.show(this, installationDate, null, LocalDate.now(), date -> {
            installationDate = date;
            showDate();
        });
    }

    private void showDate() {
        ((TextView) findViewById(R.id.tvInstallationDate)).setText(installationDate.format(DISPLAY));
    }

    private void activate(String barcode) {
        confirm.setEnabled(false);
        gateway.activate(barcode, installationDate.toString(), new PanelsGateway.Callback<PanelItem>() {
            @Override
            public void onSuccess(PanelItem panel) {
                if (isFinishing() || isDestroyed()) return;
                org.apollo.mobile.view.common.AppToast.success(PanelActivationActivity.this, "Placa instalada com sucesso!");
                if (panel != null && panel.getStringId() != null) {
                    ScannedPanelRouter.openString(PanelActivationActivity.this, panel.getStringId(),
                            panel.getStringCode(), panel.getSerialNumber(), gateway, true);
                } else {
                    finish();
                }
            }

            @Override
            public void onFailure(PanelsError error) {
                if (isFinishing() || isDestroyed()) return;
                confirm.setEnabled(true);
                if (error.getType() == PanelsError.Type.UNAUTHORIZED
                        || error.getType() == PanelsError.Type.NETWORK) {
                    PanelsUi.handleFailure(PanelActivationActivity.this, error);
                    return;
                }
                org.apollo.mobile.view.common.AppSheets.activationFailed(PanelActivationActivity.this,
                        () -> activate(barcode));
            }
        });
    }

    private static String join(String a, String b) {
        if (a == null || a.trim().isEmpty()) return b;
        if (b == null || b.trim().isEmpty()) return a;
        return a.trim() + " " + b.trim();
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}
