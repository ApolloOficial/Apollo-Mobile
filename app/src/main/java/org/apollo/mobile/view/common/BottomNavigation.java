package org.apollo.mobile.view.common;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import org.apollo.mobile.R;
import org.apollo.mobile.view.chat.ChatbotActivity;
import org.apollo.mobile.view.home.HomeActivity;
import org.apollo.mobile.view.maintenance.MaintenanceActivity;
import org.apollo.mobile.view.scanner.ScannerActivity;
import org.apollo.mobile.view.settings.SettingsActivity;


public final class BottomNavigation {

    public enum Tab { HOME, PANELS, TOOLS, SETTINGS, NONE }

    private BottomNavigation() {}

    public static void setup(Activity activity, Tab current) {
        highlight(activity, current);

        bind(activity, R.id.llHome, current == Tab.HOME ? null : HomeActivity.class);
        bind(activity, R.id.llPanels, null); // TODO: PanelsActivity.class quando existir
        bind(activity, R.id.llTools, current == Tab.TOOLS ? null : MaintenanceActivity.class);
        bind(activity, R.id.llSettings, current == Tab.SETTINGS ? null : SettingsActivity.class);
        bind(activity, R.id.cvScanContainer, ScannerActivity.class);
        bind(activity, R.id.fabChatbot, ChatbotActivity.class);
    }

    private static void bind(Activity activity, int viewId, Class<? extends Activity> target) {
        View view = activity.findViewById(viewId);
        if (view == null) return;

        if (target == null) {
            view.setOnClickListener(null);
            return;
        }

        view.setOnClickListener(v -> {
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
        });
    }

    private static void highlight(Activity activity, Tab current) {
        set(activity, R.id.ivHome, R.id.indicatorHome,
                current == Tab.HOME, R.drawable.ic_home_black, R.drawable.ic_home_gray);
        set(activity, R.id.ivPanels, R.id.indicatorPanels,
                current == Tab.PANELS, R.drawable.ic_solarpanel_black, R.drawable.ic_solarpanel_gray);
        set(activity, R.id.ivTools, R.id.indicatorTools,
                current == Tab.TOOLS, R.drawable.ic_tools_black, R.drawable.ic_tools_gray);
        set(activity, R.id.ivSettings, R.id.indicatorSettings,
                current == Tab.SETTINGS, R.drawable.ic_settings_black, R.drawable.ic_settings_gray);
    }

    private static void set(Activity a, int iconId, int indicatorId, boolean active, int on, int off) {
        ImageView icon = a.findViewById(iconId);
        View indicator = a.findViewById(indicatorId);
        if (icon != null) icon.setImageResource(active ? on : off);
        if (indicator != null) indicator.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
    }
}