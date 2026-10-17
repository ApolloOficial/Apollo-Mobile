package org.apollo.mobile.view.scanner;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.apollo.mobile.R;

public final class ScannerOverlayView extends View {

    private final Paint dim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint clear = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint corner = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint success = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF frame = new RectF();
    private final RectF target = new RectF();
    private final Path path = new Path();
    private final float density;
    private float progress;
    private float pulse = 1f;
    private ValueAnimator pulseAnimator;

    public ScannerOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        setLayerType(LAYER_TYPE_HARDWARE, null);
        dim.setColor(Color.argb(140, 0, 0, 0));
        clear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        corner.setColor(ContextCompat.getColor(context, R.color.secondary_three));
        corner.setStyle(Paint.Style.STROKE);
        corner.setStrokeCap(Paint.Cap.ROUND);
        corner.setStrokeWidth(8 * density);
        success.setColor(ContextCompat.getColor(context, R.color.sucess_two));
        success.setStyle(Paint.Style.STROKE);
        success.setStrokeWidth(6 * density);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        pulseAnimator = ValueAnimator.ofFloat(1f, 1.03f);
        pulseAnimator.setDuration(900);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.addUpdateListener(a -> {
            pulse = (float) a.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (pulseAnimator != null) pulseAnimator.cancel();
        super.onDetachedFromWindow();
    }

    public RectF frameRect() {
        return new RectF(frame);
    }

    public void showSuccess(Runnable done) {
        if (pulseAnimator != null) pulseAnimator.cancel();
        pulse = 1f;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(420);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                postDelayed(done, 450);
            }
        });
        animator.start();
    }

    public void reset() {
        progress = 0f;
        if (pulseAnimator != null && !pulseAnimator.isRunning()) pulseAnimator.start();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float scale = Math.min(1.3f, w / (402f * density));
        float fw = Math.min(296 * density * scale, w - 64 * density);
        float fh = 150 * density * scale;
        float left = (w - fw) / 2f;
        float top = h * 0.44f;
        frame.set(left, top, left + fw, top + fh);
        float tw = fw * (288f / 359f);
        float th = fh * (89f / 191f);
        float tl = (w - tw) / 2f;
        float tt = frame.centerY() - th / 2f;
        target.set(tl, tt, tl + tw, tt + th);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF r = new RectF(
                lerp(frame.left, target.left), lerp(frame.top, target.top),
                lerp(frame.right, target.right), lerp(frame.bottom, target.bottom));
        float cx = r.centerX();
        float cy = r.centerY();
        float hw = r.width() / 2f * pulse;
        float hh = r.height() / 2f * pulse;
        r.set(cx - hw, cy - hh, cx + hw, cy + hh);
        float radius = 24 * density;
        canvas.drawRect(0, 0, getWidth(), getHeight(), dim);
        canvas.drawRoundRect(r, radius, radius, clear);
        if (progress > 0f) {
            success.setAlpha((int) (255 * progress));
            canvas.drawRoundRect(r, radius, radius, success);
        }
        if (progress < 1f) {
            corner.setAlpha((int) (255 * (1f - progress)));
            float arm = Math.min(r.width(), r.height()) * 0.28f;
            float inset = -3 * density;
            RectF c = new RectF(r.left + inset, r.top + inset, r.right - inset, r.bottom - inset);
            drawCorner(canvas, c.left, c.top, arm, 1, 1, radius);
            drawCorner(canvas, c.right, c.top, arm, -1, 1, radius);
            drawCorner(canvas, c.left, c.bottom, arm, 1, -1, radius);
            drawCorner(canvas, c.right, c.bottom, arm, -1, -1, radius);
        }
    }

    private void drawCorner(Canvas canvas, float x, float y, float arm, int sx, int sy, float radius) {
        path.reset();
        path.moveTo(x, y + sy * arm);
        path.lineTo(x, y + sy * radius);
        path.quadTo(x, y, x + sx * radius, y);
        path.lineTo(x + sx * arm, y);
        canvas.drawPath(path, corner);
    }

    private float lerp(float a, float b) {
        return a + (b - a) * progress;
    }
}
