package org.apollo.mobile.view.maintenance.os;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apollo.mobile.R;
import org.apollo.mobile.maintenance.OsVerificationStore;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Details;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Panel;
import org.apollo.mobile.view.common.Motion;

import java.util.List;
import java.util.Map;

final class OsSummary {

    private OsSummary() {
    }

    static boolean bind(View card, Details details, Map<Long, OsVerificationStore.Result> results,
                        boolean unidentified) {
        List<Panel> targets = details.getTargetPanels();
        LinearLayout list = card.findViewById(R.id.llProblemPlates);
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(card.getContext());
        int verified = 0;
        int problems = 0;
        for (Panel panel : targets) {
            OsVerificationStore.Result result = results.get(panel.getId());
            if (result == null) continue;
            verified++;
            if (result.hasProblem()) problems++;
            View row = inflater.inflate(R.layout.item_summary_plate, list, false);
            int position = details.positionOf(panel);
            String string = ServiceOrderUi.safe(panel.getStringCode());
            ((TextView) row.findViewById(R.id.tvSummaryPlate)).setText(
                    ServiceOrderUi.safe(panel.getSerialNumber()) + " (" + string
                            + (position > 0 ? ", Posição " + position : "") + ")");
            TextView condition = row.findViewById(R.id.tvSummaryCondition);
            condition.setText(result.hasProblem() ? result.getProblem() : "Sem problema aparente.");
            View chevron = row.findViewById(R.id.ivSummaryChevron);
            row.findViewById(R.id.llSummaryHeader).setOnClickListener(v -> {
                boolean expand = condition.getVisibility() != View.VISIBLE;
                condition.setVisibility(expand ? View.VISIBLE : View.GONE);
                chevron.animate().rotation(expand ? 90f : 0f).setDuration(320).start();
                if (expand) Motion.fadeUp(condition, 0);
            });
            list.addView(row);
        }

        TextView verifiedTag = card.findViewById(R.id.tvVerifiedTag);
        if (verified == 0 && unidentified) {
            verifiedTag.setText("Placa não identificada");
        } else {
            verifiedTag.setText(verified + " de " + targets.size() + " verificadas");
        }
        TextView problemTag = card.findViewById(R.id.tvProblemTag);
        problemTag.setText(problems == 1 ? "1 com problema" : problems + " com problema");
        problemTag.setVisibility(problems > 0 ? View.VISIBLE : View.GONE);
        list.setVisibility(verified > 0 ? View.VISIBLE : View.GONE);
        return verified > 0 || unidentified;
    }
}
