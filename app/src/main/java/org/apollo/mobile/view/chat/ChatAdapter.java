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
    private int lastAnimatedPosition = -1;

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
                if (lastAnimatedPosition >= index) lastAnimatedPosition = index - 1;
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
        int adapterPosition = holder.getBindingAdapterPosition();
        if (adapterPosition == RecyclerView.NO_POSITION) return;

        ChatMessage message = messages.get(adapterPosition);
        if (holder instanceof UserHolder) {
            ((UserHolder) holder).bind(message);
        } else if (holder instanceof BotHolder) {
            ((BotHolder) holder).bind(message);
        }
        if (adapterPosition > lastAnimatedPosition) {
            lastAnimatedPosition = adapterPosition;
            animateIn(holder);
        } else {
            resetPosition(holder.itemView);
        }
    }

    private static void resetPosition(View view) {
        view.animate().cancel();
        view.setAlpha(1f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
    }

    private void animateIn(RecyclerView.ViewHolder holder) {
        View view = holder.itemView;
        float density = view.getResources().getDisplayMetrics().density;
        float dx = holder instanceof UserHolder ? 24 * density : -24 * density;
        view.setAlpha(0f);
        view.setTranslationX(dx);
        view.setTranslationY(12 * density);
        view.animate().alpha(1f).translationX(0f).translationY(0f).setDuration(320)
                .setInterpolator(new android.view.animation.DecelerateInterpolator(1.8f))
                .setListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(android.animation.Animator animation) {
                        view.setAlpha(1f);
                        view.setTranslationX(0f);
                        view.setTranslationY(0f);
                    }

                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        view.animate().setListener(null);
                    }
                }).start();
        if (holder instanceof TypingHolder) {
            View label = view.findViewById(R.id.tvTyping);
            android.animation.ObjectAnimator blink = android.animation.ObjectAnimator.ofFloat(label, View.ALPHA, 1f, 0.45f);
            blink.setDuration(650);
            blink.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            blink.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            blink.start();
            label.setTag(blink);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        resetPosition(holder.itemView);
        View label = holder.itemView.findViewById(R.id.tvTyping);
        if (label != null && label.getTag() instanceof android.animation.Animator) {
            ((android.animation.Animator) label.getTag()).cancel();
            label.setAlpha(1f);
        }
        super.onViewRecycled(holder);
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
            bubble(itemView.findViewById(R.id.llUserBubble), 0xFFFE4A11, 0xFFFF672C, true);
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
            bubble(itemView.findViewById(R.id.llBotBubble), 0xFFFFFFFF, 0xB3D1CEC1, false);
            sources = itemView.findViewById(R.id.tvBotSources);
            copy = itemView.findViewById(R.id.ivCopyBot);
            speak = itemView.findViewById(R.id.ivSpeakBot);
        }

        void bind(ChatMessage message) {
            text.setText(MarkdownRenderer.render(message.getText()));
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
            bubble(itemView.findViewById(R.id.tvTyping), 0xFFFFFFFF, 0xB3D1CEC1, false);
        }
    }

    private static void bubble(View view, int fill, int stroke, boolean tailRight) {
        float density = view.getResources().getDisplayMetrics().density;
        view.setBackground(new ChatBubbleDrawable(density, fill, stroke, tailRight));
        int side = Math.round(ChatBubbleDrawable.TAIL_W_DP * density);
        view.setPadding(view.getPaddingLeft() + (tailRight ? 0 : side), view.getPaddingTop(),
                view.getPaddingRight() + (tailRight ? side : 0),
                view.getPaddingBottom() + Math.round(ChatBubbleDrawable.TAIL_DP * density));
    }
}
