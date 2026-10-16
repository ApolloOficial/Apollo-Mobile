package org.apollo.mobile.view.panels;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apollo.mobile.R;
import org.apollo.mobile.panels.PanelCategory;
import org.apollo.mobile.panels.gateway.PanelItem;
import org.apollo.mobile.panels.gateway.PanelStringItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class InverterAdapter extends RecyclerView.Adapter<InverterAdapter.Holder> {
    private static final int PREVIEW_LIMIT = 5;
    private static final Pattern TRAILING_NUMBER = Pattern.compile("(\\d+)$");

    interface Listener {
        void onExpand(PanelStringItem item);

        void onSeeAll(PanelStringItem item);
    }

    private final List<PanelStringItem> items = new ArrayList<>();
    private final Set<String> expanded = new HashSet<>();
    private final Map<String, List<PanelItem>> previews = new HashMap<>();
    private final Listener listener;

    InverterAdapter(Listener listener) {
        this.listener = listener;
    }

    void submit(List<PanelStringItem> strings) {
        items.clear();
        items.addAll(strings);
        notifyDataSetChanged();
    }

    void setPreview(String stringId, List<PanelItem> panels) {
        previews.put(stringId, panels);
        for (int index = 0; index < items.size(); index++) {
            if (stringId.equals(items.get(index).getId())) {
                notifyItemChanged(index);
            }
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inverter, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        PanelStringItem item = items.get(position);
        boolean firstOfInverter = position == 0
                || !items.get(position - 1).getInverterCode().equals(item.getInverterCode());
        boolean isExpanded = expanded.contains(item.getId());
        holder.bind(item, firstOfInverter, isExpanded, previews.get(item.getId()));
        holder.header.setOnClickListener(view -> toggle(item));
        holder.seeAll.setOnClickListener(view -> listener.onSeeAll(item));
    }

    private void toggle(PanelStringItem item) {
        String id = item.getId();
        if (expanded.contains(id)) {
            expanded.remove(id);
        } else {
            expanded.add(id);
            if (!previews.containsKey(id)) {
                listener.onExpand(item);
            }
        }
        for (int index = 0; index < items.size(); index++) {
            if (id.equals(items.get(index).getId())) {
                notifyItemChanged(index);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static String inverterLabel(Holder holder, String inverterCode) {
        Matcher matcher = TRAILING_NUMBER.matcher(inverterCode);
        String name = matcher.find() ? matcher.group(1) : inverterCode;
        return holder.itemView.getContext().getString(R.string.panels_inverter_group, name);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final View header;
        final TextView seeAll;
        private final TextView group;
        private final TextView stringCode;
        private final TextView details;
        private final ImageView chevron;
        private final LinearLayout content;
        private final TextView stock;
        private final TextView operation;
        private final TextView attention;
        private final TextView health;
        private final LinearLayout platesList;

        Holder(@NonNull View view) {
            super(view);
            group = view.findViewById(R.id.tvInverterGroupName);
            stringCode = view.findViewById(R.id.tvInverterId);
            details = view.findViewById(R.id.tvInverterDetails);
            chevron = view.findViewById(R.id.ivChevron);
            content = view.findViewById(R.id.llCardContent);
            header = (View) stringCode.getParent();
            stock = view.findViewById(R.id.tvStockCount);
            operation = view.findViewById(R.id.tvOperationCount);
            attention = view.findViewById(R.id.tvAttentionCount);
            health = view.findViewById(R.id.tvHealthValue);
            platesList = view.findViewById(R.id.llPlatesList);
            seeAll = view.findViewById(R.id.tvSeeAllPlates);
        }

        void bind(PanelStringItem item, boolean firstOfInverter, boolean expanded, List<PanelItem> preview) {
            android.content.Context context = itemView.getContext();
            group.setVisibility(firstOfInverter ? View.VISIBLE : View.GONE);
            group.setText(inverterLabel(this, item.getInverterCode()));
            stringCode.setText(item.getCode());
            long total = totalPanels(item);
            details.setText(context.getString(R.string.panels_summary,
                    String.format(Locale.US, "%.0f", item.getNominalPowerKwp()), total));
            stock.setText(String.format(Locale.US, "%02d", item.getPanelsInStock()));
            operation.setText(String.format(Locale.US, "%02d", item.getPanelsOperational()));
            attention.setText(String.format(Locale.US, "%02d", item.getActiveWarnings()));
            String percent = PanelsUi.percent(item.getHealthScore());
            health.setText(percent == null ? context.getString(R.string.panels_health_unknown) : percent);
            seeAll.setText(context.getString(R.string.panels_see_all, total));
            chevron.setRotation(expanded ? 270f : 90f);
            for (int index = 1; index < content.getChildCount(); index++) {
                content.getChildAt(index).setVisibility(expanded ? View.VISIBLE : View.GONE);
            }
            platesList.removeAllViews();
            if (expanded && preview != null) {
                int shown = Math.min(PREVIEW_LIMIT, preview.size());
                LayoutInflater inflater = LayoutInflater.from(context);
                for (int index = 0; index < shown; index++) {
                    bindPreviewRow(inflater, preview.get(index));
                }
            }
        }

        private void bindPreviewRow(LayoutInflater inflater, PanelItem panel) {
            View row = inflater.inflate(R.layout.item_plate_summary, platesList, false);
            PanelCategory category = PanelCategory.of(panel);
            int dot = category == PanelCategory.ATTENTION ? R.color.error_one
                    : category == PanelCategory.STOCK ? R.color.disable_gray : R.color.sucess_one;
            int label = category == PanelCategory.ATTENTION ? R.string.panels_attention
                    : category == PanelCategory.STOCK ? R.string.panels_in_stock : R.string.panels_in_operation;
            row.findViewById(R.id.vStatusDot).setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(itemView.getContext().getColor(dot)));
            ((TextView) row.findViewById(R.id.tvPlateName)).setText(panel.getSerialNumber());
            ((TextView) row.findViewById(R.id.tvPlateValue)).setText(label);
            platesList.addView(row);
        }

        private static long totalPanels(PanelStringItem item) {
            return item.getPanelsInStock() + item.getPanelsOperational() + item.getPanelsInMaintenance();
        }
    }
}
