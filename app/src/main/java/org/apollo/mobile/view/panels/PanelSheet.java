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
        show(activity, panel, stringCode, null);
    }

    public static void show(Activity activity, PanelItem panel, String stringCode, Runnable onDismiss) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.sheet_panel_details, null);
        dialog.setContentView(view);
        View container = (View) view.getParent();
        if (container != null) container.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        ((TextView) view.findViewById(R.id.tvSheetSerial)).setText(panel.getSerialNumber());
        LinearLayout rows = view.findViewById(R.id.llSheetRows);
        String unavailable = activity.getString(R.string.panels_unavailable_value);

        addRow(activity, rows, R.string.panels_sheet_model, orDash(joinModel(panel), unavailable));
        addRow(activity, rows, R.string.panels_sheet_string,
                orDash(joinInverter(panel, stringCode), unavailable));
        addRow(activity, rows, R.string.panels_sheet_installation, formatDate(panel.getInstallationDt(), unavailable));
        String efficiency = PanelsUi.percent(panel.getStringHealthScore());
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
