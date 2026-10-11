package org.apollo.mobile.settings.gateway;

import java.util.Collections;
import java.util.List;

public final class LanguageSettings {
    private String selected;
    private List<LanguageOption> options;

    public String getSelected() {
        return selected;
    }

    public List<LanguageOption> getOptions() {
        return options == null ? Collections.emptyList() : options;
    }
}
