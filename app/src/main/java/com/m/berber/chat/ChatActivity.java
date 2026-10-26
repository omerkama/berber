package com.m.berber.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import com.m.berber.R;
import com.m.berber.adapters.ChatAdapter;
import com.m.berber.auth.FirebaseHelper;
import com.m.berber.models.ChatMessage;
import com.m.berber.models.User;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ImageButton btnBack, btnCall, btnSendMessage;
    private TextView tvRecipientName, tvRecipientSub;
    private EditText etChatMessage;
    private RecyclerView rvChatMessages;

    private ChatAdapter chatAdapter;
    private FirebaseHelper firebaseHelper;
    private ListenerRegistration chatListenerRegistration;

    private String recipientUid;
    private String recipientName;
    private String recipientPhone;
    private String currentUid;
    private User currentUser;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recipientUid = getIntent().getStringExtra("recipientUid");
        recipientName = getIntent().getStringExtra("recipientName");
        recipientPhone = getIntent().getStringExtra("recipientPhone");

        if (recipientUid == null) {
            Toast.makeText(this, "Sohbet edilecek kişi bulunamadı.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseHelper = FirebaseHelper.getInstance();
        currentUid = firebaseHelper.getCurrentUid();

        if (currentUid == null) {
            finish();
            return;
        }

        // Generate deterministic chatId
        chatId = currentUid.compareTo(recipientUid) < 0
                ? currentUid + "_" + recipientUid
                : recipientUid + "_" + currentUid;

        btnBack = findViewById(R.id.btnBack);
        btnCall = findViewById(R.id.btnCall);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        tvRecipientName = findViewById(R.id.tvRecipientName);
        tvRecipientSub = findViewById(R.id.tvRecipientSub);
        etChatMessage = findViewById(R.id.etChatMessage);
        rvChatMessages = findViewById(R.id.rvChatMessages);

        tvRecipientName.setText(recipientName != null ? recipientName : "Kullanıcı");
        tvRecipientSub.setText(recipientPhone != null ? "Tel: " + recipientPhone : "Sohbet");

        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(currentUid);
        rvChatMessages.setAdapter(chatAdapter);

        btnBack.setOnClickListener(v -> finish());
        btnCall.setOnClickListener(v -> callRecipient());
        btnSendMessage.setOnClickListener(v -> sendMessage());

        fetchCurrentUser();
        listenToMessages();
    }

    private void fetchCurrentUser() {
        firebaseHelper.fetchCurrentUser(new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
            }

            @Override
            public void onFailure(String error) {}
        });
    }

    private void callRecipient() {
        if (recipientPhone == null || recipientPhone.trim().isEmpty()) {
            Toast.makeText(this, "Telefon numarası bulunamadı.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + recipientPhone.trim()));
        startActivity(intent);
    }

    private void listenToMessages() {
        FirebaseFirestore db = firebaseHelper.getDb();
        chatListenerRegistration = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null || queryDocumentSnapshots == null) return;

                    List<ChatMessage> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) messages.add(msg);
                    }

                    chatAdapter.setMessages(messages);
                    if (!messages.isEmpty()) {
                        rvChatMessages.smoothScrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = etChatMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        etChatMessage.setText("");

        String msgId = firebaseHelper.getDb().collection("chats").document(chatId).collection("messages").document().getId();
        String senderName = currentUser != null ? currentUser.getName() : "Kullanıcı";

        ChatMessage chatMessage = new ChatMessage(
                msgId,
                currentUid,
                senderName,
                recipientUid,
                text,
                System.currentTimeMillis()
        );

        firebaseHelper.getDb().collection("chats")
                .document(chatId)
                .collection("messages")
                .document(msgId)
                .set(chatMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove();
        }
    }
}