package org.apollo.mobile.view.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apollo.mobile.R;
import org.apollo.mobile.chat.ChatSourcesFormatter;
import org.apollo.mobile.chat.GreetingName;
import org.apollo.mobile.chat.error.ChatError;
import org.apollo.mobile.chat.gateway.ChatGateway;
import org.apollo.mobile.chat.gateway.ChatGatewayFactory;
import org.apollo.mobile.chat.gateway.ChatReply;
import org.apollo.mobile.session.SessionManager;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.session.UserSession;
import org.apollo.mobile.view.auth.LoginActivity;

public final class ChatbotActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private ChatGateway chatGateway;
    private ChatAdapter adapter;
    private RecyclerView messageList;
    private View initialState;
    private View conversationInput;
    private EditText initialInput;
    private EditText conversationEditor;
    private SpeechReader speechReader;
    private SuggestionCarousel suggestionCarousel;
    private ChatMessage speakingMessage;
    private String sessionId;
    private boolean waitingReply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = SessionManagerFactory.create(getApplicationContext());
        if (!sessionManager.hasActiveSession()) {
            openLogin();
            return;
        }

        setContentView(R.layout.activity_chatbot);
        chatGateway = ChatGatewayFactory.create(this);

        applyInsets();
        bindViews();
        bindGreeting();
        bindActions();
        speechReader = new SpeechReader(this, this::onSpeechFinished);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (suggestionCarousel != null && initialState.getVisibility() == View.VISIBLE) {
            suggestionCarousel.start();
        }
    }

    @Override
    protected void onStop() {
        if (suggestionCarousel != null) {
            suggestionCarousel.stop();
        }
        stopSpeaking();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (speechReader != null) {
            speechReader.shutdown();
        }
        if (isFinishing() && sessionId != null && chatGateway != null) {
            chatGateway.close(sessionId, new ChatGateway.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                }

                @Override
                public void onFailure(ChatError error) {
                }
            });
        }
        super.onDestroy();
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(0, 0, 0, Math.max(bars.bottom, keyboard.bottom));
            return insets;
        });
    }

    private void bindViews() {
        initialState = findViewById(R.id.llInitialState);
        conversationInput = findViewById(R.id.cvConversationInput);
        initialInput = findViewById(R.id.etMessageInitial);
        conversationEditor = findViewById(R.id.etMessage);
        messageList = findViewById(R.id.rvChat);

        adapter = new ChatAdapter(new ChatAdapter.Actions() {
            @Override
            public void onCopy(ChatMessage message) {
                copyToClipboard(message.getText());
            }

            @Override
            public void onEdit(ChatMessage message) {
                editMessage(message.getText());
            }

            @Override
            public void onSpeak(ChatMessage message) {
                toggleSpeech(message);
            }
        });
        messageList.setLayoutManager(new LinearLayoutManager(this));
        messageList.setAdapter(adapter);
    }

    private void bindGreeting() {
        UserSession session = sessionManager.getActiveSession();
        String name = session == null ? null : GreetingName.firstName(session.getEmail());
        TextView greeting = findViewById(R.id.tvGreeting);
        greeting.setText(name == null
                ? getString(R.string.chat_greeting)
                : getString(R.string.chat_greeting_named, name));
    }

    private void bindActions() {
        findViewById(R.id.tvBack).setOnClickListener(view -> finish());

        findViewById(R.id.ibSendInitial).setOnClickListener(view -> submit(initialInput));
        findViewById(R.id.ibSend).setOnClickListener(view -> submit(conversationEditor));
        initialInput.setOnEditorActionListener((view, actionId, event) -> handleEditorAction(actionId, initialInput));
        conversationEditor.setOnEditorActionListener((view, actionId, event) ->
                handleEditorAction(actionId, conversationEditor));

        suggestionCarousel = new SuggestionCarousel(
                findViewById(R.id.chipSuggestion),
                findViewById(R.id.llSuggestionDots),
                new String[]{
                        getString(R.string.chat_chip_board_error),
                        getString(R.string.chat_chip_technical_question),
                        getString(R.string.chat_chip_activate_board),
                        getString(R.string.chat_chip_scan_board),
                        getString(R.string.chat_chip_maintenance_history)
                },
                this::sendMessage);
    }

    private boolean handleEditorAction(int actionId, EditText input) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            submit(input);
            return true;
        }
        return false;
    }

    private void submit(EditText input) {
        String text = input.getText().toString().trim();
        if (text.isEmpty() || waitingReply) {
            return;
        }
        input.setText("");
        sendMessage(text);
    }

    private void sendMessage(String text) {
        if (waitingReply) {
            return;
        }
        waitingReply = true;
        showConversation();
        adapter.add(ChatMessage.user(text));
        adapter.add(ChatMessage.typing());
        scrollToEnd();

        chatGateway.send(sessionId, text, new ChatGateway.Callback<ChatReply>() {
            @Override
            public void onSuccess(ChatReply reply) {
                if (isDestroyed()) {
                    return;
                }
                if (reply.getSessionId() != null && !reply.getSessionId().isEmpty()) {
                    sessionId = reply.getSessionId();
                }
                String answer = reply.getAnswer().trim();
                if (answer.isEmpty()) {
                    answer = getString(R.string.chat_empty_answer);
                }
                showBotMessage(answer, ChatSourcesFormatter.format(reply.getSources()));
            }

            @Override
            public void onFailure(ChatError error) {
                if (isDestroyed()) {
                    return;
                }
                if (error.getType() == ChatError.Type.SESSION_EXPIRED) {
                    sessionManager.logout();
                    openLogin();
                    return;
                }
                if (error.getType() == ChatError.Type.FORBIDDEN) {
                    sessionId = null;
                }
                showBotMessage(getString(ChatMessages.error(error)), "");
            }
        });
    }

    private void showBotMessage(String text, String sources) {
        adapter.removeTyping();
        adapter.add(ChatMessage.bot(text, sources));
        waitingReply = false;
        scrollToEnd();
    }

    private void showConversation() {
        suggestionCarousel.stop();
        initialState.setVisibility(View.GONE);
        messageList.setVisibility(View.VISIBLE);
        conversationInput.setVisibility(View.VISIBLE);
    }

    private void scrollToEnd() {
        int last = adapter.getItemCount() - 1;
        if (last >= 0) {
            messageList.scrollToPosition(last);
        }
    }

    private void editMessage(String text) {
        conversationEditor.setText(text);
        conversationEditor.setSelection(text.length());
        conversationEditor.requestFocus();
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.showSoftInput(conversationEditor, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void toggleSpeech(ChatMessage message) {
        boolean sameMessage = message == speakingMessage;
        if (speechReader.isSpeaking() && sameMessage) {
            stopSpeaking();
            return;
        }
        if (!speechReader.speak(message.getText())) {
            Toast.makeText(this, R.string.chat_speech_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        speakingMessage = message;
        adapter.setSpeaking(message);
    }

    private void stopSpeaking() {
        if (speechReader == null) {
            return;
        }
        speechReader.stop();
        onSpeechFinished();
    }

    private void onSpeechFinished() {
        speakingMessage = null;
        if (adapter != null) {
            adapter.setSpeaking(null);
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.chat_title), text));
        Toast.makeText(this, R.string.chat_copied, Toast.LENGTH_SHORT).show();
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
