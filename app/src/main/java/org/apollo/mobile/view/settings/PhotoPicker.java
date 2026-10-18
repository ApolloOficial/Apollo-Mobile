package org.apollo.mobile.view.settings;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.apollo.mobile.R;
import org.apollo.mobile.settings.format.PhotoSizing;
import org.apollo.mobile.view.common.AppSheets;
import org.apollo.mobile.view.common.AppToast;
import org.apollo.mobile.view.common.Motion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

final class PhotoPicker {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private final AppCompatActivity activity;
    private final Consumer<Uri> onReady;
    private final ActivityResultLauncher<String> gallery;
    private final ActivityResultLauncher<Uri> camera;
    private final ActivityResultLauncher<String> permission;
    private Uri cameraUri;

    PhotoPicker(AppCompatActivity activity, Consumer<Uri> onReady) {
        this.activity = activity;
        this.onReady = onReady;
        gallery = activity.registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) edit(uri);
        });
        camera = activity.registerForActivityResult(new ActivityResultContracts.TakePicture(), saved -> {
            if (Boolean.TRUE.equals(saved) && cameraUri != null) edit(cameraUri);
        });
        permission = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                openCamera();
            } else {
                AppToast.error(activity, "Permita o acesso à câmera para tirar a foto.");
            }
        });
    }

    void showMenu(boolean hasPhoto, Runnable remove) {
        Dialog dialog = AppSheets.centered(activity, R.layout.dialog_photo_options);
        dialog.findViewById(R.id.btnPhotoClose).setOnClickListener(v -> dialog.dismiss());
        View galleryButton = dialog.findViewById(R.id.btnPhotoGallery);
        View cameraButton = dialog.findViewById(R.id.btnPhotoCamera);
        View removeButton = dialog.findViewById(R.id.btnPhotoRemove);
        removeButton.setVisibility(hasPhoto ? View.VISIBLE : View.GONE);
        galleryButton.setOnClickListener(v -> {
            dialog.dismiss();
            gallery.launch("image/*");
        });
        cameraButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                permission.launch(Manifest.permission.CAMERA);
            }
        });
        removeButton.setOnClickListener(v -> {
            dialog.dismiss();
            remove.run();
        });
        Motion.pressable(galleryButton, cameraButton);
        dialog.show();
    }

    private void openCamera() {
        try {
            File dir = new File(activity.getCacheDir(), "photos");
            if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException();
            File file = new File(dir, "camera.jpg");
            cameraUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".photos", file);
            camera.launch(cameraUri);
        } catch (RuntimeException e) {
            AppToast.error(activity, "Não foi possível abrir a câmera.");
        }
    }

    private void edit(Uri uri) {
        EXECUTOR.execute(() -> {
            Bitmap bitmap = decode(uri);
            MAIN.post(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                if (bitmap == null) {
                    AppToast.error(activity, R.string.settings_photo_invalid);
                    return;
                }
                showCrop(bitmap);
            });
        });
    }

    private void showCrop(Bitmap bitmap) {
        Dialog dialog = AppSheets.centered(activity, R.layout.dialog_crop);
        CropImageView cropView = dialog.findViewById(R.id.cropView);
        cropView.setBitmap(bitmap);
        dialog.findViewById(R.id.btnCropClose).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnCropCancel).setOnClickListener(v -> dialog.dismiss());
        View rotate = dialog.findViewById(R.id.btnCropRotate);
        rotate.setOnClickListener(v -> {
            cropView.rotate();
            v.animate().rotationBy(90f).setDuration(220).start();
        });
        View save = dialog.findViewById(R.id.btnCropSave);
        Motion.pressable(save);
        save.setOnClickListener(v -> {
            Bitmap result = cropView.cropped(PhotoSizing.MAX_EDGE);
            dialog.dismiss();
            if (result == null) {
                AppToast.error(activity, R.string.settings_photo_invalid);
                return;
            }
            EXECUTOR.execute(() -> {
                Uri saved = save(result);
                MAIN.post(() -> {
                    if (saved == null) {
                        AppToast.error(activity, R.string.settings_photo_invalid);
                    } else {
                        onReady.accept(saved);
                    }
                });
            });
        });
        dialog.show();
    }

    private Bitmap decode(Uri uri) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream input = activity.getContentResolver().openInputStream(uri)) {
                if (input == null) return null;
                BitmapFactory.decodeStream(input, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = PhotoSizing.sampleSize(bounds.outWidth, bounds.outHeight, 1600);
            try (InputStream input = activity.getContentResolver().openInputStream(uri)) {
                if (input == null) return null;
                return BitmapFactory.decodeStream(input, null, options);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Uri save(Bitmap bitmap) {
        try {
            File dir = new File(activity.getCacheDir(), "photos");
            if (!dir.exists() && !dir.mkdirs()) return null;
            File file = new File(dir, "cropped.jpg");
            try (FileOutputStream output = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output);
            }
            return Uri.fromFile(file);
        } catch (Exception e) {
            return null;
        }
    }
}
