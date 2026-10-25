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
    private org.apollo.mobile.view.common.EmptyState emptyText;
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
        emptyText = new org.apollo.mobile.view.common.EmptyState(findViewById(R.id.llMaintenanceEmpty)).onRetry(this::loadOrders);
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
                if (error.getType() == ServiceOrderError.Type.NETWORK) {
                    emptyText.timeout();
                } else {
                    emptyText.error();
                }
                summaryText.setText(org.apollo.mobile.view.common.Tr.t("Ordens indisponíveis"));
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
        org.apollo.mobile.tracking.TrackingManager.trackCurrent("filter_changed",
                org.apollo.mobile.tracking.TrackingManager.properties("filter_name", "maintenance_type",
                        "filter_value", key == null ? "all" : key));
        if (key == null) {
            selectedTypeKey = null;
            selectedTypeId = null;
            paintFilters();
            loadOrders();
            return;
        }

        Long typeId = findTypeId(key);
        if (typeId == null) {
            org.apollo.mobile.view.common.AppToast.error(this, org.apollo.mobile.view.common.Tr.t("Não foi possível localizar esse tipo de manutenção."));
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
        chip.setSelected(selected);
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
            summaryText.setText(open + org.apollo.mobile.view.common.Tr.t(" em aberto · ") + inProgress + org.apollo.mobile.view.common.Tr.t(" em andamento"));
        }

        if (orders.isEmpty()) {
            emptyText.empty(org.apollo.mobile.view.common.EmptyState.Kind.ORDERS, null);
            return;
        }

        emptyText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        String lastSection = null;
        int index = 0;
        for (Order order : orders) {
            String section = sectionLabel(order.getOpeningDt());
            if (!section.equals(lastSection)) {
                TextView header = (TextView) inflater.inflate(R.layout.item_history_section, historyList, false);
                header.setText(section);
                historyList.addView(header);
                lastSection = section;
            }
            View card = inflater.inflate(R.layout.item_maintenance_os, historyList, false);
            ((TextView) card.findViewById(R.id.tvSubtitle)).setText(assetLabel(order));
            ((TextView) card.findViewById(R.id.tvMainId)).setText("OS" + order.getId());
            TextView tag = card.findViewById(R.id.tvTag);
            tag.setText(maintenanceTypeLabel(order));
            tag.setBackgroundTintList(ColorStateList.valueOf(typeColor(order)));
            tag.setTextColor(getColor(isCorrective(order) ? R.color.neutral_white : R.color.neutral_black));
            Motion.fadeUp(card, Math.min(index++, 8) * 50L);
            Motion.pressable(card.findViewById(R.id.btnDetails));
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
                ? org.apollo.mobile.view.common.Tr.t("Ordem de serviço")
                : org.apollo.mobile.view.common.Tr.v(type.trim());
    }

    private String typeLabel(Order order) {
        String status = order.getMaintenanceStatus();
        if ("ABERTA".equals(status)) return org.apollo.mobile.view.common.Tr.t("Aberta");
        if ("EM_ANDAMENTO".equals(status)) return org.apollo.mobile.view.common.Tr.t("Em andamento");
        if ("CONCLUIDA".equals(status)) return org.apollo.mobile.view.common.Tr.t("Concluída");
        if ("CANCELADA".equals(status)) return org.apollo.mobile.view.common.Tr.t("Cancelada");
        return "OS";
    }

    private int statusColor(Order order) {
        String status = order.getMaintenanceStatus();
        if ("ABERTA".equals(status)) return getColor(R.color.info_two);
        if ("EM_ANDAMENTO".equals(status)) return getColor(R.color.primary_two);
        if ("CONCLUIDA".equals(status)) return getColor(R.color.sucess_two);
        return getColor(R.color.disable_gray);
    }

    private String sectionLabel(String value) {
        java.time.LocalDate date = parseDate(value);
        if (date == null) return org.apollo.mobile.view.common.Tr.t("Anteriores");
        java.time.LocalDate today = java.time.LocalDate.now();
        if (date.equals(today)) return org.apollo.mobile.view.common.Tr.t("Hoje");
        if (date.equals(today.minusDays(1))) return org.apollo.mobile.view.common.Tr.t("Ontem");
        if (!date.isBefore(today.minusDays(30))) return org.apollo.mobile.view.common.Tr.t("Nos últimos 30 dias");
        return org.apollo.mobile.view.common.Tr.t("Anteriores");
    }

    private java.time.LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(value).toLocalDate();
        } catch (DateTimeParseException ignored) {
            try {
                return java.time.LocalDate.parse(value.length() >= 10 ? value.substring(0, 10) : value);
            } catch (DateTimeParseException ignoredToo) {
                return null;
            }
        }
    }

    private String maintenanceTypeLabel(Order order) {
        String type = normalize(order.getMaintenanceTypeName());
        if (type.contains("prevent")) return org.apollo.mobile.view.common.Tr.t("Preventiva");
        if (type.contains("corret")) return org.apollo.mobile.view.common.Tr.t("Corretiva");
        if (type.contains("predit")) return org.apollo.mobile.view.common.Tr.t("Preditiva");
        return typeLabel(order);
    }

    private boolean isCorrective(Order order) {
        return normalize(order.getMaintenanceTypeName()).contains("corret");
    }

    private int typeColor(Order order) {
        String type = normalize(order.getMaintenanceTypeName());
        if (type.contains("prevent")) return getColor(R.color.alert_two);
        if (type.contains("corret")) return getColor(R.color.error_one);
        if (type.contains("predit")) return getColor(R.color.info_two);
        return statusColor(order);
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
