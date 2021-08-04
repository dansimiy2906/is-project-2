package com.example.onyeshatalanta;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class messagesAdapter extends RecyclerView.Adapter<messagesAdapter.MessageViewHolder> {
    private List<messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    public messagesAdapter (List<messages> userMessagesList){
        this.userMessagesList = userMessagesList;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_of_users,parent,false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        messages Messages = userMessagesList.get(position);
        String fromUserID = Messages.getFrom();
        String fromMessageType = Messages.getType();

        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //String image = snapshot.child("url").getValue().toString();
                    //Picasso.get().load(image).placeholder(R.drawable.profile_icon).into(holder.RecieverProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (fromMessageType.equals("text")){
            holder.RecieverMessageText.setVisibility(View.INVISIBLE);
            holder.RecieverProfileImage.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderId)){
                holder.SenderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.SenderMessageText.setTextColor(Color.WHITE);
                holder.SenderMessageText.setGravity(Gravity.LEFT);
                holder.SenderMessageText.setText(Messages.getMessage());

            }
            else{
                holder.SenderMessageText.setVisibility(View.INVISIBLE);
                holder.RecieverMessageText.setVisibility(View.VISIBLE);
                holder.RecieverProfileImage.setVisibility(View.VISIBLE);

                holder.RecieverMessageText.setBackgroundResource(R.drawable.reciver_message_text_background);
                holder.RecieverMessageText.setTextColor(Color.WHITE);
                holder.RecieverMessageText.setGravity(Gravity.LEFT);
                holder.RecieverMessageText.setText(Messages.getMessage());


            }

        }



    }

    @Override
    public int getItemCount() {

        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView SenderMessageText,RecieverMessageText;
        public CircleImageView RecieverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            SenderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            RecieverMessageText = (TextView) itemView.findViewById(R.id.reciever_message_text);
            RecieverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);

        }
    }
}
