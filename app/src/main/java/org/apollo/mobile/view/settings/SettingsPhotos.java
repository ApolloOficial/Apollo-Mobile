package org.apollo.mobile.view.settings;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;


import org.apollo.mobile.R;

import org.apollo.mobile.settings.format.PhotoSizing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

final class SettingsPhotos {
    static final String MIME_JPEG = "image/jpeg";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private SettingsPhotos() {
    }

    static void prepareAsync(ContentResolver resolver, Uri uri, Consumer<byte[]> callback) {
        EXECUTOR.execute(() -> {
            byte[] data = prepareForUpload(resolver, uri);
            MAIN.post(() -> callback.accept(data));
        });
    }

    static void showMenu(Activity activity, boolean hasPhoto, Runnable pick, Runnable remove) {
        String[] labels = hasPhoto
                ? new String[]{
                        activity.getString(R.string.settings_photo_choose),
                        activity.getString(R.string.settings_photo_remove)}
                : new String[]{activity.getString(R.string.settings_photo_choose)};
        if (labels.length > 1) {
            org.apollo.mobile.view.common.AppSheets.message(activity, activity.getString(R.string.settings_photo_title),
                    org.apollo.mobile.view.common.Tr.t("Escolha uma nova foto de perfil ou remova a atual."), labels[0], pick, labels[1], remove);
        } else {
            org.apollo.mobile.view.common.AppSheets.message(activity, activity.getString(R.string.settings_photo_title),
                    org.apollo.mobile.view.common.Tr.t("Escolha uma foto da galeria para o seu perfil."), labels[0], pick, null, null);
        }
    }

    private static byte[] prepareForUpload(ContentResolver resolver, Uri uri) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream input = resolver.openInputStream(uri)) {
                if (input == null) {
                    return null;
                }
                BitmapFactory.decodeStream(input, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = PhotoSizing.sampleSize(bounds.outWidth, bounds.outHeight, PhotoSizing.MAX_EDGE);
            Bitmap decoded;
            try (InputStream input = resolver.openInputStream(uri)) {
                if (input == null) {
                    return null;
                }
                decoded = BitmapFactory.decodeStream(input, null, options);
            }
            if (decoded == null) {
                return null;
            }

            int[] size = PhotoSizing.scaledSize(decoded.getWidth(), decoded.getHeight(), PhotoSizing.MAX_EDGE);
            Bitmap scaled = size[0] == decoded.getWidth() && size[1] == decoded.getHeight()
                    ? decoded
                    : Bitmap.createScaledBitmap(decoded, size[0], size[1], true);

            int quality = PhotoSizing.START_QUALITY;
            while (quality > 0) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, output);
                byte[] data = output.toByteArray();
                if (PhotoSizing.fits(data.length)) {
                    return data;
                }
                quality = PhotoSizing.nextQuality(quality);
            }
            return null;
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }

    static Bitmap decode(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
}
