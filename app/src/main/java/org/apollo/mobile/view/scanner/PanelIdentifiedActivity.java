package org.apollo.mobile.view.scanner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.panels.error.PanelsError;
import org.apollo.mobile.panels.gateway.PanelItem;
import org.apollo.mobile.panels.gateway.PanelsGateway;
import org.apollo.mobile.panels.gateway.PanelsGatewayFactory;
import org.apollo.mobile.view.common.Motion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class PanelIdentifiedActivity extends AppCompatActivity {

    private static final String EXTRA_ID = "identified_id";
    private static final String EXTRA_SERIAL = "identified_serial";
    private static final String EXTRA_MODEL = "identified_model";
    private static final String EXTRA_POWER = "identified_power";
    private static final String EXTRA_DATE = "identified_date";
    private static final String EXTRA_STRING_ID = "identified_string_id";
    private static final String EXTRA_STRING_CODE = "identified_string_code";
    private static final String EXTRA_INVERTER = "identified_inverter";
    private static final String EXTRA_EFFICIENCY = "identified_efficiency";

    private PanelsGateway gateway;
    private TextView positionValue;

    static Intent intent(Context context, PanelItem panel) {
        Double efficiency = panel.getEfficiencyPct() != null ? panel.getEfficiencyPct() : panel.getStringHealthScore();
        return new Intent(context, PanelIdentifiedActivity.class)
                .putExtra(EXTRA_ID, panel.getId())
                .putExtra(EXTRA_SERIAL, panel.getSerialNumber())
                .putExtra(EXTRA_MODEL, join(panel.getManufacturer(), panel.getModel()))
                .putExtra(EXTRA_POWER, panel.getPmaxW() == null ? -1d : panel.getPmaxW())
                .putExtra(EXTRA_DATE, panel.getInstallationDt())
                .putExtra(EXTRA_STRING_ID, panel.getStringId())
                .putExtra(EXTRA_STRING_CODE, panel.getStringCode())
                .putExtra(EXTRA_INVERTER, panel.getInverterCode())
                .putExtra(EXTRA_EFFICIENCY, efficiency == null ? -1d : efficiency);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_identified);
        Motion.enterScreen(this);
        gateway = PanelsGatewayFactory.create(getApplicationContext());

        String serial = getIntent().getStringExtra(EXTRA_SERIAL);
        String stringId = getIntent().getStringExtra(EXTRA_STRING_ID);
        String stringCode = getIntent().getStringExtra(EXTRA_STRING_CODE);
        double power = getIntent().getDoubleExtra(EXTRA_POWER, -1d);
        double efficiency = getIntent().getDoubleExtra(EXTRA_EFFICIENCY, -1d);

        LinearLayout rows = findViewById(R.id.llInfoRows);
        rows.removeAllViews();
        addRow(rows, "Número de série", serial);
        addRow(rows, "Modelo", getIntent().getStringExtra(EXTRA_MODEL));
        addRow(rows, "Potência de etiqueta", power >= 0 ? String.format(Locale.US, "%.0fW", power) : null);
        addRow(rows, "Data de instalação", formatDate(getIntent().getStringExtra(EXTRA_DATE)));
        addRow(rows, "Inversor / String",
                safe(getIntent().getStringExtra(EXTRA_INVERTER)) + " / " + safe(stringCode));
        positionValue = addRow(rows, "Posição", "...");
        addRow(rows, "Eficiência", efficiency >= 0 ? Math.round(efficiency) + "%" : null);

        Motion.popIn(findViewById(R.id.ivSuccessIcon), 120);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        TextView go = findViewById(R.id.btnGoToString);
        go.setText("Ir para string " + safe(stringCode) + " · " + safe(serial));
        Motion.pressable(go);
        go.setOnClickListener(v -> openString());

        loadPosition(stringId, getIntent().getLongExtra(EXTRA_ID, -1L));
    }

    private TextView addRow(LinearLayout rows, String label, String value) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_info_row, rows, false);
        ((TextView) row.findViewById(R.id.tvLabel)).setText(label);
        TextView valueView = row.findViewById(R.id.tvValue);
        valueView.setText(safe(value));
        rows.addView(row);
        return valueView;
    }

    private void loadPosition(String stringId, long panelId) {
        if (stringId == null) {
            positionValue.setText("-");
            return;
        }
        gateway.panels(stringId, new PanelsGateway.Callback<List<PanelItem>>() {
            @Override
            public void onSuccess(List<PanelItem> panels) {
                if (isFinishing() || isDestroyed()) return;
                for (int i = 0; i < panels.size(); i++) {
                    if (panels.get(i).getId() == panelId) {
                        positionValue.setText(String.valueOf(i + 1));
                        return;
                    }
                }
                positionValue.setText("-");
            }

            @Override
            public void onFailure(PanelsError error) {
                if (!isFinishing() && !isDestroyed()) positionValue.setText("-");
            }
        });
    }

    private void openString() {
        PanelItemRef ref = new PanelItemRef(getIntent().getStringExtra(EXTRA_STRING_ID),
                getIntent().getStringExtra(EXTRA_STRING_CODE), getIntent().getStringExtra(EXTRA_SERIAL));
        ScannedPanelRouter.openString(this, ref.stringId, ref.stringCode, ref.serial, gateway, true);
    }

    private static final class PanelItemRef {
        final String stringId;
        final String stringCode;
        final String serial;

        PanelItemRef(String stringId, String stringCode, String serial) {
            this.stringId = stringId;
            this.stringCode = stringCode;
            this.serial = serial;
        }
    }

    private static String formatDate(String value) {
        if (value == null || value.length() < 10) return value;
        try {
            return LocalDate.parse(value.substring(0, 10)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (RuntimeException e) {
            return value;
        }
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
