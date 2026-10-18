package org.apollo.mobile.view.settings;

import android.os.Bundle;
import org.apollo.mobile.view.common.Motion;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.settings.error.SettingsError;
import org.apollo.mobile.settings.gateway.LanguageSettings;
import org.apollo.mobile.settings.gateway.SettingsGateway;
import org.apollo.mobile.settings.gateway.SettingsGatewayFactory;

public final class LanguageActivity extends AppCompatActivity {
    private static final String PORTUGUESE = "pt-BR";
    private static final String SPANISH = "es";
    private static final String ENGLISH = "en";

    private SettingsGateway gateway;
    private RadioButton portuguese;
    private RadioButton spanish;
    private RadioButton english;
    private String selected = PORTUGUESE;
    private boolean busy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        Motion.enterScreen(this);

        gateway = SettingsGatewayFactory.create(getApplicationContext());
        portuguese = findViewById(R.id.rbPortuguese);
        spanish = findViewById(R.id.rbSpanish);
        english = findViewById(R.id.rbEnglish);

        findViewById(R.id.tvBack).setOnClickListener(view -> finish());
        bindOption(portuguese, R.id.ivBrazil, PORTUGUESE);
        bindOption(spanish, R.id.ivSpain, SPANISH);
        bindOption(english, R.id.ivEnglish, ENGLISH);

        load();
    }

    private void bindOption(RadioButton button, int flagId, String code) {
        button.setOnClickListener(view -> choose(code));
        findViewById(flagId).setOnClickListener(view -> choose(code));
        View row = (View) button.getParent();
        row.setOnClickListener(view -> choose(code));
    }

    private void load() {
        gateway.language(new SettingsGateway.Callback<LanguageSettings>() {
            @Override
            public void onSuccess(LanguageSettings settings) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                select(settings.getSelected());
            }

            @Override
            public void onFailure(SettingsError error) {
                SettingsUi.handleFailure(LanguageActivity.this, error);
            }
        });
    }

    private void select(String code) {
        selected = code == null ? PORTUGUESE : code;
        portuguese.setChecked(PORTUGUESE.equalsIgnoreCase(selected));
        spanish.setChecked(SPANISH.equalsIgnoreCase(selected));
        english.setChecked(ENGLISH.equalsIgnoreCase(selected));
    }

    private void choose(String code) {
        if (busy || code.equalsIgnoreCase(selected)) {
            select(selected);
            return;
        }
        busy = true;
        String previous = selected;
        select(code);
        gateway.updateLanguage(code, new SettingsGateway.Callback<LanguageSettings>() {
            @Override
            public void onSuccess(LanguageSettings settings) {
                busy = false;
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                select(settings.getSelected() == null ? code : settings.getSelected());
                org.apollo.mobile.view.common.AppToast.success(LanguageActivity.this, R.string.settings_language_saved);
            }

            @Override
            public void onFailure(SettingsError error) {
                busy = false;
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                select(previous);
                SettingsUi.handleFailure(LanguageActivity.this, error);
            }
        });
    }
}
