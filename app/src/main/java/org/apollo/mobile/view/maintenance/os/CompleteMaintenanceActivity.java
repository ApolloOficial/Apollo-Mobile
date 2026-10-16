package org.apollo.mobile.view.maintenance.os;

import android.os.Bundle;
import org.apollo.mobile.view.common.Motion;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.maintenance.gateway.ServiceOrderError;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGateway;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGatewayFactory;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Details;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order;
import org.apollo.mobile.view.common.BottomNavigation;

public final class CompleteMaintenanceActivity extends AppCompatActivity {
    private ServiceOrderGateway gateway;
    private long orderId;
    private EditText report;
    private Button finishButton;

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
        report = findViewById(R.id.etReport);
        finishButton = findViewById(R.id.btnFinishMaintenance);

        ((TextView) findViewById(R.id.tvTitle)).setText("Concluir OS" + orderId);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        finishButton.setOnClickListener(view -> complete());

        loadSummary();
    }

    private void loadSummary() {
        gateway.details(orderId, new ServiceOrderGateway.Callback<Details>() {
            @Override
            public void onSuccess(Details value) {
                int total = Math.max(value.getPanels().size(), 1);
                ((TextView) findViewById(R.id.tvVerifiedTag)).setText(
                        total == 1 ? "1 placa" : total + " placas");
                // Sem endpoint de verificação por placa ainda: não há contagem de problemas.
                findViewById(R.id.tvProblemTag).setVisibility(View.GONE);
                findViewById(R.id.llProblemPlates).setVisibility(View.GONE);
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                Toast.makeText(CompleteMaintenanceActivity.this,
                        "Não foi possível carregar o resumo.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void complete() {
        String text = report.getText().toString().trim();
        if (text.isEmpty()) {
            report.setError("Informe o laudo da manutenção.");
            report.requestFocus();
            return;
        }

        finishButton.setEnabled(false);
        gateway.complete(orderId, text, new ServiceOrderGateway.Callback<Order>() {
            @Override
            public void onSuccess(Order value) {
                Toast.makeText(CompleteMaintenanceActivity.this,
                        "OS concluída com sucesso.", Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                finishButton.setEnabled(true);
                Toast.makeText(CompleteMaintenanceActivity.this,
                        "Não foi possível concluir a OS.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
