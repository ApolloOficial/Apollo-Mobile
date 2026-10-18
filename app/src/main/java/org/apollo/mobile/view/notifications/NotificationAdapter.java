package org.apollo.mobile.view.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apollo.mobile.R;
import org.apollo.mobile.notifications.NotificationItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Holder> {

    public interface Listener {
        void onClick(NotificationItem item, int position);
    }

    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
    private static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm", Locale.getDefault());

    private final List<NotificationItem> items = new ArrayList<>();
    private final Listener listener;

    public NotificationAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<NotificationItem> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    public List<NotificationItem> items() {
        return items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NotificationItem item = items.get(position);
        Context context = holder.itemView.getContext();
        holder.topDivider.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        holder.icon.setImageResource(iconFor(item.getType()));
        holder.title.setText(item.getTitle());
        holder.body.setText(item.getBody());
        holder.body.setVisibility(item.getBody().isEmpty() ? View.GONE : View.VISIBLE);
        holder.time.setText(formatTime(context, item.createdAtLocal()));
        holder.unread.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);
        android.graphics.Typeface face = androidx.core.content.res.ResourcesCompat.getFont(context, item.isRead() ? R.font.manrope_regular : R.font.manrope_bold);
        holder.title.setTypeface(face);
        holder.time.setTypeface(face);
        holder.row.setOnClickListener(v -> listener.onClick(item, holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static int iconFor(String type) {
        String t = type.toUpperCase(Locale.ROOT);
        if (t.contains("SYNC")) return R.drawable.ic_notif_sync;
        if (t.contains("ALERT") || t.contains("CRITICAL") || t.contains("PERFORMANCE")
                || t.contains("REJECT") || t.contains("PROBLEM")) return R.drawable.ic_notif_alert;
        if (t.contains("ACTIVAT") || t.contains("INSTALL") || t.contains("APPROV")
                || t.contains("COMPLET") || t.contains("SUCCESS")) return R.drawable.ic_notif_activated;
        return R.drawable.ic_notif_predictive;
    }

    static String formatTime(Context context, LocalDateTime time) {
        if (time == null) return "";
        LocalDate today = LocalDate.now();
        if (time.toLocalDate().equals(today)) return time.format(HOUR);
        if (time.toLocalDate().equals(today.minusDays(1))) {
            return context.getString(R.string.notifications_yesterday, time.format(HOUR));
        }
        return time.format(FULL);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final View topDivider;
        final View row;
        final ImageView icon;
        final TextView title;
        final TextView body;
        final TextView time;
        final View unread;

        Holder(@NonNull View itemView) {
            super(itemView);
            topDivider = itemView.findViewById(R.id.vNotificationTopDivider);
            row = itemView.findViewById(R.id.llNotificationRow);
            icon = itemView.findViewById(R.id.ivNotificationIcon);
            title = itemView.findViewById(R.id.tvNotificationTitle);
            body = itemView.findViewById(R.id.tvNotificationBody);
            time = itemView.findViewById(R.id.tvNotificationTime);
            unread = itemView.findViewById(R.id.vNotificationUnread);
        }
    }
}
