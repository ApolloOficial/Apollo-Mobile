package org.apollo.mobile.view.maintenance.os;

import android.content.Intent;
import org.apollo.mobile.view.common.Motion;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.apollo.mobile.R;
import org.apollo.mobile.maintenance.OsVerificationStore;
import org.apollo.mobile.view.scanner.ScannerActivity;
import org.apollo.mobile.maintenance.gateway.ServiceOrderError;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGateway;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGatewayFactory;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Details;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Panel;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.StringGroup;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Warning;
import org.apollo.mobile.view.common.BottomNavigation;

public final class OsDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_ORDER_ID = "service_order_id";
    public static final String EXTRA_COMPLETED = "service_order_completed";

    private ServiceOrderGateway gateway;
    private long orderId;
    private Details details;
    private LinearLayout infoRows;
    private LinearLayout stringGroups;
    private LinearLayout history;
    private View verifiedSection;
    private TextView primary;
    private TextView secondary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_os_details);
        Motion.enterScreen(this);

        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1L);
        if (orderId <= 0) {
            finish();
            return;
        }

        BottomNavigation.setup(this, BottomNavigation.Tab.TOOLS);
        gateway = ServiceOrderGatewayFactory.create(getApplicationContext());
        infoRows = findViewById(R.id.llInfoRows);
        stringGroups = findViewById(R.id.llStringGroups);
        history = findViewById(R.id.llHistory);
        verifiedSection = findViewById(R.id.llVerifiedSection);
        primary = findViewById(R.id.btnPrimary);
        secondary = findViewById(R.id.btnSecondary);

        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        showCompletedToast(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showCompletedToast(intent);
    }

    private void showCompletedToast(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_COMPLETED, false)) {
            intent.removeExtra(EXTRA_COMPLETED);
            primary.postDelayed(() -> org.apollo.mobile.view.common.AppToast.success(this,
                    "Manutenção registrada com sucesso!"), 400);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gateway != null) load(); // volta da conclusão já como "Concluída"
    }

    private void load() {
        gateway.details(orderId, new ServiceOrderGateway.Callback<Details>() {
            @Override
            public void onSuccess(Details value) {
                if (isFinishing() || isDestroyed()) return;
                details = value;
                findViewById(R.id.progressOsDetails).setVisibility(View.GONE);
                findViewById(R.id.osEmptyState).setVisibility(View.GONE);
                View content = findViewById(R.id.llOsContent);
                if (content.getVisibility() != View.VISIBLE) {
                    content.setVisibility(View.VISIBLE);
                    Motion.fadeUp(content, 0);
                }
                render();
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                if (isFinishing() || isDestroyed()) return;
                findViewById(R.id.progressOsDetails).setVisibility(View.GONE);
                if (details != null) {
                    org.apollo.mobile.view.common.AppToast.error(OsDetailsActivity.this, "Não foi possível atualizar a OS.");
                    return;
                }
                findViewById(R.id.llOsContent).setVisibility(View.GONE);
                boolean missing = error.getType() == ServiceOrderError.Type.NOT_FOUND
                        || error.getType() == ServiceOrderError.Type.UNAUTHORIZED;
                org.apollo.mobile.view.common.EmptyState state =
                        new org.apollo.mobile.view.common.EmptyState(findViewById(R.id.osEmptyState)).onRetry(() -> {
                            findViewById(R.id.osEmptyState).setVisibility(View.GONE);
                            findViewById(R.id.progressOsDetails).setVisibility(View.VISIBLE);
                            load();
                        });
                if (missing) {
                    state.empty(org.apollo.mobile.view.common.EmptyState.Kind.ORDERS,
                            "Essa ordem de serviço não está mais disponível para você ou foi removida.");
                } else if (error.getType() == ServiceOrderError.Type.NETWORK) {
                    state.timeout();
                } else {
                    state.error();
                }
            }
        });
    }

    private void render() {
        Order order = details.getOrder();
        ((TextView) findViewById(R.id.tvOsId)).setText("OS" + order.getId());
        bindStatusTag(findViewById(R.id.tvStatusTag), order.getMaintenanceStatus());
        bindPriorityTag(findViewById(R.id.tvPriorityTag), order.getPriority());
        ((TextView) findViewById(R.id.tvDescription)).setText(
                ServiceOrderUi.safe(details.getWarning().getMessage()));

        renderInformation();
        renderGroups();
        renderHistory();
        renderSummary();
        configureActions();
        openPendingCondition();
    }

    private void renderInformation() {
        infoRows.removeAllViews();
        Warning warning = details.getWarning();
        Order order = details.getOrder();

        switch (details.getScope()) {
            case PANEL:
                addInfo("Inversor / String", join(warning.getInverterCode(), warning.getStringCode()));
                addInfo("Número de série", warning.getPanelSerial());
                break;
            case STRING:
                addInfo("Inversor / String", join(warning.getInverterCode(), warning.getStringCode()));
                break;
            default:
                addInfo("Inversor", ServiceOrderUi.safe(warning.getInverterCode()) + ", todas as strings");
                break;
        }

        addInfo("Tipo de manutenção", order.getMaintenanceTypeName());
        if ("CONCLUIDA".equals(order.getMaintenanceStatus())) {
            addInfo("Prazo de conclusão", "Concluída em\n" + ServiceOrderUi.date(order.getConcludedAt()));
        } else {
            addInfo("Prazo de conclusão", ServiceOrderUi.date(order.getDueDate()));
        }
    }

    private void addInfo(String label, String value) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_info_row, infoRows, false);
        ((TextView) row.findViewById(R.id.tvLabel)).setText(label);
        ((TextView) row.findViewById(R.id.tvValue)).setText(ServiceOrderUi.safe(value));
        infoRows.addView(row);
    }

    private void renderGroups() {
        java.util.List<Panel> targets = details.getTargetPanels();
        boolean show = !targets.isEmpty();
        verifiedSection.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) return;

        java.util.Map<Long, OsVerificationStore.Result> results = new OsVerificationStore(this).results(orderId);
        boolean concluded = "CONCLUIDA".equals(details.getOrder().getMaintenanceStatus());
        int verified = 0;
        for (Panel panel : targets) {
            if (concluded || results.containsKey(panel.getId())) verified++;
        }
        ((TextView) findViewById(R.id.tvVerifiedCount)).setText(verified + " de " + targets.size());
        stringGroups.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (StringGroup group : details.getGroups()) {
            java.util.List<Panel> panels = new java.util.ArrayList<>();
            for (Panel panel : group.getPanels()) {
                if (targets.contains(panel)) panels.add(panel);
            }
            if (panels.isEmpty()) continue;
            int groupVerified = 0;
            for (Panel panel : panels) {
                if (concluded || results.containsKey(panel.getId())) groupVerified++;
            }

            View card = inflater.inflate(R.layout.item_string_group, stringGroups, false);
            ((TextView) card.findViewById(R.id.tvGroupTitle)).setText(
                    ServiceOrderUi.safe(group.getSolarString().getInverterCode()) + ", "
                            + ServiceOrderUi.safe(group.getSolarString().getCode()));
            ((TextView) card.findViewById(R.id.tvGroupCount)).setText(
                    groupVerified + " de " + panels.size() + " placas verificadas");

            LinearLayout plates = card.findViewById(R.id.llPlates);
            View chevron = card.findViewById(R.id.ivChevron);
            card.findViewById(R.id.llGroupHeader).setOnClickListener(v -> {
                boolean expand = plates.getVisibility() != View.VISIBLE;
                plates.setVisibility(expand ? View.VISIBLE : View.GONE);
                chevron.animate().rotation(expand ? 0f : 180f).setDuration(320).start();
                if (expand) Motion.refresh(plates);
            });

            for (int i = 0; i < panels.size(); i++) {
                Panel panel = panels.get(i);
                View row = inflater.inflate(R.layout.item_verified_plate, plates, false);
                ((TextView) row.findViewById(R.id.tvNumber)).setText(String.valueOf(details.positionOf(panel)));
                ((TextView) row.findViewById(R.id.tvPlateSerial)).setText(
                        ServiceOrderUi.safe(panel.getSerialNumber()));
                OsVerificationStore.Result result = results.get(panel.getId());
                bindPosition(row, result, concluded);
                org.apollo.mobile.view.common.CopySerial.onLongPress(row, panel.getSerialNumber());
                if ("EM_ANDAMENTO".equals(details.getOrder().getMaintenanceStatus())) {
                    row.setBackgroundResource(R.drawable.ripple_row);
                    row.setOnClickListener(v -> openCondition(panel));
                }
                row.findViewById(R.id.vDivider).setVisibility(
                        i == panels.size() - 1 ? View.INVISIBLE : View.VISIBLE);
                plates.addView(row);
            }
            stringGroups.addView(card);
        }
    }

    private void bindPosition(View row, OsVerificationStore.Result result, boolean concluded) {
        View badge = row.findViewById(R.id.flPosition);
        TextView number = row.findViewById(R.id.tvNumber);
        ImageView icon = row.findViewById(R.id.ivPositionIcon);
        if (result != null && result.hasProblem()) {
            badge.setBackgroundResource(R.drawable.bg_position_problem);
            icon.setImageResource(R.drawable.ic_exclamation);
        } else if (result != null || concluded) {
            badge.setBackgroundResource(R.drawable.bg_position_ok);
            icon.setImageResource(R.drawable.ic_check_small);
        } else {
            badge.setBackgroundResource(R.drawable.bg_number_circle);
            number.setVisibility(View.VISIBLE);
            icon.setVisibility(View.GONE);
            return;
        }
        number.setVisibility(View.INVISIBLE);
        icon.setVisibility(View.VISIBLE);
    }

    private void renderSummary() {
        View card = findViewById(R.id.cardSummary);
        View reportCard = findViewById(R.id.cardReport);
        boolean done = "CONCLUIDA".equals(details.getOrder().getMaintenanceStatus());
        OsVerificationStore store = new OsVerificationStore(this);
        boolean hasSummary = done && OsSummary.bind(card, details, store.results(orderId), store.isUnidentified(orderId));
        card.setVisibility(hasSummary ? View.VISIBLE : View.GONE);
        String report = details.getOrder().getTechnicalReport();
        boolean hasReport = done && report != null && !report.trim().isEmpty();
        reportCard.setVisibility(hasReport ? View.VISIBLE : View.GONE);
        if (hasReport) {
            int cut = report.indexOf(CompleteMaintenanceActivity.REPORT_SEPARATOR);
            ((TextView) findViewById(R.id.tvReport)).setText((cut > 0 ? report.substring(0, cut) : report).trim());
        }
        if (hasSummary) Motion.fadeUp(card, 120);
        if (hasReport) Motion.fadeUp(reportCard, 180);
    }

    private void openCondition(Panel panel) {
        OsVerificationStore store = new OsVerificationStore(this);
        OsVerificationStore.Result current = store.results(orderId).get(panel.getId());
        ConditionSheet.show(this, panel.getSerialNumber(), current == null ? null : current.getProblem(), problem -> {
            store.verify(orderId, panel.getId(), panel.getSerialNumber(), problem);
            org.apollo.mobile.view.common.AppToast.success(this,
                    problem == null ? "Placa sem problema aparente" : "Condição registrada");
            renderGroups();
            configureActions();
        });
    }

    private void openPendingCondition() {
        long pending = new OsVerificationStore(this).takePendingCondition(orderId);
        if (pending <= 0 || !"EM_ANDAMENTO".equals(details.getOrder().getMaintenanceStatus())) return;
        Panel panel = details.findPanel(pending);
        if (panel != null) primary.postDelayed(() -> openCondition(panel), 350);
    }

    private void renderHistory() {
        history.removeAllViews();
        Order order = details.getOrder();
        String status = order.getMaintenanceStatus();

        addHistory("OS criada", "Aberta a partir de alerta. Técnico: "
                        + ServiceOrderUi.safe(order.getTechnicianName()) + ".",
                ServiceOrderUi.date(order.getOpeningDt()), false);

        if ("ABERTA".equals(status)) {
            addHistory("Aguardando execução", null, null, true);
        } else if ("EM_ANDAMENTO".equals(status) || "CONCLUIDA".equals(status)) {
            addHistory("Execução iniciada", "Você escaneou a placa em campo.", null, false);
        }

        if ("CONCLUIDA".equals(status)) {
            addHistory("OS concluída", "Laudo técnico registrado.",
                    ServiceOrderUi.date(order.getConcludedAt()), false);
        }

        int last = history.getChildCount() - 1;
        if (last >= 0) history.getChildAt(last).findViewById(R.id.vLine).setVisibility(View.INVISIBLE);
    }

    private void addHistory(String title, String description, String date, boolean pending) {
        View item = LayoutInflater.from(this).inflate(R.layout.item_history_event, history, false);
        ((TextView) item.findViewById(R.id.tvTitle)).setText(title);
        setOrHide(item.findViewById(R.id.tvDescription), description);
        setOrHide(item.findViewById(R.id.tvDate), date);
        ((ImageView) item.findViewById(R.id.ivDot)).setImageTintList(ColorStateList.valueOf(
                getColor(pending ? R.color.alert_one : R.color.sucess_one)));
        history.addView(item);
    }

    private void setOrHide(TextView view, String text) {
        view.setText(text);
        view.setVisibility(text == null ? View.GONE : View.VISIBLE);
    }

    private void configureActions() {
        String status = details.getOrder().getMaintenanceStatus();
        boolean hasPanels = !details.getTargetPanels().isEmpty();
        secondary.setVisibility(View.GONE);
        secondary.setEnabled(true);
        primary.setEnabled(true);

        if ("ABERTA".equals(status)) {
            primary.setVisibility(View.VISIBLE);
            if (hasPanels) {
                primary.setText("Escanear placa para iniciar");
                primary.setOnClickListener(view -> openScanner());
            } else {
                primary.setText("Iniciar manutenção");
                primary.setOnClickListener(view -> startMaintenance());
            }
        } else if ("EM_ANDAMENTO".equals(status)) {
            primary.setVisibility(View.VISIBLE);
            primary.setText("Ir para conclusão");
            primary.setOnClickListener(view -> openCompletion());
            if (details.getTargetPanels().size() == 1) {
                Panel panel = details.getTargetPanels().get(0);
                boolean registered = new OsVerificationStore(this).results(orderId).containsKey(panel.getId());
                secondary.setVisibility(View.VISIBLE);
                secondary.setText(registered ? "Condição registrada!" : "Registrar condição");
                secondary.setEnabled(!registered);
                secondary.setOnClickListener(view -> openCondition(panel));
            }
        } else {
            primary.setVisibility(View.GONE);
        }
    }

    private void openScanner() {
        Intent intent = new Intent(this, ScannerActivity.class);
        intent.putExtra(ScannerActivity.EXTRA_ORDER_ID, orderId);
        startActivity(intent);
    }

    private void openCompletion() {
        Intent intent = new Intent(this, CompleteMaintenanceActivity.class);
        intent.putExtra(EXTRA_ORDER_ID, orderId);
        startActivity(intent);
    }

    private void startMaintenance() {
        primary.setEnabled(false);
        gateway.start(orderId, new ServiceOrderGateway.Callback<Order>() {
            @Override
            public void onSuccess(Order value) {
                primary.setEnabled(true);
                if (isFinishing() || isDestroyed()) return;
                org.apollo.mobile.view.common.AppToast.success(OsDetailsActivity.this, "Manutenção iniciada");
                load();
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                primary.setEnabled(true);
                org.apollo.mobile.view.common.AppToast.error(OsDetailsActivity.this, "Não foi possível iniciar a manutenção.");
            }
        });
    }

    private static String join(String inverter, String string) {
        return ServiceOrderUi.safe(inverter) + " / " + ServiceOrderUi.safe(string);
    }

    // ---- Tags coloridas (também usadas na lista de OS) ----

    static void bindStatusTag(TextView tag, String status) {
        int color;
        if ("EM_ANDAMENTO".equals(status)) color = R.color.alert_two;
        else if ("CONCLUIDA".equals(status)) color = R.color.sucess_two;
        else if ("CANCELADA".equals(status)) color = R.color.disable_gray;
        else color = R.color.info_two;
        tag.setText(ServiceOrderUi.status(status));
        tag.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(tag.getContext(), color)));
    }

    static void bindPriorityTag(TextView tag, String priority) {
        int color;
        if ("ALTA".equals(priority) || "CRITICA".equals(priority)) color = R.color.error_two;
        else if ("BAIXA".equals(priority)) color = R.color.sucess_two;
        else color = R.color.alert_two;
        tag.setText(ServiceOrderUi.priority(priority));
        tag.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(tag.getContext(), color)));
    }
}
