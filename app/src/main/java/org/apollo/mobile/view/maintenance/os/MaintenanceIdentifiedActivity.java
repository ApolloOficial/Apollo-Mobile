package org.apollo.mobile.view.maintenance.os;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.maintenance.OsVerificationStore;
import org.apollo.mobile.maintenance.gateway.ServiceOrderError;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGateway;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGatewayFactory;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order;
import org.apollo.mobile.view.common.Motion;

public final class MaintenanceIdentifiedActivity extends AppCompatActivity {

    public static final String EXTRA_PANEL_ID = "extra_panel_id";
    public static final String EXTRA_SERIAL = "extra_serial";
    public static final String EXTRA_STRING = "extra_string";
    public static final String EXTRA_POSITION = "extra_position";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_OPEN = "extra_open";

    private long orderId;
    private long panelId;
    private String serial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_identified);
        Motion.enterScreen(this);

        orderId = getIntent().getLongExtra(OsDetailsActivity.EXTRA_ORDER_ID, -1L);
        panelId = getIntent().getLongExtra(EXTRA_PANEL_ID, -1L);
        serial = getIntent().getStringExtra(EXTRA_SERIAL);
        if (orderId <= 0 || panelId <= 0) {
            finish();
            return;
        }

        LinearLayout rows = findViewById(R.id.llInfoRows);
        rows.removeAllViews();
        int position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        addRow(rows, "Inversor / String", getIntent().getStringExtra(EXTRA_STRING));
        addRow(rows, "Posição", position > 0 ? String.valueOf(position) : null);
        addRow(rows, "Número de série", serial);
        addRow(rows, "Status", statusLabel(getIntent().getStringExtra(EXTRA_STATUS)));
        addRow(rows, "Ordem de serviço", "OS" + orderId);

        Motion.popIn(findViewById(R.id.ivSuccessIcon), 120);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        View start = findViewById(R.id.btnStartMaintenance);
        Motion.pressable(start);
        start.setOnClickListener(this::begin);
    }

    private void addRow(LinearLayout rows, String label, String value) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_info_row, rows, false);
        ((TextView) row.findViewById(R.id.tvLabel)).setText(label);
        ((TextView) row.findViewById(R.id.tvValue)).setText(ServiceOrderUi.safe(value));
        rows.addView(row);
    }

    private void begin(View button) {
        if (!getIntent().getBooleanExtra(EXTRA_OPEN, false)) {
            proceed();
            return;
        }
        button.setEnabled(false);
        ServiceOrderGatewayFactory.create(getApplicationContext()).start(orderId,
                new ServiceOrderGateway.Callback<Order>() {
                    @Override
                    public void onSuccess(Order value) {
                        if (isFinishing() || isDestroyed()) return;
                        proceed();
                    }

                    @Override
                    public void onFailure(ServiceOrderError error) {
                        if (isFinishing() || isDestroyed()) return;
                        button.setEnabled(true);
                        org.apollo.mobile.view.common.AppToast.error(MaintenanceIdentifiedActivity.this, "Não foi possível iniciar a manutenção.");
                    }
                });
    }

    private void proceed() {
        new OsVerificationStore(this).setPendingCondition(orderId, panelId);
        setResult(RESULT_OK);
        finish();
    }

    private static String statusLabel(String status) {
        if (status == null) return null;
        switch (status) {
            case "EM_OPERACAO":
                return "Em operação";
            case "EM_ESTOQUE":
                return "Em estoque";
            case "MANUTENCAO":
            case "EM_MANUTENCAO":
                return "Em manutenção";
            case "INATIVA":
            case "DESATIVADA":
                return "Desativada";
            default:
                return status.charAt(0) + status.substring(1).toLowerCase().replace('_', ' ');
        }
    }
}
