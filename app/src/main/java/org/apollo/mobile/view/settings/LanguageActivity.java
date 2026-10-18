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
    private static boolean announce;
    private View skeleton;
    private View options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gateway = SettingsGatewayFactory.create(getApplicationContext());
        bindUi();
        load();
    }

    @Override
    public void onConfigurationChanged(@androidx.annotation.NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bindUi();
        select(selected);
        skeleton.setVisibility(View.GONE);
        options.setVisibility(View.VISIBLE);
        if (announce) {
            announce = false;
            org.apollo.mobile.view.common.AppToast.success(this, R.string.settings_language_saved);
        }
    }

    private void bindUi() {
        setContentView(R.layout.activity_language);
        Motion.enterScreen(this);
        portuguese = findViewById(R.id.rbPortuguese);
        spanish = findViewById(R.id.rbSpanish);
        english = findViewById(R.id.rbEnglish);
        skeleton = findViewById(R.id.skeletonLanguage);
        options = findViewById(R.id.llLanguageOptions);

        findViewById(R.id.tvBack).setOnClickListener(view -> finish());
        bindOption(portuguese, R.id.ivBrazil, PORTUGUESE);
        bindOption(spanish, R.id.ivSpain, SPANISH);
        bindOption(english, R.id.ivEnglish, ENGLISH);
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
                showOptions();
                if (announce) {
                    announce = false;
                    org.apollo.mobile.view.common.AppToast.success(LanguageActivity.this, R.string.settings_language_saved);
                }
                apply(selected);
            }

            @Override
            public void onFailure(SettingsError error) {
                if (isFinishing() || isDestroyed()) return;
                showOptions();
                if (announce) {
                    announce = false;
                    org.apollo.mobile.view.common.AppToast.success(LanguageActivity.this, R.string.settings_language_saved);
                }
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
        showSkeleton();
        gateway.updateLanguage(code, new SettingsGateway.Callback<LanguageSettings>() {
            @Override
            public void onSuccess(LanguageSettings settings) {
                busy = false;
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                String applied = settings.getSelected() == null ? code : settings.getSelected();
                select(applied);
                if (sameAsApp(applied)) {
                    showOptions();
                    org.apollo.mobile.view.common.AppToast.success(LanguageActivity.this, R.string.settings_language_saved);
                } else {
                    announce = true;
                    apply(applied);
                }
            }

            @Override
            public void onFailure(SettingsError error) {
                busy = false;
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                select(previous);
                showOptions();
                org.apollo.mobile.view.common.AppToast.error(LanguageActivity.this, R.string.settings_language_failed);
            }
        });
    }

    private void showSkeleton() {
        options.setVisibility(View.GONE);
        skeleton.setAlpha(1f);
        skeleton.setVisibility(View.VISIBLE);
    }

    private void showOptions() {
        org.apollo.mobile.view.common.SkeletonView.hide(skeleton);
        if (options.getVisibility() == View.VISIBLE) return;
        options.setAlpha(0f);
        options.setVisibility(View.VISIBLE);
        options.animate().alpha(1f).setDuration(320).start();
    }

    private static boolean sameAsApp(String code) {
        androidx.core.os.LocaleListCompat current = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales();
        if (current.isEmpty()) return false;
        String tag = current.toLanguageTags();
        return tag.toLowerCase(java.util.Locale.ROOT).startsWith(code.toLowerCase(java.util.Locale.ROOT).substring(0, 2));
    }

    private static void apply(String code) {
        if (code == null || sameAsApp(code)) return;
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                androidx.core.os.LocaleListCompat.forLanguageTags(code));
    }
}
