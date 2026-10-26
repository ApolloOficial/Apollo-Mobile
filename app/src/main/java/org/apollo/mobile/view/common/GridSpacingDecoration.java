package org.apollo.mobile.view.common;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public final class GridSpacingDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public GridSpacingDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
            @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (spacing == 0 || !(parent.getLayoutManager() instanceof GridLayoutManager)) return;
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) return;
        GridLayoutManager manager = (GridLayoutManager) parent.getLayoutManager();
        int spanCount = manager.getSpanCount();
        int column = manager.getSpanSizeLookup().getSpanIndex(position, spanCount);
        int start = column * spacing / spanCount;
        int end = spacing - (column + 1) * spacing / spanCount;
        if (parent.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            outRect.left = end;
            outRect.right = start;
        } else {
            outRect.left = start;
            outRect.right = end;
        }
    }
}
