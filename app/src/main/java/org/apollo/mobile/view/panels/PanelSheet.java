package org.apollo.mobile.view.panels;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.apollo.mobile.R;
import org.apollo.mobile.panels.gateway.PanelItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class PanelSheet {
    private PanelSheet() {
    }

    static void show(Activity activity, PanelItem panel, String stringCode) {
        show(activity, panel, stringCode, 0, 0, null);
    }

    static void show(Activity activity, PanelItem panel, String stringCode, int position, int total) {
        show(activity, panel, stringCode, position, total, null);
    }

    public static void show(Activity activity, PanelItem panel, String stringCode, Runnable onDismiss) {
        show(activity, panel, stringCode, 0, 0, onDismiss);
    }

    public static void show(Activity activity, PanelItem panel, String stringCode, int position, int total,
                            Runnable onDismiss) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.sheet_panel_details, null);
        dialog.setContentView(view);
        View container = (View) view.getParent();
        if (container != null) container.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        TextView sheetSerial = view.findViewById(R.id.tvSheetSerial);
        sheetSerial.setText(panel.getSerialNumber());
        org.apollo.mobile.view.common.CopySerial.attach(sheetSerial);
        LinearLayout rows = view.findViewById(R.id.llSheetRows);
        String unavailable = activity.getString(R.string.panels_unavailable_value);

        addRow(activity, rows, R.string.panels_sheet_model, orDash(joinModel(panel), unavailable));
        addRow(activity, rows, R.string.panels_sheet_string,
                orDash(joinInverter(panel, stringCode), unavailable));
        if (position > 0 && total > 0) {
            addRow(activity, rows, org.apollo.mobile.view.common.Tr.t("Posição"), String.format(java.util.Locale.ROOT, "%02d de %02d", position, total));
        }
        addRow(activity, rows, R.string.panels_sheet_installation, formatDate(panel.getInstallationDt(), unavailable));
        Double value = panel.getEfficiencyPct() != null ? panel.getEfficiencyPct() : panel.getStringHealthScore();
        String efficiency = PanelsUi.percent(value);
        addRow(activity, rows, R.string.panels_sheet_efficiency, efficiency == null ? unavailable : efficiency);

        view.findViewById(R.id.btnSheetClose).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSheetReport).setOnClickListener(v -> {
            dialog.dismiss();
            activity.startActivity(PanelDetailsActivity.reportIntent(activity, panel));
        });
        if (onDismiss != null) {
            dialog.setOnDismissListener(d -> onDismiss.run());
        }
        dialog.show();
    }

    private static void addRow(Activity activity, LinearLayout rows, int label, String value) {
        addRow(activity, rows, activity.getString(label), value);
    }

    private static void addRow(Activity activity, LinearLayout rows, String label, String value) {
        View row = LayoutInflater.from(activity).inflate(R.layout.item_activation_info, rows, false);
        ((TextView) row.findViewById(R.id.tvLabel)).setText(label);
        ((TextView) row.findViewById(R.id.tvValue)).setText(value);
        rows.addView(row);
    }

    private static String joinModel(PanelItem panel) {
        String manufacturer = panel.getManufacturer();
        String model = panel.getModel();
        if (manufacturer.isEmpty()) {
            return model;
        }
        return model.isEmpty() ? manufacturer : manufacturer + " " + model;
    }

    private static String joinInverter(PanelItem panel, String fallbackString) {
        String string = panel.getStringCode().isEmpty() ? fallbackString : panel.getStringCode();
        String inverter = panel.getInverterCode();
        if (string == null || string.isEmpty()) {
            return inverter;
        }
        return inverter.isEmpty() ? string : inverter + " / " + string;
    }

    private static String orDash(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static String formatDate(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException exception) {
            return value;
        }
    }
}
