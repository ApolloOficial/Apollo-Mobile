package org.apollo.mobile.view.scanner;

import android.app.Activity;
import android.content.Intent;

import org.apollo.mobile.panels.error.PanelsError;
import org.apollo.mobile.panels.gateway.PanelItem;
import org.apollo.mobile.panels.gateway.PanelStringItem;
import org.apollo.mobile.panels.gateway.PanelsGateway;
import org.apollo.mobile.view.panels.PanelDetailsActivity;

import java.util.List;

public final class ScannedPanelRouter {

    private ScannedPanelRouter() {
    }

    public static void open(Activity activity, PanelItem panel) {
        if ("EM_ESTOQUE".equals(panel.getOperatingStats())) {
            activity.startActivity(PanelActivationActivity.intent(activity, panel));
        } else {
            activity.startActivity(PanelIdentifiedActivity.intent(activity, panel));
        }
    }

    static void openString(Activity activity, String stringId, String stringCode, String serial,
                           PanelsGateway gateway, boolean finishAfter) {
        if (stringId == null) {
            if (finishAfter) activity.finish();
            return;
        }
        gateway.strings(new PanelsGateway.Callback<List<PanelStringItem>>() {
            @Override
            public void onSuccess(List<PanelStringItem> strings) {
                double power = 0d;
                if (strings != null) {
                    for (PanelStringItem item : strings) {
                        if (stringId.equals(item.getId())) {
                            power = item.getNominalPowerKwp();
                            break;
                        }
                    }
                }
                start(activity, stringId, stringCode, serial, power, finishAfter);
            }

            @Override
            public void onFailure(PanelsError error) {
                start(activity, stringId, stringCode, serial, 0d, finishAfter);
            }
        });
    }

    private static void start(Activity activity, String stringId, String stringCode, String serial,
                              double power, boolean finishAfter) {
        if (activity.isDestroyed()) return;
        Intent intent = new Intent(activity, PanelDetailsActivity.class);
        intent.putExtra(PanelDetailsActivity.EXTRA_STRING_ID, stringId);
        intent.putExtra(PanelDetailsActivity.EXTRA_STRING_CODE, stringCode);
        intent.putExtra(PanelDetailsActivity.EXTRA_NOMINAL_POWER, power);
        intent.putExtra(PanelDetailsActivity.EXTRA_QUERY, serial);
        activity.startActivity(intent);
        if (finishAfter) activity.finish();
    }
}
