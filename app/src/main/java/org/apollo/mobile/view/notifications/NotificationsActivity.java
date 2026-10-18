package org.apollo.mobile.view.notifications;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apollo.mobile.R;
import org.apollo.mobile.notifications.NotificationItem;
import org.apollo.mobile.notifications.NotificationRepository;
import org.apollo.mobile.view.common.Motion;
import org.apollo.mobile.view.common.TopBarFragment;

import java.util.List;

public final class NotificationsActivity extends AppCompatActivity implements TopBarFragment.Host {

    private NotificationRepository repository;
    private NotificationAdapter adapter;
    private RecyclerView list;
    private View empty;
    private TextView emptyText;
    private View progress;
    private View markAll;
    private TextView filterLabel;
    private boolean unreadOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        org.apollo.mobile.view.common.SystemBars.apply(this);

        repository = new NotificationRepository(this);
        list = findViewById(R.id.rvNotifications);
        empty = findViewById(R.id.llNotificationsEmpty);
        emptyText = findViewById(R.id.tvNotificationsEmpty);
        progress = findViewById(R.id.progressNotifications);
        markAll = findViewById(R.id.btnMarkAllRead);
        filterLabel = findViewById(R.id.tvNotificationsFilter);

        adapter = new NotificationAdapter(this::open);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        markAll.setOnClickListener(v -> repository.markAllRead(this::load));

        Motion.stagger(80, findViewById(R.id.btnBack));
        load();
    }

    @Override
    public int topBarActionIcon() {
        return R.drawable.ic_options;
    }

    @Override
    public void onTopBarAction() {
        startActivity(new android.content.Intent(this, org.apollo.mobile.view.settings.NotificationsSettingsActivity.class));
    }

    private void load() {
        progress.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        repository.list(unreadOnly, new NotificationRepository.Result<List<NotificationItem>>() {
            @Override
            public void onSuccess(List<NotificationItem> value) {
                if (isFinishing()) return;
                progress.setVisibility(View.GONE);
                adapter.submit(value);
                list.scheduleLayoutAnimation();
                boolean isEmpty = value.isEmpty();
                emptyText.setText(unreadOnly ? R.string.notifications_empty_unread : R.string.notifications_empty);
                empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                if (isEmpty) Motion.fadeUp(empty, 0);
                boolean anyUnread = false;
                for (NotificationItem item : value) anyUnread |= !item.isRead();
                markAll.setVisibility(anyUnread ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure() {
                if (isFinishing()) return;
                progress.setVisibility(View.GONE);
                if (adapter.getItemCount() == 0) {
                    emptyText.setText(R.string.notifications_error);
                    empty.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void open(NotificationItem item, int position) {
        if (!item.isRead()) {
            item.markRead();
            repository.markRead(item.getId());
            if (position != RecyclerView.NO_POSITION) adapter.notifyItemChanged(position);
        }
    }
}
