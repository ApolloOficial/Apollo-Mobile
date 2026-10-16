package org.apollo.mobile.view.panels;

import android.os.Bundle;
import org.apollo.mobile.view.common.Motion;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
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
    private static final String[] SEVERITY_LABELS = {"Baixa", "Média", "Alta", "Crítica"};
    private static final String[] TYPE_VALUES = {"DANO_FISICO", "SUJIDADE", "PONTO_QUENTE", "CONEXAO"};
    private static final String[] TYPE_LABELS = {"Dano físico", "Sujidade", "Ponto quente", "Conexão"};

    private PanelsGateway gateway;
    private String barcode;
    private int severityIndex = -1;
    private int typeIndex = -1;
    private EditText description;
    private MaterialButton send;

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
        send = findViewById(R.id.btnSend);

        fillGroup(findViewById(R.id.cgSeverity), SEVERITY_LABELS, index -> severityIndex = index);
        fillGroup(findViewById(R.id.cgType), TYPE_LABELS, index -> typeIndex = index);

        send.setOnClickListener(view -> submit());
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
            chip.setTextColor(getColorStateList(R.color.selector_choice_text));
            chip.setPadding(dp(20), dp(8), dp(20), dp(8));
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
        String message = description.getText().toString().trim();
        if (severityIndex < 0) {
            Toast.makeText(this, R.string.report_pick_severity, Toast.LENGTH_SHORT).show();
            return;
        }
        if (typeIndex < 0) {
            Toast.makeText(this, R.string.report_pick_type, Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.isEmpty()) {
            Toast.makeText(this, R.string.report_write_description, Toast.LENGTH_SHORT).show();
            return;
        }
        setSending(true);
        PanelIssueBody body = new PanelIssueBody(barcode, TYPE_VALUES[typeIndex], SEVERITY_VALUES[severityIndex], message);
        gateway.reportIssue(body, new PanelsGateway.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Toast.makeText(ReportIssueActivity.this, R.string.report_sent, Toast.LENGTH_LONG).show();
                finish();
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
