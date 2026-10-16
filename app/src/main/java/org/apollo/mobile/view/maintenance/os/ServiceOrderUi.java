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
            case "ABERTA": return "Aberta";
            case "EM_ANDAMENTO": return "Em andamento";
            case "CONCLUIDA": return "Concluída";
            case "CANCELADA": return "Cancelada";
            default: return value;
        }
    }

    static String priority(String value) {
        if (value == null) return "-";
        switch (value) {
            case "BAIXA": return "Baixa";
            case "MEDIA": return "Média";
            case "ALTA": return "Alta";
            case "CRITICA": return "Crítica";
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

    static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}
