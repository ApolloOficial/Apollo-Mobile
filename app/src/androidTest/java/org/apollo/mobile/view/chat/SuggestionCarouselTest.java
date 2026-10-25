package org.apollo.mobile.view.chat;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SuggestionCarouselTest {
    @Test
    public void keepsTwoTwoOneRowsAcrossPhoneWidthsAndFontScales() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Context base = InstrumentationRegistry.getInstrumentation().getTargetContext();
            String[] labels = {"Erro na placa", "Dúvida técnica", "Ativar placa nova",
                    "Como escanear uma placa?", "Ver histórico de manutenções"};
            for (float scale : new float[]{1f, 1.5f, 2f}) {
                Configuration config = new Configuration(base.getResources().getConfiguration());
                config.fontScale = scale;
                Context context = base.createConfigurationContext(config);
                for (int widthDp : new int[]{280, 320, 360, 411, 600}) {
                    LinearLayout group = new LinearLayout(context);
                    group.setOrientation(LinearLayout.VERTICAL);
                    new SuggestionCarousel(group, labels, ignored -> {});
                    int width = Math.round(widthDp * context.getResources().getDisplayMetrics().density);
                    group.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    group.layout(0, 0, width, group.getMeasuredHeight());
                    assertEquals(3, group.getChildCount());
                    for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
                        ViewGroup row = (ViewGroup) group.getChildAt(rowIndex);
                        assertEquals(rowIndex == 2 ? 1 : 2, row.getChildCount());
                        assertTrue(row.getRight() <= width);
                        for (int i = 0; i < row.getChildCount(); i++) {
                            TextView chip = (TextView) row.getChildAt(i);
                            assertTrue(chip.getWidth() > 0);
                            assertTrue(chip.getRight() <= row.getWidth());
                            assertNotNull(chip.getLayout());
                            assertEquals(chip.getText().length(),
                                    chip.getLayout().getLineEnd(chip.getLineCount() - 1));
                            assertTrue(chip.getHeight() >= chip.getLayout().getHeight()
                                    + chip.getCompoundPaddingTop() + chip.getCompoundPaddingBottom());
                        }
                    }
                }
            }
        });
    }
}
