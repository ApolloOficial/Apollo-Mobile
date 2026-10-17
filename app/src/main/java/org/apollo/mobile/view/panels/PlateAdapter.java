package org.apollo.mobile.view.panels;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.apollo.mobile.R;
import org.apollo.mobile.panels.PanelCategory;
import org.apollo.mobile.panels.gateway.PanelItem;

import java.util.ArrayList;
import java.util.List;

final class PlateAdapter extends RecyclerView.Adapter<PlateAdapter.Holder> {

    interface Listener {
        void onClick(PanelItem panel);
    }

    private final List<PanelItem> items = new ArrayList<>();
    private final Listener listener;

    PlateAdapter(Listener listener) {
        this.listener = listener;
    }

    void submit(List<PanelItem> panels) {
        items.clear();
        items.addAll(panels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plate_detail, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        PanelItem panel = items.get(position);
        holder.bind(panel);
        holder.itemView.setOnClickListener(view -> listener.onClick(panel));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView serial;
        private final View position;
        private final View statusTag;
        private final View statusIcon;
        private final TextView statusText;
        private final View efficiency;
        private final TextView efficiencyValue;
        private final LinearProgressIndicator progress;
        private final TextView stockInfo;
        private final View seeOrder;

        Holder(@NonNull View view) {
            super(view);
            card = view.findViewById(R.id.cardPlate);
            serial = view.findViewById(R.id.tvSerialNumber);
            position = view.findViewById(R.id.tvPosition);
            statusTag = view.findViewById(R.id.llStatusTag);
            statusIcon = view.findViewById(R.id.ivStatusIcon);
            statusText = view.findViewById(R.id.tvStatusTagText);
            efficiency = view.findViewById(R.id.clEfficiency);
            efficiencyValue = view.findViewById(R.id.tvEfficiencyValue);
            progress = view.findViewById(R.id.progressEfficiency);
            stockInfo = view.findViewById(R.id.tvStockInfo);
            seeOrder = view.findViewById(R.id.btnVerOS);
        }

        void bind(PanelItem panel) {
            android.content.Context context = itemView.getContext();
            PanelCategory category = PanelCategory.of(panel);
            serial.setText(panel.getSerialNumber());
            position.setVisibility(View.GONE);
            seeOrder.setVisibility(View.GONE);

            int tint;
            int label;
            int stroke = R.color.disable_gray;
            if (category == PanelCategory.STOCK) {
                tint = R.color.disable_gray;
                label = R.string.panels_in_stock;
            } else if (category == PanelCategory.ATTENTION) {
                tint = R.color.error_one;
                label = R.string.panels_attention;
                stroke = R.color.error_one;
            } else {
                tint = R.color.sucess_two;
                label = R.string.panels_in_operation;
            }
            statusTag.setBackgroundTintList(ColorStateList.valueOf(context.getColor(tint)));
            statusText.setText(label);
            statusText.setTextColor(context.getColor(category == PanelCategory.ATTENTION
                    ? R.color.neutral_white : category == PanelCategory.STOCK
                    ? R.color.neutral_gray : R.color.neutral_black));
            statusIcon.setVisibility(category == PanelCategory.ATTENTION ? View.VISIBLE : View.GONE);
            card.setStrokeColor(context.getColor(stroke));
            boolean attention = category == PanelCategory.ATTENTION;
            seeOrder.setVisibility(attention ? View.VISIBLE : View.GONE);
            seeOrder.setOnClickListener(attention ? v -> context.startActivity(new android.content.Intent(
                    context, org.apollo.mobile.view.maintenance.os.ServiceOrdersActivity.class)) : null);

            boolean stock = category == PanelCategory.STOCK;
            stockInfo.setVisibility(stock ? View.VISIBLE : View.GONE);
            stockInfo.setText(R.string.panels_awaiting_activation);
            Double health = panel.getStringHealthScore();
            boolean showEfficiency = !stock && health != null;
            efficiency.setVisibility(showEfficiency ? View.VISIBLE : View.GONE);
            if (showEfficiency) {
                int color = category == PanelCategory.ATTENTION ? R.color.error_one
                        : health >= 50 ? R.color.sucess_two : R.color.alert_one;
                int textColor = category == PanelCategory.ATTENTION ? R.color.error_one
                        : health >= 50 ? R.color.sucess_one : R.color.alert_three;
                efficiencyValue.setText(PanelsUi.percent(health));
                efficiencyValue.setTextColor(context.getColor(textColor));
                progress.setProgressCompat((int) Math.round(health), true);
                progress.setIndicatorColor(context.getColor(color));
            }
        }
    }
}
