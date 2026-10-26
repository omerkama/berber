package com.m.berber.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.m.berber.R;
import com.m.berber.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<ChatMessage> messageList = new ArrayList<>();
    private final String currentUid;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatAdapter(String currentUid) {
        this.currentUid = currentUid;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messageList = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if (currentUid != null && currentUid.equals(message.getSenderId())) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        String formattedTime = timeFormat.format(new Date(message.getTimestamp()));

        if (holder instanceof SentViewHolder) {
            SentViewHolder sentHolder = (SentViewHolder) holder;
            sentHolder.tvMessageText.setText(message.getMessageText());
            sentHolder.tvMessageTime.setText(formattedTime);
        } else if (holder instanceof ReceivedViewHolder) {
            ReceivedViewHolder receivedHolder = (ReceivedViewHolder) holder;
            receivedHolder.tvSenderName.setText(message.getSenderName() != null ? message.getSenderName() : "");
            receivedHolder.tvMessageText.setText(message.getMessageText());
            receivedHolder.tvMessageTime.setText(formattedTime);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvMessageTime;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvMessageText, tvMessageTime;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }
    }
}