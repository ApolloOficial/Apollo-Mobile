package org.apollo.mobile.view.panels;

import android.content.Intent;
import org.apollo.mobile.view.common.Motion;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apollo.mobile.R;
import org.apollo.mobile.home.error.HomeError;
import org.apollo.mobile.home.gateway.HomeGateway;
import org.apollo.mobile.home.gateway.HomeGatewayFactory;
import org.apollo.mobile.home.gateway.HomeResponse;
import org.apollo.mobile.panels.PanelFilter;
import org.apollo.mobile.panels.error.PanelsError;
import org.apollo.mobile.panels.gateway.PanelItem;
import org.apollo.mobile.panels.gateway.PanelsGateway;
import org.apollo.mobile.panels.gateway.PanelsGatewayFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PanelDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_STRING_ID = "string_id";
    public static final String EXTRA_STRING_CODE = "string_code";
    public static final String EXTRA_NOMINAL_POWER = "nominal_power";
    public static final String EXTRA_QUERY = "query";

    private PanelsGateway gateway;
    private PlateAdapter adapter;
    private String stringId;
    private String stringCode;
    private double nominalPower;
    private final List<PanelItem> allPanels = new ArrayList<>();
    private PanelFilter filter = PanelFilter.ALL;
    private TextView[] chips;
    private PanelFilter[] filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_details);
        Motion.enterScreen(this);

        stringId = getIntent().getStringExtra(EXTRA_STRING_ID);
        stringCode = getIntent().getStringExtra(EXTRA_STRING_CODE);
        nominalPower = getIntent().getDoubleExtra(EXTRA_NOMINAL_POWER, 0d);
        if (stringId == null) {
            finish();
            return;
        }

        gateway = PanelsGatewayFactory.create(getApplicationContext());
        PanelsUi.bindNavigation(this);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        ((TextView) findViewById(R.id.tvInverterIdDetail)).setText(stringCode);
        updateSummary(0);

        RecyclerView list = findViewById(R.id.rvPlatesDetail);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlateAdapter(panel -> PanelSheet.show(this, panel, stringCode));
        list.setAdapter(adapter);

        chips = new TextView[]{
                findViewById(R.id.chipAll),
                findViewById(R.id.chipStock),
                findViewById(R.id.chipInUse),
                findViewById(R.id.chipAttention)
        };
        filters = new PanelFilter[]{
                PanelFilter.ALL,
                PanelFilter.STOCK,
                PanelFilter.IN_USE,
                PanelFilter.ATTENTION
        };
        for (int index = 0; index < chips.length; index++) {
            final PanelFilter selected = filters[index];
            chips[index].setOnClickListener(view -> select(selected));
        }

        ((EditText) findViewById(R.id.etSearch)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                render();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        String query = getIntent().getStringExtra(EXTRA_QUERY);
        if (query != null) ((EditText) findViewById(R.id.etSearch)).setText(query);
        select(PanelFilter.ALL);
        loadLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stringId != null) {
            loadPanels();
        }
    }

    private void select(PanelFilter selected) {
        filter = selected;
        for (int index = 0; index < chips.length; index++) {
            chips[index].setSelected(filters[index] == selected);
        }
        render();
    }

    private void render() {
        String query = ((EditText) findViewById(R.id.etSearch)).getText().toString();
        adapter.submit(filter.apply(allPanels, query));
    }

    private void updateSummary(int count) {
        String power = String.format(Locale.US, "%.0f", nominalPower);
        ((TextView) findViewById(R.id.tvInverterSummaryDetail))
                .setText(getString(R.string.panels_summary, power, count));
    }

    private void updateHealth(List<PanelItem> panels) {
        TextView health = findViewById(R.id.tvHealthDetail);
        for (PanelItem panel : panels) {
            String percent = PanelsUi.percent(panel.getStringHealthScore());
            if (percent != null) {
                health.setText(getString(R.string.panels_string_health, percent));
                if (health.getVisibility() != android.view.View.VISIBLE) {
                    health.setVisibility(android.view.View.VISIBLE);
                    Motion.refresh(health);
                }
                return;
            }
        }
    }

    private void loadPanels() {
        gateway.panels(stringId, new PanelsGateway.Callback<List<PanelItem>>() {
            @Override
            public void onSuccess(List<PanelItem> result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                allPanels.clear();
                allPanels.addAll(result);
                updateSummary(result.size());
                updateHealth(result);
                render();
                ((RecyclerView) findViewById(R.id.rvPlatesDetail)).scheduleLayoutAnimation();
                loadOrders();
            }

            @Override
            public void onFailure(PanelsError error) {
                PanelsUi.handleFailure(PanelDetailsActivity.this, error);
            }
        });
    }

    private void loadOrders() {
        gateway.panelOrders(stringId, new PanelsGateway.Callback<java.util.Map<Long, Long>>() {
            @Override
            public void onSuccess(java.util.Map<Long, Long> result) {
                if (isFinishing() || isDestroyed()) return;
                adapter.setOrders(result);
            }

            @Override
            public void onFailure(PanelsError error) {
            }
        });
    }

    private void loadLocation() {
        HomeGatewayFactory.create(getApplicationContext()).getHome(new HomeGateway.Callback() {
            @Override
            public void onSuccess(HomeResponse response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                ((TextView) findViewById(R.id.tvLocation)).setText(response.getFormattedLocation());
            }

            @Override
            public void onFailure(HomeError error) {
            }
        });
    }

    static Intent reportIntent(android.content.Context context, PanelItem panel) {
        Intent intent = new Intent(context, ReportIssueActivity.class);
        intent.putExtra(ReportIssueActivity.EXTRA_BARCODE, panel.getBarcode());
        intent.putExtra(ReportIssueActivity.EXTRA_SERIAL, panel.getSerialNumber());
        return intent;
    }
}
