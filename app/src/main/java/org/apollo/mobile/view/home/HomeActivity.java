package org.apollo.mobile.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.apollo.mobile.R;
import org.apollo.mobile.home.error.HomeError;
import org.apollo.mobile.home.gateway.HomeAttentionItem;
import org.apollo.mobile.home.gateway.HomeGateway;
import org.apollo.mobile.home.gateway.HomeGatewayFactory;
import org.apollo.mobile.home.gateway.HomePendingRegistration;
import org.apollo.mobile.home.gateway.HomeResponse;
import org.apollo.mobile.home.gateway.HomeServiceOrder;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.sync.PendingSyncFactory;
import org.apollo.mobile.sync.PendingSyncRunner;
import org.apollo.mobile.sync.SyncReport;
import org.apollo.mobile.view.auth.LoginActivity;
import org.apollo.mobile.view.common.BottomNavigation;
import org.apollo.mobile.view.common.Motion;
import org.apollo.mobile.view.maintenance.os.OsDetailsActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

public final class HomeActivity extends AppCompatActivity {

    private HomeGateway homeGateway;

    private TextView greetingText;
    private TextView serviceOrderCountText;
    private TextView attentionCountText;

    private View serviceOrderCard;
    private View serviceOrderDetailsButton;
    private TextView assetCodeText;
    private TextView serviceOrderCodeText;
    private TextView serviceOrderIdText;
    private TextView dueDateText;
    private TextView statusText;
    private TextView priorityText;
    private View previousButton;
    private View nextButton;
    private View paginationContainer;
    private LinearLayout indicatorsContainer;
    private TextView serviceOrderEmptyText;

    private View attentionCard;
    private TextView attentionSeverityText;
    private TextView attentionTitleText;
    private TextView attentionMetricLabelText;
    private TextView attentionPercentageText;
    private LinearProgressIndicator attentionProgress;
    private TextView attentionEmptyText;

    private View pendingCard;
    private TextView pendingBatchText;
    private TextView pendingTitleText;
    private TextView pendingCountText;
    private LinearProgressIndicator pendingProgress;
    private TextView pendingEmptyText;

    private ImageView syncButton;
    private PendingSyncRunner syncRunner;

    private List<HomeServiceOrder> serviceOrders =
            Collections.emptyList();

    private int currentServiceOrderIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigation.setup(this, BottomNavigation.Tab.HOME);

        bindViews();
        bindExtraActions();
        Motion.enterScreen(this);

        homeGateway = HomeGatewayFactory.create(
                getApplicationContext()
        );
        syncRunner = PendingSyncFactory.runner(
                getApplicationContext()
        );

        previousButton.setOnClickListener(
                view -> Motion.slideSwap(serviceOrderCard, false, this::showPreviousServiceOrder)
        );

        nextButton.setOnClickListener(
                view -> Motion.slideSwap(serviceOrderCard, true, this::showNextServiceOrder)
        );

        syncButton.setOnClickListener(
                view -> syncNow()
        );

        loadHome();
    }

    private void bindViews() {
        greetingText = findViewById(R.id.tvGreeting);
        serviceOrderCountText =
                findViewById(R.id.tvServiceOrderCount);
        attentionCountText =
                findViewById(R.id.tvAttentionCount);

        serviceOrderCard =
                findViewById(R.id.cardServiceOrder);
        serviceOrderDetailsButton =
                findViewById(R.id.btnOsDetails);
        assetCodeText =
                findViewById(R.id.tvPlacaLabel);
        serviceOrderCodeText =
                findViewById(R.id.tvPlacaValue);
        serviceOrderIdText =
                findViewById(R.id.tvOsId);
        dueDateText =
                findViewById(R.id.tvOsPrazo);
        statusText =
                findViewById(R.id.tvStatusTag);
        priorityText =
                findViewById(R.id.tvPriorityTag);
        previousButton =
                findViewById(R.id.btnPrevOs);
        nextButton =
                findViewById(R.id.btnNextOs);
        paginationContainer =
                findViewById(R.id.llOsPagination);
        indicatorsContainer =
                findViewById(R.id.llOsIndicators);
        serviceOrderEmptyText =
                findViewById(R.id.tvServiceOrderEmpty);

        attentionCard =
                findViewById(R.id.cardAttention);
        attentionSeverityText =
                findViewById(R.id.tvCriticoTag);
        attentionTitleText =
                findViewById(R.id.tvAlertTitle);
        attentionMetricLabelText =
                findViewById(R.id.tvAlertEficLabel);
        attentionPercentageText =
                findViewById(R.id.tvAlertPercentage);
        attentionProgress =
                findViewById(R.id.progressAlert);
        attentionEmptyText =
                findViewById(R.id.tvAttentionEmpty);

        pendingCard =
                findViewById(R.id.cardPending);
        pendingBatchText =
                findViewById(R.id.tvPendingLote);
        pendingTitleText =
                findViewById(R.id.tvPendingTitle);
        pendingCountText =
                findViewById(R.id.tvPendingCount);
        pendingProgress =
                findViewById(R.id.progressPending);
        pendingEmptyText =
                findViewById(R.id.tvPendingEmpty);

        syncButton = findViewById(R.id.ivSync);
    }

    private void loadHome() {
        homeGateway.getHome(new HomeGateway.Callback() {
            @Override
            public void onSuccess(HomeResponse response) {
                bindHome(response);
            }

            @Override
            public void onFailure(HomeError error) {
                handleError(error);
            }
        });
    }

    private void bindHome(HomeResponse response) {
        bindHeader(response);

        serviceOrderCountText.setText(
                getString(
                        R.string.home_view_all_count,
                        response.getTotalServiceOrders()
                )
        );

        attentionCountText.setText(
                getString(
                        R.string.home_view_all_count,
                        response.getTotalAttentionItems()
                )
        );

        serviceOrders = response.getServiceOrders();
        currentServiceOrderIndex = 0;

        bindCurrentServiceOrder();
        bindAttention(response.getAttentionItems());
        bindPending(response.getPendingRegistrations());
    }

    private void bindHeader(HomeResponse response) {
        greetingText.setText(
                getString(
                        greetingResource(),
                        firstName(response.getTechnicianName())
                )
        );
    }

    /** "Vanessa Lima Mendes" -> "Vanessa". */
    private String firstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return getString(R.string.home_default_user);
        }

        return fullName.trim().split("\\s+")[0];
    }

    private int greetingResource() {
        int hour = java.util.Calendar.getInstance()
                .get(java.util.Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            return R.string.home_greeting_morning;
        }

        if (hour < 18) {
            return R.string.home_greeting_afternoon;
        }

        return R.string.home_greeting_evening;
    }

    private void bindCurrentServiceOrder() {
        if (serviceOrders.isEmpty()) {
            serviceOrderCard.setVisibility(View.GONE);
            serviceOrderEmptyText.setVisibility(View.VISIBLE);
            paginationContainer.setVisibility(View.GONE);
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
            indicatorsContainer.removeAllViews();
            return;
        }

        serviceOrderCard.setVisibility(View.VISIBLE);
        serviceOrderEmptyText.setVisibility(View.GONE);
        paginationContainer.setVisibility(View.VISIBLE);

        HomeServiceOrder order =
                serviceOrders.get(currentServiceOrderIndex);

        assetCodeText.setText(
                getString(
                        R.string.home_asset_code,
                        safeText(order.getAssetCode())
                )
        );

        serviceOrderCodeText.setText(
                safeText(order.getCode())
        );

        serviceOrderIdText.setText(
                getString(
                        R.string.home_service_order_id,
                        order.getId()
                )
        );

        String due = getString(R.string.home_due_date, formatDate(order.getDueDate()));
        android.text.SpannableString dueSpan = new android.text.SpannableString(due);
        int colon = due.indexOf(':');
        if (colon > 0) {
            dueSpan.setSpan(new android.text.style.TypefaceSpan(
                    androidx.core.content.res.ResourcesCompat.getFont(this, R.font.manrope_bold)),
                    0, colon + 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        dueDateText.setText(dueSpan);

        statusText.setText(
                formatStatus(order.getStatus())
        );

        priorityText.setText(
                formatPriority(order.getPriority())
        );

        View.OnClickListener openDetails = view -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    OsDetailsActivity.class
            );
            intent.putExtra(
                    OsDetailsActivity.EXTRA_ORDER_ID,
                    order.getId()
            );
            startActivity(intent);
        };

        serviceOrderDetailsButton.setOnClickListener(openDetails);
        serviceOrderCard.setOnClickListener(openDetails);

        previousButton.setEnabled(
                currentServiceOrderIndex > 0
        );

        nextButton.setEnabled(
                currentServiceOrderIndex
                        < serviceOrders.size() - 1
        );

        renderIndicators();
    }

    private void showPreviousServiceOrder() {
        if (currentServiceOrderIndex <= 0) {
            return;
        }

        currentServiceOrderIndex--;
        bindCurrentServiceOrder();
    }

    private void showNextServiceOrder() {
        if (currentServiceOrderIndex
                >= serviceOrders.size() - 1) {
            return;
        }

        currentServiceOrderIndex++;
        bindCurrentServiceOrder();
    }

    private void renderIndicators() {
        indicatorsContainer.removeAllViews();

        for (int index = 0;
             index < serviceOrders.size();
             index++) {

            boolean active =
                    index == currentServiceOrderIndex;

            int size = getResources().getDimensionPixelSize(
                    active ? R.dimen.space_12 : R.dimen.space_8);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(size, size);

            int gap = getResources().getDimensionPixelSize(R.dimen.space_4);
            params.setMargins(gap, 0, gap, 0);

            View indicator = new View(this);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(
                    active
                            ? R.drawable.dot_indicator_home_active
                            : R.drawable.dot_indicator_home_inactive
            );

            final int selectedIndex = index;

            indicator.setOnClickListener(view -> {
                currentServiceOrderIndex = selectedIndex;
                bindCurrentServiceOrder();
            });

            indicatorsContainer.addView(indicator);
        }
    }

    private void bindAttention(
            List<HomeAttentionItem> items
    ) {
        if (items == null || items.isEmpty()) {
            attentionCard.setVisibility(View.GONE);
            attentionEmptyText.setVisibility(View.VISIBLE);
            return;
        }

        attentionCard.setVisibility(View.VISIBLE);
        attentionEmptyText.setVisibility(View.GONE);

        HomeAttentionItem item = items.get(0);

        attentionSeverityText.setText(
                formatSeverity(item.getSeverity())
        );

        attentionTitleText.setText(
                safeText(item.getTitle())
        );

        attentionMetricLabelText.setText(
                safeText(item.getMetricLabel())
        );

        int progress = item.getProgressValue();

        attentionPercentageText.setText(
                getString(
                        R.string.home_percentage,
                        progress
                )
        );

        attentionProgress.setProgressCompat(
                progress,
                true
        );
    }

    private void bindPending(
            List<HomePendingRegistration> items
    ) {
        if (items == null || items.isEmpty()) {
            pendingCard.setVisibility(View.GONE);
            pendingEmptyText.setVisibility(View.VISIBLE);
            return;
        }

        pendingCard.setVisibility(View.VISIBLE);
        pendingEmptyText.setVisibility(View.GONE);

        HomePendingRegistration pending =
                items.get(0);

        pendingBatchText.setText(
                getString(
                        R.string.home_batch,
                        safeText(pending.getBatchCode())
                )
        );

        pendingTitleText.setText(
                getResources().getQuantityString(
                        R.plurals.home_pending_panels,
                        pending.getRemaining(),
                        pending.getRemaining()
                )
        );

        pendingCountText.setText(
                getString(
                        R.string.home_pending_count,
                        pending.getRegistered(),
                        pending.getTotal()
                )
        );

        pendingProgress.setProgressCompat(
                pending.getProgressPercentage(),
                true
        );
    }

    private void bindExtraActions() {
        serviceOrderCountText.setPaintFlags(
                serviceOrderCountText.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        serviceOrderCountText.setOnClickListener(v -> startActivity(
                new Intent(this, org.apollo.mobile.view.maintenance.os.ServiceOrdersActivity.class)));
        View attentionButton = findViewById(R.id.btnAttentionDetails);
        attentionButton.setOnClickListener(v -> startActivity(
                new Intent(this, org.apollo.mobile.view.maintenance.os.ServiceOrdersActivity.class)));
        View pendingButton = findViewById(R.id.btnPendingDetails);
        pendingButton.setOnClickListener(v -> startActivity(
                new Intent(this, org.apollo.mobile.view.panels.PanelsActivity.class)));
        Motion.pressable(serviceOrderDetailsButton, attentionButton, pendingButton,
                serviceOrderCard, attentionCard, pendingCard);
    }

    private void syncNow() {
        if (syncRunner.isRunning()) {
            return;
        }

        showSyncIcon();
        syncButton.setEnabled(false);
        Motion.spin(syncButton);

        syncRunner.run(report -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            syncButton.setEnabled(true);
            syncButton.setRotation(0f);

            if (report.getStop()
                    == SyncReport.Stop.UNAUTHORIZED) {
                goToLogin();
                return;
            }

            boolean synchronizedSuccessfully =
                    report.getStop() == SyncReport.Stop.NONE
                            && report.getRejected() == 0
                            && report.getRemaining() == 0;

            if (synchronizedSuccessfully) {
                showSyncSuccessIcon();
                loadHome();
                return;
            }

            showSyncIcon();
        });
    }

    private void showSyncIcon() {
        syncButton.setImageResource(R.drawable.ic_sync);
        syncButton.setContentDescription(
                getString(R.string.home_sync_content_description)
        );
    }

    private void showSyncSuccessIcon() {
        syncButton.setImageResource(R.drawable.ic_check_sync);
        syncButton.setContentDescription(
                getString(
                        R.string.home_sync_success_content_description
                )
        );
    }

    private void goToLogin() {
        SessionManagerFactory.create(
                getApplicationContext()
        ).logout();

        Intent intent = new Intent(
                this,
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void handleError(HomeError error) {
        if (error.getType()
                == HomeError.Type.UNAUTHORIZED) {
            goToLogin();
            return;
        }

        int message;

        switch (error.getType()) {
            case TIMEOUT:
                message = R.string.home_timeout;
                break;

            case NETWORK:
                message = R.string.home_network_error;
                break;

            case SERVER:
                message = R.string.home_server_error;
                break;

            default:
                message = R.string.home_load_error;
                break;
        }

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }

        try {
            LocalDate date = LocalDate.parse(value);

            return date.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );
        } catch (DateTimeParseException exception) {
            return value;
        }
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "-";
        }

        switch (status.trim().toUpperCase()) {
            case "IN_PROGRESS":
                return "Em andamento";

            case "PENDING":
                return "Pendente";

            case "COMPLETED":
                return "Concluída";

            case "CANCELLED":
                return "Cancelada";

            default:
                return status;
        }
    }

    private String formatPriority(String priority) {
        if (priority == null) {
            return "-";
        }

        switch (priority.trim().toUpperCase()) {
            case "LOW":
                return "Baixa";

            case "MEDIUM":
                return "Média";

            case "HIGH":
                return "Alta";

            case "CRITICAL":
                return "Crítica";

            default:
                return priority;
        }
    }

    private String formatSeverity(String severity) {
        if (severity == null) {
            return "Atenção";
        }

        switch (severity.trim().toUpperCase()) {
            case "CRITICAL":
                return "Crítico";

            case "HIGH":
                return "Alto";

            case "MEDIUM":
                return "Médio";

            case "LOW":
                return "Baixo";

            default:
                return severity;
        }
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty()
                ? "-"
                : value.trim();
    }

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
