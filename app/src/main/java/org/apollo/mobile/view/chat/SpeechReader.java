package org.apollo.mobile.view.chat;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import org.apollo.mobile.chat.SpeechText;

import java.util.List;
import java.util.Locale;

final class SpeechReader {
    private static final String LAST_SUFFIX = "last";
    private static final int MAX_CHUNK_LENGTH = 3000;

    interface Listener {
        void onFinished();
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Listener listener;
    private TextToSpeech engine;
    private boolean ready;
    private boolean speaking;
    private int generation;

    SpeechReader(Context context, Listener listener) {
        this.listener = listener;
        engine = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status != TextToSpeech.SUCCESS) {
                return;
            }
            int result = engine.setLanguage(new Locale("pt", "BR"));
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            engine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                }

                @Override
                public void onDone(String utteranceId) {
                    if (utteranceId != null && utteranceId.endsWith(":" + LAST_SUFFIX)) {
                        finish(utteranceId);
                    }
                }

                @Override
                public void onError(String utteranceId) {
                    finish(utteranceId);
                }

                @Override
                public void onStop(String utteranceId, boolean interrupted) {
                    if (interrupted) {
                        finish(utteranceId);
                    }
                }
            });
        });
    }

    boolean speak(String text) {
        if (!ready) {
            return false;
        }
        List<String> chunks = SpeechText.chunks(text, MAX_CHUNK_LENGTH);
        if (chunks.isEmpty()) {
            return false;
        }
        speaking = true;
        generation++;
        int current = generation;
        for (int index = 0; index < chunks.size(); index++) {
            boolean last = index == chunks.size() - 1;
            engine.speak(chunks.get(index),
                    index == 0 ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD,
                    null,
                    current + ":" + (last ? LAST_SUFFIX : String.valueOf(index)));
        }
        return true;
    }

    void stop() {
        speaking = false;
        engine.stop();
    }

    boolean isSpeaking() {
        return speaking;
    }

    void shutdown() {
        speaking = false;
        engine.stop();
        engine.shutdown();
    }

    private void finish(String utteranceId) {
        mainHandler.post(() -> {
            if (utteranceId != null && !utteranceId.startsWith(generation + ":")) {
                return;
            }
            speaking = false;
            listener.onFinished();
        });
    }
}
