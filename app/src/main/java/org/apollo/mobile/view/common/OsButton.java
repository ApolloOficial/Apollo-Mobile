package org.apollo.mobile.view.common;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import org.apollo.mobile.R;
import org.apollo.mobile.view.maintenance.os.OsDetailsActivity;

public final class OsButton {

    private OsButton() {
    }

    public static void bind(TextView button, Long orderId) {
        Context context = button.getContext();
        boolean hasOrder = orderId != null && orderId > 0;
        button.setText(hasOrder ? org.apollo.mobile.view.common.Tr.t("Ver OS") : org.apollo.mobile.view.common.Tr.t("Aguardando OS"));
        button.setBackgroundResource(hasOrder ? R.drawable.bg_cta_outline : R.drawable.bg_cta_disabled);
        button.setTextColor(context.getColor(hasOrder ? R.color.secondary_one : R.color.neutral_gray));
        button.setEnabled(hasOrder);
        button.setClickable(hasOrder);
        button.setOnClickListener(hasOrder ? v -> {
            Intent intent = new Intent(context, OsDetailsActivity.class);
            intent.putExtra(OsDetailsActivity.EXTRA_ORDER_ID, orderId.longValue());
            context.startActivity(intent);
        } : null);
    }
}
