package org.apollo.mobile.view.panels;

import android.os.Bundle;
import org.apollo.mobile.view.common.Motion;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;

import org.apollo.mobile.R;
import org.apollo.mobile.panels.error.PanelsError;
import org.apollo.mobile.panels.gateway.PanelIssueBody;
import org.apollo.mobile.panels.gateway.PanelsGateway;
import org.apollo.mobile.panels.gateway.PanelsGatewayFactory;

public final class ReportIssueActivity extends AppCompatActivity {
    static final String EXTRA_BARCODE = "barcode";
    static final String EXTRA_SERIAL = "serial";

    private static final String[] SEVERITY_VALUES = {"BAIXA", "MEDIA", "ALTA", "CRITICA"};
    private final String[] SEVERITY_LABELS = {org.apollo.mobile.view.common.Tr.t("Baixa"), org.apollo.mobile.view.common.Tr.t("Média"), org.apollo.mobile.view.common.Tr.t("Alta"), org.apollo.mobile.view.common.Tr.t("Crítica")};
    private static final String[] TYPE_VALUES = {"DANO_FISICO", "SUJIDADE", "PONTO_QUENTE", "CONEXAO"};
    private final String[] TYPE_LABELS = {org.apollo.mobile.view.common.Tr.t("Dano físico"), org.apollo.mobile.view.common.Tr.t("Sujidade"), org.apollo.mobile.view.common.Tr.t("Ponto quente"), org.apollo.mobile.view.common.Tr.t("Conexão")};

    private PanelsGateway gateway;
    private String barcode;
    private int severityIndex = -1;
    private int typeIndex = -1;
    private EditText description;
    private TextView send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);
        Motion.enterScreen(this);

        barcode = getIntent().getStringExtra(EXTRA_BARCODE);
        String serial = getIntent().getStringExtra(EXTRA_SERIAL);
        if (barcode == null || barcode.isEmpty()) {
            finish();
            return;
        }

        gateway = PanelsGatewayFactory.create(getApplicationContext());
        ((TextView) findViewById(R.id.tvReportTitle)).setText(getString(R.string.report_title, serial));
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        description = findViewById(R.id.etDescription);
        description.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) org.apollo.mobile.tracking.lifecycle.TrackingForm.started(ReportIssueActivity.this);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        send = findViewById(R.id.btnSend);

        fillGroup(findViewById(R.id.cgSeverity), SEVERITY_LABELS, index -> severityIndex = index);
        fillGroup(findViewById(R.id.cgType), TYPE_LABELS, index -> typeIndex = index);

        send.setOnClickListener(view -> submit());
        PanelsUi.bindNavigation(this);
    }

    private interface Selection {
        void onSelected(int index);
    }

    private void fillGroup(ChipGroup group, String[] labels, Selection selection) {
        TextView[] views = new TextView[labels.length];
        for (int index = 0; index < labels.length; index++) {
            TextView chip = new TextView(this);
            chip.setText(labels[index]);
            chip.setBackgroundResource(R.drawable.bg_choice_chip);
            chip.setTextAppearance(R.style.apollo_label3);
            chip.setTextColor(getColorStateList(R.color.selector_choice_text));
            chip.setIncludeFontPadding(false);
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setMinWidth(getResources().getDimensionPixelSize(R.dimen.space_110));
            chip.setMinHeight(getResources().getDimensionPixelSize(R.dimen.space_39));
            chip.setPadding(dp(16), 0, dp(16), 0);
            chip.setClickable(true);
            chip.setFocusable(true);
            final int selected = index;
            views[index] = chip;
            chip.setOnClickListener(view -> {
                for (int other = 0; other < views.length; other++) {
                    views[other].setSelected(other == selected);
                }
                selection.onSelected(selected);
            });
            group.addView(chip);
        }
    }

    private void submit() {
        org.apollo.mobile.tracking.lifecycle.TrackingForm.submitted(this);
        String message = description.getText().toString().trim();
        if (severityIndex < 0) {
            org.apollo.mobile.tracking.lifecycle.TrackingForm.invalid(this, "severity", "required");
            org.apollo.mobile.view.common.AppToast.info(this, R.string.report_pick_severity);
            return;
        }
        if (typeIndex < 0) {
            org.apollo.mobile.tracking.lifecycle.TrackingForm.invalid(this, "type", "required");
            org.apollo.mobile.view.common.AppToast.info(this, R.string.report_pick_type);
            return;
        }
        if (message.isEmpty()) {
            org.apollo.mobile.tracking.lifecycle.TrackingForm.invalid(this, "description", "required");
            org.apollo.mobile.view.common.AppToast.info(this, R.string.report_write_description);
            return;
        }
        setSending(true);
        PanelIssueBody body = new PanelIssueBody(barcode, TYPE_VALUES[typeIndex], SEVERITY_VALUES[severityIndex], message);
        gateway.reportIssue(body, new PanelsGateway.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                org.apollo.mobile.tracking.lifecycle.TrackingForm.succeeded(ReportIssueActivity.this);
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                org.apollo.mobile.view.common.AppToast.success(ReportIssueActivity.this, getString(R.string.report_sent));
                send.postDelayed(ReportIssueActivity.this::finish, 1400);
            }

            @Override
            public void onFailure(PanelsError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setSending(false);
                PanelsUi.handleFailure(ReportIssueActivity.this, error);
            }
        });
    }

    private void setSending(boolean sending) {
        send.setEnabled(!sending);
        send.setText(sending ? R.string.report_sending : R.string.report_send);
        send.setAlpha(sending ? 0.6f : 1f);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
