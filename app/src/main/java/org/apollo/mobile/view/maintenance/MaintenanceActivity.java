package org.apollo.mobile.view.maintenance;

import android.content.Intent;
import org.apollo.mobile.view.common.Motion;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.maintenance.gateway.ServiceOrderError;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGateway;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGatewayFactory;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.MaintenanceType;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order;
import org.apollo.mobile.view.common.BottomNavigation;
import org.apollo.mobile.view.maintenance.os.OsDetailsActivity;
import org.apollo.mobile.view.maintenance.os.ServiceOrdersActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MaintenanceActivity extends AppCompatActivity {
    private ServiceOrderGateway gateway;
    private LinearLayout historyList;
    private TextView emptyText;
    private View loading;
    private TextView summaryText;
    private TextView chipAll;
    private TextView chipPreventive;
    private TextView chipCorrective;
    private TextView chipPredictive;
    private final Map<String, Long> maintenanceTypeIds = new HashMap<>();
    private Long selectedTypeId;
    private String selectedTypeKey;
    private int loadVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);
        Motion.enterScreen(this);

        BottomNavigation.setup(this, BottomNavigation.Tab.TOOLS);
        gateway = ServiceOrderGatewayFactory.create(getApplicationContext());
        historyList = findViewById(R.id.maintenanceHistoryList);
        emptyText = findViewById(R.id.tvMaintenanceEmpty);
        loading = findViewById(R.id.progressMaintenanceHistory);
        summaryText = findViewById(R.id.tvOsSummary);
        chipAll = findViewById(R.id.chipMaintenanceAll);
        chipPreventive = findViewById(R.id.chipMaintenancePreventive);
        chipCorrective = findViewById(R.id.chipMaintenanceCorrective);
        chipPredictive = findViewById(R.id.chipMaintenancePredictive);

        chipAll.setOnClickListener(view -> selectType(null));
        chipPreventive.setOnClickListener(view -> selectType("preventiva"));
        chipCorrective.setOnClickListener(view -> selectType("corretiva"));
        chipPredictive.setOnClickListener(view -> selectType("preditiva"));
        setTypeFiltersEnabled(false);
        paintFilters();
        loadMaintenanceTypes();

        findViewById(R.id.btnVerOrdens).setOnClickListener(view ->
                startActivity(new Intent(this, ServiceOrdersActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        int requestVersion = ++loadVersion;
        loading.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        gateway.listByType(selectedTypeId, new ServiceOrderGateway.Callback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                if (requestVersion != loadVersion) return;
                loading.setVisibility(View.GONE);
                renderOrders(orders);
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                if (requestVersion != loadVersion) return;
                loading.setVisibility(View.GONE);
                historyList.removeAllViews();
                emptyText.setText("Não foi possível carregar as ordens de serviço.");
                emptyText.setVisibility(View.VISIBLE);
                summaryText.setText("Ordens indisponíveis");
            }
        });
    }

    private void loadMaintenanceTypes() {
        gateway.maintenanceTypes(new ServiceOrderGateway.Callback<List<MaintenanceType>>() {
            @Override
            public void onSuccess(List<MaintenanceType> types) {
                maintenanceTypeIds.clear();
                for (MaintenanceType type : types) {
                    maintenanceTypeIds.put(normalize(type.getName()), type.getId());
                }
                setTypeFiltersEnabled(true);
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                maintenanceTypeIds.clear();
                setTypeFiltersEnabled(false);
            }
        });
    }

    private void selectType(String key) {
        if (key == null) {
            selectedTypeKey = null;
            selectedTypeId = null;
            paintFilters();
            loadOrders();
            return;
        }

        Long typeId = findTypeId(key);
        if (typeId == null) {
            emptyText.setText("Não foi possível localizar esse tipo de manutenção.");
            emptyText.setVisibility(View.VISIBLE);
            loadMaintenanceTypes();
            return;
        }

        selectedTypeKey = key;
        selectedTypeId = typeId;
        paintFilters();
        loadOrders();
    }

    private Long findTypeId(String key) {
        for (Map.Entry<String, Long> entry : maintenanceTypeIds.entrySet()) {
            if (entry.getKey().contains(key)) return entry.getValue();
        }
        return null;
    }

    private void paintFilters() {
        paintFilter(chipAll, selectedTypeKey == null);
        paintFilter(chipPreventive, "preventiva".equals(selectedTypeKey));
        paintFilter(chipCorrective, "corretiva".equals(selectedTypeKey));
        paintFilter(chipPredictive, "preditiva".equals(selectedTypeKey));
    }

    private void paintFilter(TextView chip, boolean selected) {
        chip.setBackgroundTintList(ColorStateList.valueOf(selected
                ? getColor(R.color.secondary_three)
                : 0xFFEBEBEB));
        chip.setTextColor(getColor(selected ? R.color.neutral_white : R.color.neutral_gray));
    }

    private void setTypeFiltersEnabled(boolean enabled) {
        chipPreventive.setEnabled(enabled);
        chipCorrective.setEnabled(enabled);
        chipPredictive.setEnabled(enabled);
        float alpha = enabled ? 1f : 0.55f;
        chipPreventive.setAlpha(alpha);
        chipCorrective.setAlpha(alpha);
        chipPredictive.setAlpha(alpha);
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private void renderOrders(List<Order> orders) {
        historyList.removeAllViews();

        int open = 0;
        int inProgress = 0;
        for (Order order : orders) {
            if ("ABERTA".equals(order.getMaintenanceStatus())) open++;
            if ("EM_ANDAMENTO".equals(order.getMaintenanceStatus())) inProgress++;
        }
        if (selectedTypeId == null) {
            summaryText.setText(open + " em aberto · " + inProgress + " em andamento");
        }

        if (orders.isEmpty()) {
            emptyText.setText("Nenhuma OS encontrada.");
            emptyText.setVisibility(View.VISIBLE);
            return;
        }

        emptyText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Order order : orders) {
            View card = inflater.inflate(R.layout.item_maintenance_os, historyList, false);
            ((TextView) card.findViewById(R.id.tvSubtitle)).setText(assetLabel(order));
            ((TextView) card.findViewById(R.id.tvMainId)).setText("OS" + order.getId());
            TextView tag = card.findViewById(R.id.tvTag);
            tag.setText(typeLabel(order));
            tag.setBackgroundTintList(ColorStateList.valueOf(statusColor(order)));
            ((TextView) card.findViewById(R.id.tvDate)).setText(formatDate(order.getOpeningDt()));
            card.findViewById(R.id.btnDetails).setOnClickListener(view -> openDetails(order.getId()));
            card.setOnClickListener(view -> openDetails(order.getId()));
            historyList.addView(card);
            bindTarget(order, card.findViewById(R.id.tvSubtitle));
        }
    }

    private void bindTarget(Order order, TextView target) {
        if (order.getWarningId() == null) return;
        gateway.targetLabel(order.getWarningId(), new ServiceOrderGateway.Callback<String>() {
            @Override
            public void onSuccess(String value) {
                target.setText(value);
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                target.setText(assetLabel(order));
            }
        });
    }

    private String assetLabel(Order order) {
        String type = order.getMaintenanceTypeName();
        return type == null || type.trim().isEmpty()
                ? "Ordem de serviço"
                : type.trim();
    }

    private String typeLabel(Order order) {
        String status = order.getMaintenanceStatus();
        if ("ABERTA".equals(status)) return "Aberta";
        if ("EM_ANDAMENTO".equals(status)) return "Em andamento";
        if ("CONCLUIDA".equals(status)) return "Concluída";
        if ("CANCELADA".equals(status)) return "Cancelada";
        return "OS";
    }

    private int statusColor(Order order) {
        String status = order.getMaintenanceStatus();
        if ("ABERTA".equals(status)) return getColor(R.color.info_two);
        if ("EM_ANDAMENTO".equals(status)) return getColor(R.color.primary_two);
        if ("CONCLUIDA".equals(status)) return getColor(R.color.sucess_two);
        return getColor(R.color.disable_gray);
    }

    private String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) return "-";
        try {
            return LocalDateTime.parse(value)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException ignored) {
            return value.length() >= 10 ? value.substring(0, 10) : value;
        }
    }

    private void openDetails(long id) {
        Intent intent = new Intent(this, OsDetailsActivity.class);
        intent.putExtra(OsDetailsActivity.EXTRA_ORDER_ID, id);
        startActivity(intent);
    }
}
