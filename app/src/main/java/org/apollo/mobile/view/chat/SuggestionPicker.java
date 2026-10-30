package org.apollo.mobile.view.chat;

import android.content.Context;
import android.content.SharedPreferences;

import org.apollo.mobile.R;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class SuggestionPicker {
    private static final String PREFS = "chat_suggestions";
    private static final String KEY_RECENT_ITEMS = "recent_items";
    private static final String KEY_RECENT_COMBOS = "recent_combos";
    private static final String ITEM_SEPARATOR = ",";
    private static final String COMBO_SEPARATOR = ";";
    private static final int RECENT_ROUNDS = 2;
    private static final int MAX_COMBOS = 200;
    private static final int MAX_ATTEMPTS = 60;
    private static final int[] THEMES = {
            R.array.chat_suggestions_diagnosis,
            R.array.chat_suggestions_maintenance,
            R.array.chat_suggestions_panels,
            R.array.chat_suggestions_app
    };

    private final SecureRandom random = new SecureRandom();
    private final SharedPreferences prefs;
    private final String[][] themes;

    SuggestionPicker(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        themes = new String[THEMES.length][];
        for (int i = 0; i < THEMES.length; i++) {
            themes[i] = context.getResources().getStringArray(THEMES[i]);
        }
    }

    String[] pick(int count) {
        List<String> recentItems = split(prefs.getString(KEY_RECENT_ITEMS, ""), ITEM_SEPARATOR);
        List<String> recentCombos = split(prefs.getString(KEY_RECENT_COMBOS, ""), COMBO_SEPARATOR);
        Set<String> blockedItems = new HashSet<>(recentItems);
        Set<String> blockedCombos = new HashSet<>(recentCombos);
        List<String> chosen = draw(count, blockedItems);
        for (int attempt = 0; attempt < MAX_ATTEMPTS && blockedCombos.contains(key(chosen)); attempt++) {
            chosen = draw(count, blockedItems);
        }
        remember(chosen, recentItems, recentCombos, count);
        String[] texts = new String[chosen.size()];
        for (int i = 0; i < chosen.size(); i++) {
            texts[i] = text(chosen.get(i));
        }
        return texts;
    }

    private List<String> draw(int count, Set<String> blocked) {
        List<List<String>> pools = new ArrayList<>();
        for (int t = 0; t < themes.length; t++) {
            List<String> fresh = new ArrayList<>();
            List<String> all = new ArrayList<>();
            for (int i = 0; i < themes[t].length; i++) {
                String id = t + ":" + i;
                all.add(id);
                if (!blocked.contains(id)) fresh.add(id);
            }
            List<String> pool = fresh.isEmpty() ? all : fresh;
            Collections.shuffle(pool, random);
            pools.add(pool);
        }
        List<Integer> order = new ArrayList<>();
        for (int t = 0; t < themes.length; t++) order.add(t);
        Collections.shuffle(order, random);

        Set<String> seenTexts = new HashSet<>();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        int[] cursor = new int[themes.length];
        boolean progressed = true;
        while (result.size() < count && progressed) {
            progressed = false;
            for (int t : order) {
                if (result.size() >= count) break;
                List<String> pool = pools.get(t);
                while (cursor[t] < pool.size()) {
                    String id = pool.get(cursor[t]++);
                    if (seenTexts.add(text(id).trim().toLowerCase(Locale.ROOT))) {
                        result.add(id);
                        progressed = true;
                        break;
                    }
                }
            }
        }
        return new ArrayList<>(result);
    }

    private String text(String id) {
        int colon = id.indexOf(':');
        return themes[Integer.parseInt(id.substring(0, colon))][Integer.parseInt(id.substring(colon + 1))];
    }

    private void remember(List<String> chosen, List<String> recentItems, List<String> recentCombos, int count) {
        List<String> items = new ArrayList<>(chosen);
        items.addAll(recentItems);
        if (items.size() > count * RECENT_ROUNDS) items = items.subList(0, count * RECENT_ROUNDS);
        List<String> combos = new ArrayList<>();
        combos.add(key(chosen));
        combos.addAll(recentCombos);
        if (combos.size() > MAX_COMBOS) combos = combos.subList(0, MAX_COMBOS);
        prefs.edit()
                .putString(KEY_RECENT_ITEMS, String.join(ITEM_SEPARATOR, items))
                .putString(KEY_RECENT_COMBOS, String.join(COMBO_SEPARATOR, combos))
                .apply();
    }

    private static String key(List<String> ids) {
        String[] sorted = ids.toArray(new String[0]);
        Arrays.sort(sorted);
        return String.join(ITEM_SEPARATOR, sorted);
    }

    private static List<String> split(String value, String separator) {
        List<String> out = new ArrayList<>();
        if (value == null || value.isEmpty()) return out;
        for (String part : value.split(separator)) {
            if (!part.isEmpty()) out.add(part);
        }
        return out;
    }
}
