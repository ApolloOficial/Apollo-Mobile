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
import org.apollo.mobile.panels.error.PanelsError;
import org.apollo.mobile.panels.gateway.PanelItem;
import org.apollo.mobile.panels.gateway.PanelStringItem;
import org.apollo.mobile.panels.gateway.PanelsGateway;
import org.apollo.mobile.panels.gateway.PanelsGatewayFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class PanelsActivity extends AppCompatActivity {
    private PanelsGateway gateway;
    private InverterAdapter adapter;
    private final List<PanelStringItem> allStrings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panels);
        Motion.enterScreen(this);

        gateway = PanelsGatewayFactory.create(getApplicationContext());
        PanelsUi.bindNavigation(this);

        RecyclerView list = findViewById(R.id.rvInverters);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InverterAdapter(new InverterAdapter.Listener() {
            @Override
            public void onExpand(PanelStringItem item) {
                loadPreview(item);
            }

            @Override
            public void onSeeAll(PanelStringItem item) {
                Intent intent = new Intent(PanelsActivity.this, PanelDetailsActivity.class);
                intent.putExtra(PanelDetailsActivity.EXTRA_STRING_ID, item.getId());
                intent.putExtra(PanelDetailsActivity.EXTRA_STRING_CODE, item.getCode());
                intent.putExtra(PanelDetailsActivity.EXTRA_NOMINAL_POWER, item.getNominalPowerKwp());
                startActivity(intent);
            }
        });
        list.setAdapter(adapter);

        ((EditText) findViewById(R.id.etSearch)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStrings();
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

    private void loadStrings() {
        gateway.strings(new PanelsGateway.Callback<List<PanelStringItem>>() {
            @Override
            public void onSuccess(List<PanelStringItem> result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                allStrings.clear();
                allStrings.addAll(result);
                String query = ((EditText) findViewById(R.id.etSearch)).getText().toString();
                applySearch(query);
            }

            @Override
            public void onFailure(PanelsError error) {
                if (isFinishing() || isDestroyed()) return;
                if (error.getType() == PanelsError.Type.UNAUTHORIZED) {
                    PanelsUi.handleFailure(PanelsActivity.this, error);
                    return;
                }
                if (error.getType() == PanelsError.Type.NETWORK) {
                    org.apollo.mobile.view.common.AppToast.offline(PanelsActivity.this);
                }
                org.apollo.mobile.view.common.EmptyState state =
                        new org.apollo.mobile.view.common.EmptyState(findViewById(R.id.llPanelsEmpty))
                                .onRetry(PanelsActivity.this::loadStrings);
                if (error.getType() == PanelsError.Type.TIMEOUT || error.getType() == PanelsError.Type.NETWORK) {
                    state.timeout();
                } else {
                    state.error();
                }
            }
        });
    }

    private void applySearch(String query) {
        String needle = query.trim().toLowerCase(Locale.ROOT);
        List<PanelStringItem> filtered = new ArrayList<>();
        for (PanelStringItem item : allStrings) {
            if (needle.isEmpty()
                    || item.getCode().toLowerCase(Locale.ROOT).contains(needle)
                    || item.getInverterCode().toLowerCase(Locale.ROOT).contains(needle)) {
                filtered.add(item);
            }
        }
        filtered.sort(Comparator.comparing(PanelStringItem::getInverterCode)
                .thenComparing(PanelStringItem::getCode));
        adapter.submit(filtered);
        org.apollo.mobile.view.common.EmptyState empty =
                new org.apollo.mobile.view.common.EmptyState(findViewById(R.id.llPanelsEmpty));
        if (filtered.isEmpty()) {
            empty.empty(org.apollo.mobile.view.common.EmptyState.Kind.STRINGS, null);
        } else {
            empty.setVisibility(android.view.View.GONE);
        }
    }

    private void loadPreview(PanelStringItem item) {
        gateway.panels(item.getId(), new PanelsGateway.Callback<List<PanelItem>>() {
            @Override
            public void onSuccess(List<PanelItem> result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                adapter.setPreview(item.getId(), result);
            }

            @Override
            public void onFailure(PanelsError error) {
                PanelsUi.handleFailure(PanelsActivity.this, error);
            }
        });
    }
}
