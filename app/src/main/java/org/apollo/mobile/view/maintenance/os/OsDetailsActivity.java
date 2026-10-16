package org.apollo.mobile.view.maintenance.os;

import android.content.Intent;
import org.apollo.mobile.view.common.Motion;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.apollo.mobile.R;
import org.apollo.mobile.maintenance.gateway.ServiceOrderError;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGateway;
import org.apollo.mobile.maintenance.gateway.ServiceOrderGatewayFactory;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Details;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Panel;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Scope;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.StringGroup;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Warning;
import org.apollo.mobile.view.common.BottomNavigation;

public final class OsDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_ORDER_ID = "service_order_id";

    private ServiceOrderGateway gateway;
    private long orderId;
    private Details details;
    private LinearLayout infoRows;
    private LinearLayout stringGroups;
    private LinearLayout history;
    private View verifiedSection;
    private Button primary;
    private Button secondary;

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
                details = value;
                render();
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                Toast.makeText(OsDetailsActivity.this,
                        "Não foi possível carregar a OS.", Toast.LENGTH_LONG).show();
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
        configureActions();
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

    /** Placas por string (OS de string/inversor). Sem endpoint de verificação, todas aparecem pendentes. */
    private void renderGroups() {
        boolean show = details.getScope() != Scope.PANEL;
        verifiedSection.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) return;

        int total = details.getPanels().size();
        ((TextView) findViewById(R.id.tvVerifiedCount)).setText("0 de " + total);
        stringGroups.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (StringGroup group : details.getGroups()) {
            View card = inflater.inflate(R.layout.item_string_group, stringGroups, false);
            ((TextView) card.findViewById(R.id.tvGroupTitle)).setText(
                    ServiceOrderUi.safe(group.getSolarString().getInverterCode()) + ", "
                            + ServiceOrderUi.safe(group.getSolarString().getCode()));
            ((TextView) card.findViewById(R.id.tvGroupCount)).setText(
                    "0 de " + group.getPanels().size() + " placas verificadas");

            LinearLayout plates = card.findViewById(R.id.llPlates);
            View chevron = card.findViewById(R.id.ivChevron);
            card.findViewById(R.id.llGroupHeader).setOnClickListener(v -> {
                boolean expand = plates.getVisibility() != View.VISIBLE;
                plates.setVisibility(expand ? View.VISIBLE : View.GONE);
                chevron.setRotation(expand ? 0f : 180f);
            });

            for (int i = 0; i < group.getPanels().size(); i++) {
                Panel panel = group.getPanels().get(i);
                View row = inflater.inflate(R.layout.item_verified_plate, plates, false);
                ((TextView) row.findViewById(R.id.tvNumber)).setText(String.valueOf(i + 1));
                ((TextView) row.findViewById(R.id.tvPlateSerial)).setText(
                        ServiceOrderUi.safe(panel.getSerialNumber()));
                row.findViewById(R.id.vDivider).setVisibility(
                        i == group.getPanels().size() - 1 ? View.INVISIBLE : View.VISIBLE);
                plates.addView(row);
            }
            stringGroups.addView(card);
        }
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
            addHistory("Execução iniciada", "Manutenção em atendimento pelo técnico.", null, false);
        }

        if ("CONCLUIDA".equals(status)) {
            addHistory("OS concluída", ServiceOrderUi.safe(order.getTechnicalReport()),
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
        secondary.setVisibility(View.GONE);

        if ("ABERTA".equals(status)) {
            primary.setVisibility(View.VISIBLE);
            primary.setText("Iniciar manutenção");
            primary.setOnClickListener(view -> startMaintenance());
        } else if ("EM_ANDAMENTO".equals(status)) {
            primary.setVisibility(View.VISIBLE);
            primary.setText("Ir para conclusão");
            primary.setOnClickListener(view -> {
                Intent intent = new Intent(this, CompleteMaintenanceActivity.class);
                intent.putExtra(EXTRA_ORDER_ID, orderId);
                startActivity(intent);
            });
        } else {
            primary.setVisibility(View.GONE);
        }
    }

    private void startMaintenance() {
        primary.setEnabled(false);
        gateway.start(orderId, new ServiceOrderGateway.Callback<Order>() {
            @Override
            public void onSuccess(Order value) {
                primary.setEnabled(true);
                load();
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                primary.setEnabled(true);
                Toast.makeText(OsDetailsActivity.this,
                        "Não foi possível iniciar a manutenção.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static String join(String inverter, String string) {
        return ServiceOrderUi.safe(inverter) + " / " + ServiceOrderUi.safe(string);
    }

    // ---- Tags coloridas (também usadas na lista de OS) ----

    static void bindStatusTag(TextView tag, String status) {
        int color;
        if ("EM_ANDAMENTO".equals(status)) color = R.color.primary_one;
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
        else color = R.color.primary_one;
        tag.setText(ServiceOrderUi.priority(priority));
        tag.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(tag.getContext(), color)));
    }
}
