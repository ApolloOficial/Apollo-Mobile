package org.apollo.mobile.view.maintenance.os;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.apollo.mobile.R;
import org.apollo.mobile.view.common.Motion;

final class ConditionSheet {

    interface Listener {
        void onRegistered(String problem);
    }

    private ConditionSheet() {
    }

    static void show(Activity activity, String serial, String current, Listener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View sheet = activity.getLayoutInflater().inflate(R.layout.layout_register_condition, null, false);
        dialog.setContentView(sheet);
        View parent = (View) sheet.getParent();
        if (parent != null) parent.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        TextView serialView = sheet.findViewById(R.id.tvSerial);
        serialView.setText(ServiceOrderUi.safe(serial));
        org.apollo.mobile.view.common.CopySerial.attach(serialView);
        EditText condition = sheet.findViewById(R.id.etCondition);
        if (current != null) condition.setText(current);
        sheet.findViewById(R.id.ivClose).setOnClickListener(v -> dialog.dismiss());
        sheet.findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String text = condition.getText().toString().trim();
            if (text.isEmpty()) {
                condition.setError("Descreva a condição encontrada.");
                Motion.shake(condition);
                return;
            }
            dialog.dismiss();
            listener.onRegistered(text);
        });
        sheet.findViewById(R.id.btnNoProblem).setOnClickListener(v -> {
            dialog.dismiss();
            listener.onRegistered(null);
        });
        dialog.show();
    }
}
