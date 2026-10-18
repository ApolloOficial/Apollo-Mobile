package org.apollo.mobile.view.maintenance.os;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

final class ServiceOrderUi {
    private ServiceOrderUi() {
    }

    static String status(String value) {
        if (value == null) return "-";
        switch (value) {
            case "ABERTA": return org.apollo.mobile.view.common.Tr.t("Aberta");
            case "EM_ANDAMENTO": return org.apollo.mobile.view.common.Tr.t("Em andamento");
            case "CONCLUIDA": return org.apollo.mobile.view.common.Tr.t("Concluída");
            case "CANCELADA": return org.apollo.mobile.view.common.Tr.t("Cancelada");
            default: return value;
        }
    }

    static String priority(String value) {
        if (value == null) return "-";
        switch (value) {
            case "BAIXA": return org.apollo.mobile.view.common.Tr.t("Baixa");
            case "MEDIA": return org.apollo.mobile.view.common.Tr.t("Média");
            case "ALTA": return org.apollo.mobile.view.common.Tr.t("Alta");
            case "CRITICA": return org.apollo.mobile.view.common.Tr.t("Crítica");
            default: return value;
        }
    }

    static String date(String value) {
        if (value == null || value.trim().isEmpty()) return "-";
        try {
            return LocalDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException ignored) {
            return value.length() >= 10 ? value.substring(0, 10) : value;
        }
    }

    static CharSequence boldLabel(android.content.Context context, String text) {
        android.text.SpannableString span = new android.text.SpannableString(text);
        int colon = text.indexOf(':');
        if (colon > 0) {
            span.setSpan(new android.text.style.TypefaceSpan(
                    androidx.core.content.res.ResourcesCompat.getFont(context, org.apollo.mobile.R.font.manrope_bold)),
                    0, colon + 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}
