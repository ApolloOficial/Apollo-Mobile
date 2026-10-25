package org.apollo.mobile.view.maintenance.os;

import android.content.Intent;
import org.apollo.mobile.tracking.TrackingEvents;
import org.apollo.mobile.tracking.TrackingManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.maintenance.OsVerificationStore;
import org.apollo.mobile.maintenance.gateway.ServiceOrderError;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGateway;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGatewayFactory;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Details;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Panel;
import org.apollo.mobile.view.common.BottomNavigation;
import org.apollo.mobile.view.common.Motion;

import java.util.Map;

public final class CompleteMaintenanceActivity extends AppCompatActivity {
    static final String REPORT_SEPARATOR = "\n\n— ";
    private ServiceOrderGateway gateway;
    private OsVerificationStore store;
    private long orderId;
    private Details details;
    private EditText report;
    private EditText laborCost;
    private TextView finishButton;
    private boolean trackingReportStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_maintenance);
        Motion.enterScreen(this);

        orderId = getIntent().getLongExtra(OsDetailsActivity.EXTRA_ORDER_ID, -1L);
        if (orderId <= 0) {
            finish();
            return;
        }

        BottomNavigation.setup(this, BottomNavigation.Tab.TOOLS);
        gateway = ServiceOrderGatewayFactory.create(getApplicationContext());
        store = new OsVerificationStore(this);
        report = findViewById(R.id.etReport);
        trackingReportStarted = savedInstanceState != null
                && savedInstanceState.getBoolean("tracking.report_started", false);
        report.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!trackingReportStarted && s.toString().trim().length() > 0) {
                    org.apollo.mobile.tracking.lifecycle.TrackingForm.started(CompleteMaintenanceActivity.this);
                    trackingReportStarted = true;
                    TrackingManager.order(TrackingEvents.REPORT_STARTED,
                            "CompleteMaintenanceActivity", orderId);
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) { }
        });
        laborCost = findViewById(R.id.etLaborCost);
        finishButton = findViewById(R.id.btnFinishMaintenance);

        ((TextView) findViewById(R.id.tvTitle)).setText(org.apollo.mobile.view.common.Tr.t("Concluir OS") + orderId);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        Motion.pressable(finishButton);
        finishButton.setOnClickListener(view -> validate());

        loadSummary();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("tracking.report_started", trackingReportStarted);
        super.onSaveInstanceState(outState);
    }

    private void loadSummary() {
        gateway.details(orderId, new ServiceOrderGateway.Callback<Details>() {
            @Override
            public void onSuccess(Details value) {
                if (isFinishing() || isDestroyed()) return;
                details = value;
                renderSummary();
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                if (isFinishing() || isDestroyed()) return;
                org.apollo.mobile.view.common.AppToast.error(CompleteMaintenanceActivity.this, org.apollo.mobile.view.common.Tr.t("Não foi possível carregar o resumo."));
            }
        });
    }

    private void renderSummary() {
        View card = findViewById(R.id.cardSummary);
        boolean visible = OsSummary.bind(card, details, store.results(orderId), store.isUnidentified(orderId));
        card.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) Motion.fadeUp(card, 0);
    }

    private void validate() {
        org.apollo.mobile.tracking.lifecycle.TrackingForm.submitted(this);
        String text = report.getText().toString().trim();
        if (text.isEmpty()) {
            org.apollo.mobile.tracking.lifecycle.TrackingForm.invalid(this, "report", "required");
            report.setError(org.apollo.mobile.view.common.Tr.t("Informe o laudo da manutenção."));
            report.requestFocus();
            Motion.shake(report);
            return;
        }
        Double cost = parseCost();
        if (cost == null) {
            org.apollo.mobile.tracking.lifecycle.TrackingForm.invalid(this, "labor_cost", "invalid");
            laborCost.setError(org.apollo.mobile.view.common.Tr.t("Valor inválido."));
            laborCost.requestFocus();
            Motion.shake(laborCost);
            return;
        }

        complete(text, cost);
    }

    private Double parseCost() {
        String raw = laborCost.getText().toString().trim().replace("R$", "").replace(" ", "");
        if (raw.isEmpty()) return 0.0;
        if (raw.contains(",")) raw = raw.replace(".", "").replace(',', '.');
        try {
            double value = Double.parseDouble(raw);
            return value < 0 ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildReport(String text) {
        StringBuilder builder = new StringBuilder(text);
        Map<Long, OsVerificationStore.Result> results = store.results(orderId);
        if (details != null && !details.getTargetPanels().isEmpty()) {
            builder.append(REPORT_SEPARATOR).append("Placas verificadas: ").append(results.size())
                    .append(org.apollo.mobile.view.common.Tr.t(" de ")).append(details.getTargetPanels().size()).append('.');
            for (OsVerificationStore.Result result : results.values()) {
                builder.append("\n- ").append(result.getSerial()).append(": ")
                        .append(result.hasProblem() ? result.getProblem() : org.apollo.mobile.view.common.Tr.t("sem problema aparente"));
            }
        }
        if (store.isUnidentified(orderId)) {
            builder.append(builder.indexOf(REPORT_SEPARATOR) >= 0 ? "\n" : REPORT_SEPARATOR).append("Ressalva: placa não identificada (etiqueta ilegível). OS concluída no nível da string.");
        }
        return builder.toString();
    }

    private void complete(String text, double cost) {
        finishButton.setEnabled(false);
        gateway.complete(orderId, buildReport(text), cost, new ServiceOrderGateway.Callback<Order>() {
            @Override
            public void onSuccess(Order value) {
                org.apollo.mobile.tracking.lifecycle.TrackingForm.succeeded(CompleteMaintenanceActivity.this);
                setResult(RESULT_OK);
                Intent intent = new Intent(CompleteMaintenanceActivity.this, OsDetailsActivity.class);
                intent.putExtra(OsDetailsActivity.EXTRA_ORDER_ID, orderId);
                intent.putExtra(OsDetailsActivity.EXTRA_COMPLETED, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                if (isFinishing() || isDestroyed()) return;
                finishButton.setEnabled(true);
                org.apollo.mobile.view.common.AppSheets.completionFailed(CompleteMaintenanceActivity.this,
                        () -> complete(text, cost));
            }
        });
    }
}
