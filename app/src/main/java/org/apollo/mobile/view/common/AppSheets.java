package org.apollo.mobile.view.common;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.apollo.mobile.R;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.view.auth.LoginActivity;

public final class AppSheets {

    private static boolean sessionSheetVisible;
    private static boolean updateSheetVisible;

    private AppSheets() {
    }

    public static BottomSheetDialog create(Activity activity, int layout) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = activity.getLayoutInflater().inflate(layout, null, false);
        dialog.setContentView(view);
        View parent = (View) view.getParent();
        if (parent != null) parent.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.getBehavior().setSkipCollapsed(true);
        return dialog;
    }

    public static android.app.Dialog centered(Activity activity, int layout) {
        android.app.Dialog dialog = new android.app.Dialog(activity);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            int side = Math.round(20 * activity.getResources().getDisplayMetrics().density);
            window.getDecorView().setPadding(side, 0, side, 0);
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setWindowAnimations(android.R.style.Animation_Dialog);
        }
        return dialog;
    }

    public static android.app.Dialog feedback(Activity activity, int illustration, CharSequence title,
                                             CharSequence message, CharSequence primary, Runnable onPrimary,
                                             CharSequence secondary, Runnable onSecondary, Runnable onClose) {
        if (activity.isFinishing() || activity.isDestroyed()) return null;
        android.app.Dialog dialog = centered(activity, R.layout.sheet_feedback);
        View root = dialog.findViewById(R.id.ivFeedbackIllustration).getRootView();
        ((ImageView) root.findViewById(R.id.ivFeedbackIllustration)).setImageResource(illustration);
        ((TextView) root.findViewById(R.id.tvFeedbackTitle)).setText(title);
        ((TextView) root.findViewById(R.id.tvFeedbackMessage)).setText(message);
        TextView primaryButton = root.findViewById(R.id.btnFeedbackPrimary);
        TextView secondaryButton = root.findViewById(R.id.btnFeedbackSecondary);
        primaryButton.setText(primary);
        boolean[] handled = {false};
        primaryButton.setOnClickListener(v -> {
            handled[0] = true;
            dialog.dismiss();
            if (onPrimary != null) onPrimary.run();
        });
        if (secondary != null) {
            secondaryButton.setVisibility(View.VISIBLE);
            secondaryButton.setText(secondary);
            secondaryButton.setOnClickListener(v -> {
                handled[0] = true;
                dialog.dismiss();
                if (onSecondary != null) onSecondary.run();
            });
        } else {
            View spacer = new View(activity);
            ((android.widget.LinearLayout) primaryButton.getParent()).addView(spacer, 0,
                    new android.widget.LinearLayout.LayoutParams(0, 1, 1f));
        }
        root.findViewById(R.id.btnFeedbackClose).setOnClickListener(v -> dialog.dismiss());
        dialog.setOnDismissListener(d -> {
            if (!handled[0] && onClose != null) onClose.run();
        });
        Motion.popIn(root.findViewById(R.id.ivFeedbackIllustration), 80);
        Motion.pressable(primaryButton, secondaryButton);
        dialog.show();
        return dialog;
    }

    public static android.app.Dialog message(Activity activity, CharSequence title, CharSequence body,
                                            CharSequence primary, Runnable onPrimary,
                                            CharSequence secondary, Runnable onSecondary) {
        if (activity.isFinishing() || activity.isDestroyed()) return null;
        android.app.Dialog dialog = centered(activity, R.layout.sheet_message);
        ((TextView) dialog.findViewById(R.id.tvMessageTitle)).setText(title);
        ((TextView) dialog.findViewById(R.id.tvMessageBody)).setText(body);
        TextView primaryButton = dialog.findViewById(R.id.btnMessagePrimary);
        TextView secondaryButton = dialog.findViewById(R.id.btnMessageSecondary);
        primaryButton.setText(primary);
        primaryButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPrimary != null) onPrimary.run();
        });
        if (secondary != null) {
            secondaryButton.setVisibility(View.VISIBLE);
            secondaryButton.setText(secondary);
            secondaryButton.setOnClickListener(v -> {
                dialog.dismiss();
                if (onSecondary != null) onSecondary.run();
            });
        }
        dialog.findViewById(R.id.btnMessageClose).setOnClickListener(v -> dialog.cancel());
        Motion.pressable(primaryButton, secondaryButton);
        dialog.show();
        return dialog;
    }

    public static void plateNotFound(Activity activity, Runnable retry, Runnable close) {
        feedback(activity, R.drawable.ill_not_found, org.apollo.mobile.view.common.Tr.t("Placa não encontrada"),
                org.apollo.mobile.view.common.Tr.t("Confira o código e tente novamente."), org.apollo.mobile.view.common.Tr.t("Tentar novamente"), retry, null, null, close);
    }

    public static void activationFailed(Activity activity, Runnable retry) {
        feedback(activity, R.drawable.ill_error, org.apollo.mobile.view.common.Tr.t("Não foi possível\nativar a placa."),
                org.apollo.mobile.view.common.Tr.t("Verifique os dados e tente novamente."), org.apollo.mobile.view.common.Tr.t("Tentar novamente"), retry, null, null, null);
    }

    public static void completionFailed(Activity activity, Runnable retry) {
        feedback(activity, R.drawable.ill_error, org.apollo.mobile.view.common.Tr.t("Não foi possível\nconcluir a OS."),
                org.apollo.mobile.view.common.Tr.t("Seus dados estão seguros, tente novamente em alguns instantes."),
                org.apollo.mobile.view.common.Tr.t("Tentar novamente"), retry, null, null, null);
    }

    public static void cameraPermission(Activity activity, Runnable allow, Runnable deny) {
        feedback(activity, R.drawable.ill_camera, org.apollo.mobile.view.common.Tr.t("Permita o acesso à câmera"),
                org.apollo.mobile.view.common.Tr.t("O Apollo usa a câmera do seu celular pra escanear códigos em campo. Sem essa permissão, ")
                        + org.apollo.mobile.view.common.Tr.t("algumas funções não vão funcionar direito."),
                org.apollo.mobile.view.common.Tr.t("Permitir"), allow, org.apollo.mobile.view.common.Tr.t("Agora não"), deny, deny);
    }

    public static void updateRequired(Activity activity) {
        if (updateSheetVisible || activity.isFinishing() || activity.isDestroyed()) return;
        updateSheetVisible = true;
        android.app.Dialog dialog = feedback(activity, R.drawable.ill_update, org.apollo.mobile.view.common.Tr.t("Atualize o Apollo pra continuar"),
                org.apollo.mobile.view.common.Tr.t("Essa versão do app não é mais compatível. Atualize pra voltar a usar."),
                org.apollo.mobile.view.common.Tr.t("Atualizar agora"), () -> openStore(activity), null, null, null);
        if (dialog == null) {
            updateSheetVisible = false;
            return;
        }
        dialog.setCancelable(false);
        dialog.findViewById(R.id.btnFeedbackClose).setVisibility(View.GONE);
        dialog.setOnDismissListener(d -> updateSheetVisible = false);
    }

    public static void sessionExpired(Activity activity) {
        SessionManagerFactory.create(activity.getApplicationContext()).logout();
        if (sessionSheetVisible || activity.isFinishing() || activity.isDestroyed()) {
            if (!sessionSheetVisible) goToLogin(activity);
            return;
        }
        sessionSheetVisible = true;
        BottomSheetDialog dialog = create(activity, R.layout.sheet_session_expired);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        View button = dialog.findViewById(R.id.btnSessionLogin);
        Motion.pressable(button);
        button.setOnClickListener(v -> {
            dialog.dismiss();
            goToLogin(activity);
        });
        dialog.setOnDismissListener(d -> sessionSheetVisible = false);
        dialog.show();
    }

    private static void goToLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static void openStore(Activity activity) {
        String pkg = activity.getPackageName();
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)));
        } catch (android.content.ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)));
        }
        updateSheetVisible = false;
    }
}
