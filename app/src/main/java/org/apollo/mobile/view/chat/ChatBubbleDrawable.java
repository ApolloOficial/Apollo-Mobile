package org.apollo.mobile.view.chat;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

final class ChatBubbleDrawable extends Drawable {

    static final float TAIL_DP = 6f;
    static final float TAIL_W_DP = 6f;

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final boolean tailRight;
    private final float density;

    ChatBubbleDrawable(float density, int fillColor, int strokeColor, boolean tailRight) {
        this.density = density;
        this.tailRight = tailRight;
        fill.setColor(fillColor);
        stroke.setColor(strokeColor);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(0.7f * density);
        stroke.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        build(bounds);
    }

    private void build(Rect b) {
        float d = density;
        float half = stroke.getStrokeWidth() / 2f;
        float tail = TAIL_DP * d;
        float side = TAIL_W_DP * d;
        RectF r = new RectF(b.left + half + (tailRight ? 0 : side), b.top + half,
                b.right - half - (tailRight ? side : 0), b.bottom - tail - half);
        float radius = Math.min(24 * d, Math.min(r.width(), r.height()) / 2f);
        path.reset();
        if (!tailRight) {
            path.moveTo(r.left + radius, r.top);
            path.lineTo(r.right - radius, r.top);
            path.arcTo(new RectF(r.right - 2 * radius, r.top, r.right, r.top + 2 * radius), -90, 90);
            path.lineTo(r.right, r.bottom - radius);
            path.arcTo(new RectF(r.right - 2 * radius, r.bottom - 2 * radius, r.right, r.bottom), 0, 90);
            path.lineTo(r.left + radius, r.bottom);
            path.quadTo(r.left + radius * 0.6f, r.bottom, r.left + radius * 0.4f, r.bottom - radius * 0.08f);
            path.lineTo(b.left + half, b.bottom - half);
            path.lineTo(r.left + 3f * d, r.bottom - radius * 0.62f);
            path.quadTo(r.left, r.bottom - radius * 0.86f, r.left, r.bottom - radius);
            path.lineTo(r.left, r.top + radius);
            path.arcTo(new RectF(r.left, r.top, r.left + 2 * radius, r.top + 2 * radius), 180, 90);
        } else {
            path.moveTo(r.left + radius, r.top);
            path.lineTo(r.right - radius, r.top);
            path.arcTo(new RectF(r.right - 2 * radius, r.top, r.right, r.top + 2 * radius), -90, 90);
            path.lineTo(r.right, r.bottom - radius);
            path.quadTo(r.right, r.bottom - radius * 0.8f, r.right - 2.6f * d, r.bottom - radius * 0.55f);
            path.lineTo(b.right - half, b.bottom - half);
            path.lineTo(r.right - radius * 0.4f, r.bottom - radius * 0.1f);
            path.quadTo(r.right - radius * 0.6f, r.bottom, r.right - radius, r.bottom);
            path.lineTo(r.left + radius, r.bottom);
            path.arcTo(new RectF(r.left, r.bottom - 2 * radius, r.left + 2 * radius, r.bottom), 90, 90);
            path.lineTo(r.left, r.top + radius);
            path.arcTo(new RectF(r.left, r.top, r.left + 2 * radius, r.top + 2 * radius), 180, 90);
        }
        path.close();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawPath(path, fill);
        canvas.drawPath(path, stroke);
    }

    @Override
    public void setAlpha(int alpha) {
        fill.setAlpha(alpha);
        stroke.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        fill.setColorFilter(colorFilter);
        stroke.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
