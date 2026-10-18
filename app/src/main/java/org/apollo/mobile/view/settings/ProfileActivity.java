package org.apollo.mobile.view.settings;

import android.graphics.Bitmap;
import org.apollo.mobile.view.common.Motion;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.settings.error.SettingsError;
import org.apollo.mobile.settings.gateway.ProfileInfo;
import org.apollo.mobile.settings.gateway.SettingsGateway;
import org.apollo.mobile.settings.gateway.SettingsGatewayFactory;

public final class ProfileActivity extends AppCompatActivity {
    private SettingsGateway gateway;
    private ImageView photoView;
    private boolean hasPhoto;

    private final PhotoPicker photoPicker = new PhotoPicker(this, this::onPhotoPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Motion.enterScreen(this);

        gateway = SettingsGatewayFactory.create(getApplicationContext());
        photoView = findViewById(R.id.iv_profile_large);

        findViewById(R.id.tvBack).setOnClickListener(view -> finish());
        findViewById(R.id.btn_change_photo).setOnClickListener(view -> showPhotoMenu());
        photoView.setOnClickListener(view -> showPhotoMenu());
        ((TextView) findViewById(R.id.tv_info_footer)).setText(getText(R.string.settings_profile_footer));
        TextView changePhoto = findViewById(R.id.btn_change_photo);
        changePhoto.setPaintFlags(changePhoto.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);

        load();
    }

    private void load() {
        gateway.profile(new SettingsGateway.Callback<ProfileInfo>() {
            @Override
            public void onSuccess(ProfileInfo profile) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setText(R.id.tv_name_value, profile.getFullName());
                setText(R.id.tv_email_value, profile.getEmail());
                setText(R.id.tv_company_value, companyLine(profile));
                setText(R.id.tv_role_value, profile.getRoleLabel());
                hasPhoto = profile.hasPhoto();
                loadPhoto();
            }

            @Override
            public void onFailure(SettingsError error) {
                SettingsUi.handleFailure(ProfileActivity.this, error);
            }
        });
    }

    private String companyLine(ProfileInfo profile) {
        String company = profile.getCompanyName();
        String unit = profile.getUnitName();
        boolean hasCompany = company != null && !company.trim().isEmpty();
        boolean hasUnit = unit != null && !unit.trim().isEmpty();
        if (hasCompany && hasUnit) {
            return company.trim() + " · " + unit.trim();
        }
        if (hasCompany) {
            return company.trim();
        }
        return hasUnit ? unit.trim() : null;
    }

    private void setText(int id, String value) {
        TextView view = findViewById(id);
        view.setText(value == null || value.trim().isEmpty()
                ? getString(R.string.settings_value_empty)
                : value.trim());
    }

    private void loadPhoto() {
        if (!hasPhoto) {
            photoView.setImageResource(R.drawable.profile_picture);
            return;
        }
        gateway.photo(new SettingsGateway.Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Bitmap bitmap = SettingsPhotos.decode(data);
                if (bitmap != null) {
                    photoView.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onFailure(SettingsError error) {
                if (error.getType() == SettingsError.Type.SESSION_EXPIRED) {
                    SettingsUi.handleFailure(ProfileActivity.this, error);
                }
            }
        });
    }

    private void showPhotoMenu() {
        photoPicker.showMenu(hasPhoto, this::removePhoto);
    }

    private void onPhotoPicked(Uri uri) {
        if (uri == null) {
            return;
        }
        SettingsPhotos.prepareAsync(getContentResolver(), uri, data -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (data == null) {
                org.apollo.mobile.view.common.AppToast.error(this, R.string.settings_photo_invalid);
                return;
            }
            gateway.uploadPhoto(data, SettingsPhotos.MIME_JPEG, new SettingsGateway.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    org.apollo.mobile.view.common.AppToast.success(ProfileActivity.this, R.string.settings_photo_updated);
                    hasPhoto = true;
                    loadPhoto();
                }

                @Override
                public void onFailure(SettingsError error) {
                    SettingsUi.handleFailure(ProfileActivity.this, error);
                }
            });
        });
    }

    private void removePhoto() {
        gateway.removePhoto(new SettingsGateway.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                org.apollo.mobile.view.common.AppToast.success(ProfileActivity.this, R.string.settings_photo_removed);
                hasPhoto = false;
                loadPhoto();
            }

            @Override
            public void onFailure(SettingsError error) {
                SettingsUi.handleFailure(ProfileActivity.this, error);
            }
        });
    }
}
