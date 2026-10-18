package org.apollo.mobile.view.common;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.apollo.mobile.R;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class CalendarDialog {

    public interface Listener {
        void onDateSelected(LocalDate date);
    }

    private static final String[] WEEKDAYS = {("Dom."), ("Seg."), ("Ter."), ("Qua."), ("Qui."), ("Sex."), ("Sáb.")};
    private static final DateTimeFormatter MONTH =
            DateTimeFormatter.ofPattern("MMMM 'de' yyyy", new Locale("pt", "BR"));

    private final Activity activity;
    private final Dialog dialog;
    private final LocalDate min;
    private final LocalDate max;
    private final Listener listener;
    private LocalDate selected;
    private YearMonth month;

    private CalendarDialog(Activity activity, LocalDate selected, LocalDate min, LocalDate max, Listener listener) {
        this.activity = activity;
        this.selected = selected;
        this.min = min;
        this.max = max;
        this.listener = listener;
        this.month = YearMonth.from(selected == null ? LocalDate.now() : selected);
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_calendar);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int side = Math.round(24 * activity.getResources().getDisplayMetrics().density);
            window.getDecorView().setPadding(side, 0, side, 0);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        LinearLayout weekdays = dialog.findViewById(R.id.llCalWeekdays);
        for (String day : WEEKDAYS) {
            TextView label = cell(org.apollo.mobile.view.common.Tr.t(day));
            label.setTextColor(0xFF716764);
            weekdays.addView(label);
        }
        dialog.findViewById(R.id.btnCalPrev).setOnClickListener(v -> {
            if (canGo(month.minusMonths(1))) {
                month = month.minusMonths(1);
                render();
            }
        });
        dialog.findViewById(R.id.btnCalNext).setOnClickListener(v -> {
            if (canGo(month.plusMonths(1))) {
                month = month.plusMonths(1);
                render();
            }
        });
        render();
    }

    public static void show(Activity activity, LocalDate selected, LocalDate min, LocalDate max, Listener listener) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        new CalendarDialog(activity, selected, min, max, listener).dialog.show();
    }

    private boolean canGo(YearMonth target) {
        if (min != null && target.isBefore(YearMonth.from(min))) return false;
        return max == null || !target.isAfter(YearMonth.from(max));
    }

    private void render() {
        String title = month.format(MONTH);
        ((TextView) dialog.findViewById(R.id.tvCalMonth)).setText(title);
        tintArrow(dialog.findViewById(R.id.btnCalPrev), canGo(month.minusMonths(1)));
        tintArrow(dialog.findViewById(R.id.btnCalNext), canGo(month.plusMonths(1)));

        LinearLayout days = dialog.findViewById(R.id.llCalDays);
        days.removeAllViews();
        LocalDate first = month.atDay(1);
        LocalDate cursor = first.minusDays(first.getDayOfWeek().getValue() % 7);
        for (int week = 0; week < 6; week++) {
            if (week == 5 && cursor.getMonthValue() != month.getMonthValue()) break;
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 0; i < 7; i++) {
                LocalDate date = cursor;
                TextView cell = cell(String.valueOf(date.getDayOfMonth()));
                boolean inMonth = date.getMonthValue() == month.getMonthValue();
                boolean allowed = (min == null || !date.isBefore(min)) && (max == null || !date.isAfter(max));
                boolean isSelected = date.equals(selected);
                if (isSelected) {
                    cell.setBackgroundResource(R.drawable.bg_calendar_selected);
                    cell.setTextColor(Color.WHITE);
                } else {
                    cell.setTextColor(!allowed ? 0x66716764 : inMonth ? 0xFF1C1C1C : 0xFF716764);
                }
                if (allowed) {
                    cell.setOnClickListener(v -> {
                        selected = date;
                        render();
                        v.postDelayed(() -> {
                            listener.onDateSelected(date);
                            dialog.dismiss();
                        }, 180);
                    });
                }
                android.widget.FrameLayout holder = new android.widget.FrameLayout(activity);
                holder.setLayoutParams(cell.getLayoutParams());
                int circle = Math.round(36 * activity.getResources().getDisplayMetrics().density);
                cell.setLayoutParams(new android.widget.FrameLayout.LayoutParams(circle, circle, Gravity.CENTER));
                holder.addView(cell);
                row.addView(holder);
                cursor = cursor.plusDays(1);
            }
            days.addView(row);
        }
    }

    private TextView cell(String text) {
        float density = activity.getResources().getDisplayMetrics().density;
        TextView view = new TextView(activity);
        int size = Math.round(36 * density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, size, 1f);
        int gap = Math.round(2 * density);
        params.setMargins(gap, gap, gap, gap);
        view.setLayoutParams(params);
        view.setGravity(Gravity.CENTER);
        view.setText(text);
        view.setTextSize(14);
        view.setTypeface(ResourcesCompat.getFont(activity, R.font.manrope_bold));
        return view;
    }

    private static void tintArrow(View arrow, boolean enabled) {
        ((ImageView) arrow).setColorFilter(enabled ? 0xFFFF672C : 0xB3D1CEC1);
        arrow.setEnabled(enabled);
    }
}
