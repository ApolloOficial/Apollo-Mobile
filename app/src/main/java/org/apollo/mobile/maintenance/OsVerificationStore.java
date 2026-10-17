package org.apollo.mobile.maintenance;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OsVerificationStore {

    private static final String PREFS = "os_verification";
    private static final String UNIDENTIFIED = "unidentified";
    private static final String PANELS = "panels";

    public static final class Result {
        private final String serial;
        private final String problem;

        Result(String serial, String problem) {
            this.serial = serial;
            this.problem = problem;
        }

        public String getSerial() {
            return serial;
        }

        public String getProblem() {
            return problem;
        }

        public boolean hasProblem() {
            return problem != null && !problem.trim().isEmpty();
        }
    }

    private final SharedPreferences prefs;

    public OsVerificationStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void verify(long orderId, long panelId, String serial, String problem) {
        JSONObject root = read(orderId);
        try {
            JSONObject panels = root.optJSONObject(PANELS);
            if (panels == null) {
                panels = new JSONObject();
                root.put(PANELS, panels);
            }
            JSONObject entry = new JSONObject();
            entry.put("serial", serial == null ? "" : serial);
            if (problem != null && !problem.trim().isEmpty()) entry.put("problem", problem.trim());
            panels.put(String.valueOf(panelId), entry);
            write(orderId, root);
        } catch (JSONException ignored) {
        }
    }

    public void markUnidentified(long orderId, boolean value) {
        JSONObject root = read(orderId);
        try {
            root.put(UNIDENTIFIED, value);
            write(orderId, root);
        } catch (JSONException ignored) {
        }
    }

    public void setPendingCondition(long orderId, long panelId) {
        JSONObject root = read(orderId);
        try {
            root.put("pending", panelId);
            write(orderId, root);
        } catch (JSONException ignored) {
        }
    }

    public long takePendingCondition(long orderId) {
        JSONObject root = read(orderId);
        long value = root.optLong("pending", -1L);
        if (value > 0) {
            root.remove("pending");
            write(orderId, root);
        }
        return value;
    }

    public boolean isUnidentified(long orderId) {
        return read(orderId).optBoolean(UNIDENTIFIED, false);
    }

    public Map<Long, Result> results(long orderId) {
        Map<Long, Result> map = new LinkedHashMap<>();
        JSONObject panels = read(orderId).optJSONObject(PANELS);
        if (panels == null) return map;
        Iterator<String> keys = panels.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject entry = panels.optJSONObject(key);
            if (entry == null) continue;
            try {
                map.put(Long.parseLong(key), new Result(entry.optString("serial", ""),
                        entry.has("problem") ? entry.optString("problem") : null));
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }

    public void clear(long orderId) {
        prefs.edit().remove(key(orderId)).apply();
    }

    private JSONObject read(long orderId) {
        String raw = prefs.getString(key(orderId), null);
        if (raw == null) return new JSONObject();
        try {
            return new JSONObject(raw);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private void write(long orderId, JSONObject root) {
        prefs.edit().putString(key(orderId), root.toString()).apply();
    }

    private static String key(long orderId) {
        return "order_" + orderId;
    }
}
