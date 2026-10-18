package org.apollo.mobile.view.common;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apollo.mobile.R;

public final class EmptyState {

    public enum Kind {
        ORDERS(R.drawable.empty_orders, "Nenhuma OS encontrada."),
        PANELS(R.drawable.empty_panels, "Nenhuma placa encontrada."),
        STRINGS(R.drawable.empty_strings, "Nenhuma string encontrada.");

        final int illustration;
        final String message;

        Kind(int illustration, String message) {
            this.illustration = illustration;
            this.message = message;
        }
    }

    private final View root;
    private final ImageView illustration;
    private final TextView title;
    private final TextView message;
    private final TextView retry;
    private final TextView support;
    private Kind kind = Kind.ORDERS;
    private Runnable retryAction;

    public EmptyState(View root) {
        this.root = root;
        this.illustration = root.findViewById(R.id.ivEmptyIllustration);
        this.title = root.findViewById(R.id.tvEmptyTitle);
        this.message = root.findViewById(R.id.tvEmptyMessage);
        this.retry = root.findViewById(R.id.btnEmptyRetry);
        this.support = root.findViewById(R.id.btnEmptySupport);
        support.setOnClickListener(v -> openSupport());
        retry.setOnClickListener(v -> {
            if (retryAction != null) retryAction.run();
        });
        Motion.pressable(retry, support);
    }

    public EmptyState kind(Kind value) {
        kind = value;
        return this;
    }

    public EmptyState onRetry(Runnable action) {
        retryAction = action;
        return this;
    }

    public void setText(String text) {
        if (text != null && text.startsWith("Não foi possível")) {
            error();
        } else {
            empty(kind, null);
        }
    }

    public void empty(Kind value, String text) {
        kind = value;
        illustration.setImageResource(value.illustration);
        title.setVisibility(View.GONE);
        message.setText(text == null ? value.message : text);
        retry.setVisibility(View.GONE);
        support.setVisibility(View.GONE);
        setVisibility(View.VISIBLE);
    }

    public void error() {
        fullScreen(false);
    }

    public void timeout() {
        fullScreen(true);
    }

    private void fullScreen(boolean timeout) {
        root.setVisibility(View.GONE);
        Context context = root.getContext();
        while (context instanceof ContextWrapper && !(context instanceof Activity)) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (context instanceof Activity) {
            ErrorStateActivity.open((Activity) context, timeout, retryAction);
        }
    }

    public void show(String titleText, String messageText) {
        state(R.drawable.state_error, titleText, messageText);
    }

    private void state(int image, String titleText, String messageText) {
        illustration.setImageResource(image);
        title.setText(titleText);
        title.setVisibility(View.VISIBLE);
        message.setText(messageText);
        retry.setVisibility(retryAction != null ? View.VISIBLE : View.GONE);
        support.setVisibility(View.VISIBLE);
        setVisibility(View.VISIBLE);
    }

    public void setVisibility(int visibility) {
        boolean appearing = visibility == View.VISIBLE && root.getVisibility() != View.VISIBLE;
        root.setVisibility(visibility);
        if (appearing) Motion.fadeUp(root, 0);
    }

    private void openSupport() {
        Context context = root.getContext();
        while (context instanceof ContextWrapper && !(context instanceof Activity)) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        Intent intent = new Intent(root.getContext(), org.apollo.mobile.view.chat.ChatbotActivity.class);
        if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        root.getContext().startActivity(intent);
    }
}
