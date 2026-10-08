package org.apollo.mobile.view.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.apollo.mobile.R;

import java.util.ArrayList;
import java.util.List;

final class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT = 1;
    private static final int TYPE_TYPING = 2;

    interface Actions {
        void onCopy(ChatMessage message);

        void onEdit(ChatMessage message);

        void onSpeak(ChatMessage message);
    }

    private final List<ChatMessage> messages = new ArrayList<>();
    private final Actions actions;
    private ChatMessage speaking;

    ChatAdapter(Actions actions) {
        this.actions = actions;
    }

    void setSpeaking(ChatMessage message) {
        ChatMessage previous = speaking;
        speaking = message;
        int previousIndex = previous == null ? -1 : messages.indexOf(previous);
        int currentIndex = message == null ? -1 : messages.indexOf(message);
        if (previousIndex >= 0) {
            notifyItemChanged(previousIndex);
        }
        if (currentIndex >= 0) {
            notifyItemChanged(currentIndex);
        }
    }

    void add(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    void removeTyping() {
        for (int index = messages.size() - 1; index >= 0; index--) {
            if (messages.get(index).getKind() == ChatMessage.Kind.TYPING) {
                messages.remove(index);
                notifyItemRemoved(index);
                return;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (messages.get(position).getKind()) {
            case USER:
                return TYPE_USER;
            case BOT:
                return TYPE_BOT;
            default:
                return TYPE_TYPING;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            return new UserHolder(inflater.inflate(R.layout.item_chat_user, parent, false));
        }
        if (viewType == TYPE_BOT) {
            return new BotHolder(inflater.inflate(R.layout.item_chat_bot, parent, false));
        }
        return new TypingHolder(inflater.inflate(R.layout.item_chat_typing, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserHolder) {
            ((UserHolder) holder).bind(message);
        } else if (holder instanceof BotHolder) {
            ((BotHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private final class UserHolder extends RecyclerView.ViewHolder {
        private final TextView text;
        private final View copy;
        private final View edit;

        UserHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tvUserMessage);
            copy = itemView.findViewById(R.id.ivCopyUser);
            edit = itemView.findViewById(R.id.ivEditUser);
        }

        void bind(ChatMessage message) {
            text.setText(message.getText());
            copy.setOnClickListener(view -> actions.onCopy(message));
            edit.setOnClickListener(view -> actions.onEdit(message));
        }
    }

    private final class BotHolder extends RecyclerView.ViewHolder {
        private final TextView text;
        private final TextView sources;
        private final View copy;
        private final ImageView speak;

        BotHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tvBotMessage);
            sources = itemView.findViewById(R.id.tvBotSources);
            copy = itemView.findViewById(R.id.ivCopyBot);
            speak = itemView.findViewById(R.id.ivSpeakBot);
        }

        void bind(ChatMessage message) {
            text.setText(message.getText());
            boolean hasSources = !message.getSources().isEmpty();
            sources.setVisibility(hasSources ? View.VISIBLE : View.GONE);
            if (hasSources) {
                sources.setText(itemView.getContext().getString(R.string.chat_sources, message.getSources()));
            }
            copy.setOnClickListener(view -> actions.onCopy(message));
            speak.setOnClickListener(view -> actions.onSpeak(message));
            int tint = message == speaking ? R.color.secondary_three : R.color.neutral_gray;
            speak.setColorFilter(ContextCompat.getColor(itemView.getContext(), tint));
        }
    }

    private static final class TypingHolder extends RecyclerView.ViewHolder {
        TypingHolder(View itemView) {
            super(itemView);
        }
    }
}
