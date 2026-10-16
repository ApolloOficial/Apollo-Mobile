package org.apollo.mobile.view.common;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import org.apollo.mobile.R;
import org.apollo.mobile.view.chat.ChatbotActivity;
import org.apollo.mobile.view.home.HomeActivity;
import org.apollo.mobile.view.maintenance.MaintenanceActivity;
import org.apollo.mobile.view.panels.PanelsActivity;
import org.apollo.mobile.view.scanner.ScannerActivity;
import org.apollo.mobile.view.settings.SettingsActivity;

public final class BottomNavigation {

    public enum Tab { HOME, PANELS, TOOLS, SETTINGS, NONE }

    private static Tab lastTab = Tab.NONE;

    private BottomNavigation() {}

    public static void setup(Activity activity, Tab current) {
        highlight(activity, current);

        bind(activity, R.id.llHome, current == Tab.HOME ? null : HomeActivity.class, R.id.ivHome);
        bind(activity, R.id.llPanels, current == Tab.PANELS ? null : PanelsActivity.class, R.id.ivPanels);
        bind(activity, R.id.llTools, current == Tab.TOOLS ? null : MaintenanceActivity.class, R.id.ivTools);
        bind(activity, R.id.llSettings, current == Tab.SETTINGS ? null : SettingsActivity.class, R.id.ivSettings);
        bind(activity, R.id.cvScanContainer, ScannerActivity.class, R.id.ivScan);
        bind(activity, R.id.fabChatbot, ChatbotActivity.class, 0);

        Motion.pressable(activity.findViewById(R.id.cvScanContainer), activity.findViewById(R.id.fabChatbot));
        moveIndicator(activity, current);
    }

    private static void bind(Activity activity, int viewId, Class<? extends Activity> target, int iconId) {
        View view = activity.findViewById(viewId);
        if (view == null) return;

        view.setOnClickListener(v -> {
            View icon = iconId == 0 ? v : activity.findViewById(iconId);
            if (icon != null) Motion.bounce(icon);
            if (target == null) return;
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            v.postDelayed(() -> {
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
            }, 90);
        });
    }

    private static void highlight(Activity activity, Tab current) {
        set(activity, R.id.ivHome, current == Tab.HOME, R.drawable.ic_home_black, R.drawable.ic_home_gray);
        set(activity, R.id.ivPanels, current == Tab.PANELS, R.drawable.ic_solarpanel_black, R.drawable.ic_solarpanel_gray);
        set(activity, R.id.ivTools, current == Tab.TOOLS, R.drawable.ic_tools_black, R.drawable.ic_tools_gray);
        set(activity, R.id.ivSettings, current == Tab.SETTINGS, R.drawable.ic_settings_black, R.drawable.ic_settings_gray);
    }

    private static void set(Activity a, int iconId, boolean active, int on, int off) {
        ImageView icon = a.findViewById(iconId);
        if (icon != null) icon.setImageResource(active ? on : off);
    }

    private static int tabViewId(Tab tab) {
        switch (tab) {
            case HOME: return R.id.llHome;
            case PANELS: return R.id.llPanels;
            case TOOLS: return R.id.llTools;
            case SETTINGS: return R.id.llSettings;
            default: return 0;
        }
    }

    private static void moveIndicator(Activity activity, Tab current) {
        View indicator = activity.findViewById(R.id.navIndicator);
        if (indicator == null) return;
        int targetId = tabViewId(current);
        if (targetId == 0) {
            indicator.setVisibility(View.INVISIBLE);
            lastTab = current;
            return;
        }
        View target = activity.findViewById(targetId);
        int fromId = tabViewId(lastTab);
        View from = fromId == 0 ? null : activity.findViewById(fromId);
        lastTab = current;

        indicator.post(() -> {
            float targetX = xOf(target);
            indicator.setVisibility(View.VISIBLE);
            if (from != null && from != target) {
                indicator.setTranslationX(xOf(from));
                indicator.animate().translationX(targetX).setStartDelay(120).setDuration(420)
                        .setInterpolator(new OvershootInterpolator(1.2f)).start();
            } else {
                indicator.setTranslationX(targetX);
                indicator.setScaleX(0f);
                indicator.animate().scaleX(1f).setStartDelay(220).setDuration(380)
                        .setInterpolator(new OvershootInterpolator(2f)).start();
            }
        });
    }

    private static float xOf(View tab) {
        View row = (View) tab.getParent();
        return row.getLeft() + tab.getLeft();
    }
}
