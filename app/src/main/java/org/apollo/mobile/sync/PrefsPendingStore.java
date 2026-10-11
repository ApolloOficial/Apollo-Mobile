package org.apollo.mobile.sync;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class PrefsPendingStore implements PendingStore {
    private static final String FILE = "apollo_pending_actions";
    private static final String KEY = "actions";
    private static final Type LIST_TYPE = new TypeToken<List<PendingAction>>() {
    }.getType();

    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public PrefsPendingStore(Context context) {
        this.preferences = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    @Override
    public synchronized List<PendingAction> all() {
        String json = preferences.getString(KEY, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<PendingAction> list = gson.fromJson(json, LIST_TYPE);
            return list == null ? new ArrayList<>() : list;
        } catch (RuntimeException exception) {
            return new ArrayList<>();
        }
    }

    @Override
    public synchronized void add(PendingAction action) {
        List<PendingAction> list = all();
        list.add(action);
        save(list);
    }

    @Override
    public synchronized void update(PendingAction action) {
        List<PendingAction> list = all();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(action.getId())) {
                list.set(i, action);
                break;
            }
        }
        save(list);
    }

    @Override
    public synchronized void remove(String id) {
        List<PendingAction> list = all();
        list.removeIf(item -> item.getId().equals(id));
        save(list);
    }

    private void save(List<PendingAction> list) {
        preferences.edit().putString(KEY, gson.toJson(list, LIST_TYPE)).commit();
    }
}
