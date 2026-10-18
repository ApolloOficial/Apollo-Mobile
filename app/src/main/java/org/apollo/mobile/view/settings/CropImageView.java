package org.apollo.mobile.view.settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

public final class CropImageView extends View {

    private final Matrix matrix = new Matrix();
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF crop = new RectF();
    private final RectF mapped = new RectF();
    private final Path hole = new Path();
    private final ScaleGestureDetector scaleDetector;
    private Bitmap bitmap;
    private int rotation;
    private float lastX;
    private float lastY;

    public CropImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        dimPaint.setColor(0x99000000);
        ringPaint.setColor(Color.WHITE);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(2 * getResources().getDisplayMetrics().density);
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(),
                        detector.getFocusX(), detector.getFocusY());
                constrain();
                invalidate();
                return true;
            }
        });
    }

    public void setBitmap(Bitmap value) {
        bitmap = value;
        rotation = 0;
        reset();
    }

    public void rotate() {
        if (bitmap == null) return;
        rotation = (rotation + 90) % 360;
        matrix.postRotate(90, crop.centerX(), crop.centerY());
        constrain();
        invalidate();
    }

    public Bitmap cropped(int size) {
        if (bitmap == null || crop.isEmpty()) return null;
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(Color.WHITE);
        float scale = size / crop.width();
        canvas.scale(scale, scale);
        canvas.translate(-crop.left, -crop.top);
        canvas.drawBitmap(bitmap, matrix, bitmapPaint);
        return output;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float margin = 24 * getResources().getDisplayMetrics().density;
        float side = Math.min(w, h) - margin * 2;
        crop.set((w - side) / 2f, (h - side) / 2f, (w + side) / 2f, (h + side) / 2f);
        hole.reset();
        hole.setFillType(Path.FillType.EVEN_ODD);
        hole.addRect(0, 0, w, h, Path.Direction.CW);
        hole.addCircle(crop.centerX(), crop.centerY(), side / 2f, Path.Direction.CCW);
        reset();
    }

    private void reset() {
        matrix.reset();
        if (bitmap == null || crop.isEmpty()) {
            invalidate();
            return;
        }
        float scale = Math.max(crop.width() / bitmap.getWidth(), crop.height() / bitmap.getHeight());
        matrix.postScale(scale, scale);
        matrix.postTranslate(crop.centerX() - bitmap.getWidth() * scale / 2f,
                crop.centerY() - bitmap.getHeight() * scale / 2f);
        invalidate();
    }

    private void constrain() {
        if (bitmap == null) return;
        mapped.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        matrix.mapRect(mapped);
        float minScale = Math.max(crop.width() / mapped.width(), crop.height() / mapped.height());
        if (minScale > 1f) {
            matrix.postScale(minScale, minScale, crop.centerX(), crop.centerY());
            mapped.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            matrix.mapRect(mapped);
        }
        float dx = 0f;
        float dy = 0f;
        if (mapped.left > crop.left) dx = crop.left - mapped.left;
        if (mapped.right < crop.right) dx = crop.right - mapped.right;
        if (mapped.top > crop.top) dy = crop.top - mapped.top;
        if (mapped.bottom < crop.bottom) dy = crop.bottom - mapped.bottom;
        matrix.postTranslate(dx, dy);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress() && event.getPointerCount() == 1) {
                    matrix.postTranslate(event.getX() - lastX, event.getY() - lastY);
                    constrain();
                    invalidate();
                }
                lastX = event.getX();
                lastY = event.getY();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                int index = event.getActionIndex() == 0 ? 1 : 0;
                lastX = event.getX(index);
                lastY = event.getY(index);
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null) canvas.drawBitmap(bitmap, matrix, bitmapPaint);
        canvas.drawPath(hole, dimPaint);
        canvas.drawCircle(crop.centerX(), crop.centerY(), crop.width() / 2f, ringPaint);
    }
}
