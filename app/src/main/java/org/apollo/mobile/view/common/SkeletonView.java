package org.apollo.mobile.view.common;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public final class SkeletonView extends View {

    private final Paint block = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint card = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();
    private final RectF rect = new RectF();
    private final float density;
    private int rows = 4;
    private float shift;
    private ValueAnimator animator;
    private LinearGradient shader;

    public SkeletonView(Context context) {
        this(context, null);
    }

    public SkeletonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        card.setColor(0xFFFFFEFC);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.max});
            rows = a.getInt(0, rows);
            a.recycle();
        }
    }

    public static void hide(View view) {
        if (view == null || view.getVisibility() != VISIBLE) return;
        view.animate().alpha(0f).setDuration(320).withEndAction(() -> {
            view.setVisibility(GONE);
            view.setAlpha(1f);
        }).start();
    }

    private float rowHeight() {
        return 104 * density;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        int height = (int) (rows * rowHeight() + 8 * density) + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, resolveSize(height, heightSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        shader = new LinearGradient(0, 0, w * 0.6f, 0,
                new int[]{0xFFEDE9E4, 0xFFF8F6F2, 0xFFEDE9E4}, new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
        block.setShader(shader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shader == null) return;
        float w = getWidth();
        matrix.setTranslate(-w * 0.6f + shift * w * 1.6f, 0);
        shader.setLocalMatrix(matrix);
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        w -= getPaddingLeft() + getPaddingRight();
        float pad = 4 * density;
        float r = 16 * density;
        for (int i = 0; i < rows; i++) {
            float top = i * rowHeight() + pad;
            rect.set(pad, top, w - pad, top + rowHeight() - 12 * density);
            canvas.drawRoundRect(rect, r, r, card);
            float x = rect.left + 16 * density;
            float y = rect.top + 16 * density;
            bar(canvas, x, y, (w - 2 * pad) * 0.45f, 12);
            bar(canvas, x, y + 24 * density, (w - 2 * pad) * 0.3f, 18);
            bar(canvas, x, y + 52 * density, (w - 2 * pad) * 0.22f, 10);
            float bw = 96 * density;
            rect.set(w - pad - 16 * density - bw, y + 48 * density, w - pad - 16 * density, y + 66 * density);
            canvas.drawRoundRect(rect, 9 * density, 9 * density, block);
        }
        canvas.restore();
    }

    private void bar(Canvas canvas, float x, float y, float width, float heightDp) {
        float h = heightDp * density;
        canvas.drawRoundRect(x, y, x + width, y + h, h / 2, h / 2, block);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (isShown()) start();
        else stop();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isShown()) start();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    private void start() {
        if (animator != null) return;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1600);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            shift = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private void stop() {
        if (animator == null) return;
        animator.cancel();
        animator = null;
    }
}
