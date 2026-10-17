package org.apollo.mobile.view.common;

import android.view.View;
import android.widget.TextView;

import org.apollo.mobile.R;

public final class EmptyState {

    private final View root;
    private final TextView title;
    private final TextView message;

    public EmptyState(View root) {
        this.root = root;
        this.title = root.findViewById(R.id.tvEmptyTitle);
        this.message = root.findViewById(R.id.tvEmptyMessage);
    }

    public void setText(String text) {
        boolean error = text != null && text.startsWith("Não foi possível");
        title.setText(error ? "Algo deu errado" : "Nenhuma OS por aqui");
        message.setText(error ? text + " Tente novamente em instantes."
                : "Quando uma ordem de serviço for atribuída a você, ela aparece aqui.");
    }

    public void show(String titleText, String messageText) {
        title.setText(titleText);
        message.setText(messageText);
        setVisibility(View.VISIBLE);
    }

    public void setVisibility(int visibility) {
        boolean appearing = visibility == View.VISIBLE && root.getVisibility() != View.VISIBLE;
        root.setVisibility(visibility);
        if (appearing) Motion.fadeUp(root, 0);
    }
}
