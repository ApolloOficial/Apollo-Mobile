package org.apollo.mobile.view.common;

import android.content.Context;
import android.content.res.Configuration;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.apollo.mobile.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class TabletResponsiveResourcesTest {
    private Context context(int widthDp) {
        Context base = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        configuration.smallestScreenWidthDp = widthDp;
        configuration.screenWidthDp = widthDp;
        configuration.screenHeightDp = Math.round(widthDp * 1.5f);
        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        return base.createConfigurationContext(configuration);
    }

    private float dp(Context context, int dimension) {
        return context.getResources().getDimension(dimension)
                / context.getResources().getDisplayMetrics().density;
    }

    @Test
    public void tabletKeepsComponentProportionsWithWiderSearch() {
        for (int width : new int[]{600, 800, 1024}) {
            Context context = context(width);
            assertEquals(32f, dp(context, R.dimen.screen_horizontal_padding), 0.01f);
            assertEquals(32f, dp(context, R.dimen.list_horizontal_padding), 0.01f);
            assertEquals(16f, dp(context, R.dimen.list_column_spacing), 0.01f);
            assertEquals(2, context.getResources().getInteger(R.integer.list_columns));
            assertEquals(width >= 840 ? 400f : 320f,
                    dp(context, R.dimen.search_pill_width), 0.01f);
            assertEquals(dp(context, R.dimen.space_36),
                    dp(context, R.dimen.search_pill_height), 0.01f);
            assertEquals(dp(context, R.dimen.space_40),
                    dp(context, R.dimen.panel_search_height), 0.01f);
            assertEquals(dp(context, R.dimen.text_12),
                    dp(context, R.dimen.search_pill_text_size), 0.01f);
            android.content.res.TypedArray chip = context.obtainStyledAttributes(
                    R.style.apollo_filter_chip, new int[]{android.R.attr.layout_height});
            try {
                assertEquals(context.getResources().getDimensionPixelSize(R.dimen.space_30),
                        chip.getLayoutDimension(0, -1));
            } finally {
                chip.recycle();
            }
        }
    }

    @Test
    public void phoneRetainsItsExistingSearchAndSingleColumn() {
        Context context = context(360);
        assertEquals(dp(context, R.dimen.space_200),
                dp(context, R.dimen.search_pill_width), 0.01f);
        assertEquals(dp(context, R.dimen.space_36),
                dp(context, R.dimen.search_pill_height), 0.01f);
        assertEquals(0f, dp(context, R.dimen.list_column_spacing), 0.01f);
        assertEquals(1, context.getResources().getInteger(R.integer.list_columns));
    }
}
