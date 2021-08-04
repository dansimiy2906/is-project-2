package com.example.onyeshatalanta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private Toolbar ChattoolBar;
    private ImageButton sendMessageButton,sendImageFileButton;
    private EditText userMessageInput;

    private RecyclerView userMessageList;
    private final List<messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private messagesAdapter mAdapter;

    private String MessageReciverid,messageSenderId,saveCurrentDate,saveCurrentTime;
    private TextView Username;
    private CircleImageView RecieverProfImage;
    private TextView recieverName;
    private DatabaseReference Rootref;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Rootref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        MessageReciverid = getIntent().getExtras().get("visit_user_id").toString();
        InitializeFields();
        DisplayRecieverInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        FetchMessages();

    }

    private void FetchMessages() {
        Rootref.child("Messages").child(messageSenderId).child(MessageReciverid)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()){
                            messages Messages = snapshot.getValue(messages.class);
                            messagesList.add(Messages);
                            mAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void SendMessage() {
        String messageText = userMessageInput.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Please Type a message First", Toast.LENGTH_SHORT).show();
        }
        else{
            String message_Sender_ref = "Messages/" + messageSenderId + "/" + MessageReciverid;
            String message_Reciever_ref = "Messages/" + MessageReciverid + "/" + messageSenderId;

            DatabaseReference user_message_key_ref = Rootref.child("messages").child(messageSenderId)
                    .child(MessageReciverid)
                    .push();
            String message_push_id = user_message_key_ref.getKey();

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            saveCurrentTime = currentTime.format(calendar.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_Sender_ref + "/" + message_push_id, messageTextBody);
            messageBodyDetails.put(message_Sender_ref + "/" + message_push_id, messageTextBody);

            Rootref.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                        userMessageInput.setText(" ");

                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this,"Error : " + message, Toast.LENGTH_SHORT).show();
                        userMessageInput.setText(" ");


                    }

                }

            });


        }
    }

    private void DisplayRecieverInfo() {
        Rootref.child("Messages").child(MessageReciverid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //final String profileImage = snapshot.child("url").getValue().toString();
                    //Picasso.get().load(profileImage).placeholder(R.drawable.profile_icon).into(RecieverProfImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFields() {
        ChattoolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChattoolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        sendImageFileButton = (ImageButton) findViewById(R.id.send_image_file_button);
        userMessageInput = (EditText) findViewById(R.id.input_message);
        recieverName = (TextView) findViewById(R.id.custom_profile_name);
        RecieverProfImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        mAdapter = new messagesAdapter(messagesList);
        userMessageList = (RecyclerView) findViewById(R.id.messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(mAdapter);



    }
}