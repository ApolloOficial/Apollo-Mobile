package org.apollo.mobile.view.maintenance.os;

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
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order;
import org.apollo.mobile.view.common.BottomNavigation;

import java.util.List;

public final class ServiceOrdersActivity extends AppCompatActivity {
    private ServiceOrderGateway gateway;
    private LinearLayout list;
    private org.apollo.mobile.view.common.EmptyState empty;
    private View loading;
    private TextView chipAll;
    private TextView chipOpen;
    private TextView chipInProgress;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_orders);
        Motion.enterScreen(this);
        BottomNavigation.setup(this, BottomNavigation.Tab.TOOLS);

        gateway = ServiceOrderGatewayFactory.create(getApplicationContext());
        list = findViewById(R.id.service_orders_list_container);
        empty = new org.apollo.mobile.view.common.EmptyState(findViewById(R.id.llEmptyOrders)).onRetry(this::load);
        loading = findViewById(R.id.progressOrders);
        chipAll = findViewById(R.id.chipAll);
        chipOpen = findViewById(R.id.chipOpen);
        chipInProgress = findViewById(R.id.chipInProgress);

        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        chipAll.setOnClickListener(view -> select(null));
        chipOpen.setOnClickListener(view -> select("ABERTA"));
        chipInProgress.setOnClickListener(view -> select("EM_ANDAMENTO"));

        paintChips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        load(); // volta da conclusão com a lista atualizada
    }

    private void select(String selectedStatus) {
        status = selectedStatus;
        paintChips();
        load();
    }

    private void paintChips() {
        paint(chipAll, status == null);
        paint(chipOpen, "ABERTA".equals(status));
        paint(chipInProgress, "EM_ANDAMENTO".equals(status));
    }

    private void paint(TextView chip, boolean selected) {
        chip.setSelected(selected);
    }

    private void load() {
        loading.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
        gateway.list(status, new ServiceOrderGateway.Callback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                loading.setVisibility(View.GONE);
                render(orders);
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                loading.setVisibility(View.GONE);
                list.removeAllViews();
                if (error.getType() == ServiceOrderError.Type.NETWORK) {
                    org.apollo.mobile.view.common.AppToast.offline(ServiceOrdersActivity.this);
                    empty.timeout();
                } else {
                    empty.error();
                }
            }
        });
    }

    private void render(List<Order> orders) {
        list.removeAllViews();
        if (orders.isEmpty()) {
            empty.empty(org.apollo.mobile.view.common.EmptyState.Kind.ORDERS, null);
        } else {
            empty.setVisibility(View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Order order : orders) {
            View card = inflater.inflate(R.layout.item_service_order, list, false);
            ((TextView) card.findViewById(R.id.tvSubtitle)).setText(
                    org.apollo.mobile.view.common.Tr.v(ServiceOrderUi.safe(order.getMaintenanceTypeName())));
            ((TextView) card.findViewById(R.id.tvMainId)).setText("OS" + order.getId());
            OsDetailsActivity.bindStatusTag(card.findViewById(R.id.tvStatusTag), order.getMaintenanceStatus());
            OsDetailsActivity.bindPriorityTag(card.findViewById(R.id.tvPriorityTag), order.getPriority());
            ((TextView) card.findViewById(R.id.tvDueDate)).setText(ServiceOrderUi.boldLabel(this,
                    "Prazo: " + ServiceOrderUi.date(order.getDueDate())));
            Motion.fadeUp(card, Math.min(list.getChildCount(), 8) * 50L);
            Motion.pressable(card.findViewById(R.id.btnDetails));
            card.findViewById(R.id.btnDetails).setOnClickListener(view -> open(order.getId()));
            card.setOnClickListener(view -> open(order.getId()));
            list.addView(card);
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
                target.setText(org.apollo.mobile.view.common.Tr.v(ServiceOrderUi.safe(order.getMaintenanceTypeName())));
            }
        });
    }

    private void open(long id) {
        Intent intent = new Intent(this, OsDetailsActivity.class);
        intent.putExtra(OsDetailsActivity.EXTRA_ORDER_ID, id);
        startActivity(intent);
    }
}
