package org.apollo.mobile.tracking.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.gson.Gson;

import org.apollo.mobile.tracking.gateway.TrackingEvent;

import java.util.ArrayList;
import java.util.List;

public final class TrackingStore extends SQLiteOpenHelper {
    private final Gson gson = new Gson();

    public TrackingStore(Context context) {
        super(context.getApplicationContext(), "apollo_tracking.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE events (event_id TEXT PRIMARY KEY, owner TEXT NOT NULL, "
                + "json TEXT NOT NULL, state INTEGER NOT NULL DEFAULT 0, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX events_pending ON events(owner, state, created_at)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("Tracking migration required");
    }

    public void insert(String owner, TrackingEvent event) {
        ContentValues values = new ContentValues();
        values.put("event_id", event.eventId);
        values.put("owner", owner);
        values.put("json", gson.toJson(event));
        values.put("created_at", System.currentTimeMillis());
        long result = getWritableDatabase().insertWithOnConflict("events", null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
        if (result == -1) {
            try (Cursor cursor = getReadableDatabase().rawQuery(
                    "SELECT 1 FROM events WHERE event_id=?", new String[]{event.eventId})) {
                if (!cursor.moveToFirst()) throw new IllegalStateException("Tracking insert failed");
            }
        }
    }

    public List<TrackingEvent> pending(String owner) {
        List<TrackingEvent> events = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("events", new String[]{"json"},
                "owner=? AND state=0", new String[]{owner}, null, null, "created_at, event_id", "50")) {
            while (cursor.moveToNext()) events.add(gson.fromJson(cursor.getString(0), TrackingEvent.class));
        }
        return events;
    }

    public void acknowledge(String owner, List<String> ids) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (String id : ids) db.delete("events", "owner=? AND event_id=?", new String[]{owner, id});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void quarantine(String owner, List<TrackingEvent> events) {
        ContentValues values = new ContentValues();
        values.put("state", 1);
        for (TrackingEvent event : events) getWritableDatabase().update("events", values,
                "owner=? AND event_id=?", new String[]{owner, event.eventId});
    }
}