package com.example.onyeshatalanta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class OpportunityResponse extends AppCompatActivity implements View.OnClickListener {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference reference;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    RecyclerView recyclerView;
    private Toolbar mToolbar;

    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opportunity_response);

        mToolbar = (Toolbar) findViewById(R.id.Response);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Opportunities");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        imageView = (ImageView) findViewById(R.id.iv_f2);
        recyclerView = (RecyclerView) findViewById(R.id.rv_f2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseReference = database.getReference("AllQuestions");

        FirebaseRecyclerOptions<QuestionMember> options = new FirebaseRecyclerOptions.Builder<QuestionMember>()
                .setQuery(databaseReference,QuestionMember.class)
                .build();

        FirebaseRecyclerAdapter<QuestionMember,OportunityViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<QuestionMember, OportunityViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OportunityViewHolder holder, int position, @NonNull QuestionMember model) {
                holder.time_result.setText(model.getTime());
                holder.name_result.setText(model.getName());
                holder.question_result.setText(model.getQuestion());
                Picasso.get().load(model.getUrl()).placeholder(R.drawable.profile_icon).into(holder.imageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        String name = getRef(position).getKey();
                        String visit_user_id = getRef(position).getKey();
                        Intent profileIntent = new Intent(OpportunityResponse.this, ReplyActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                       // profileIntent.putExtra("Name", name);
                        startActivity(profileIntent);
                    }
                });




            }

            @NonNull
            @Override
            public OportunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.question_item,parent,false);
                return new OportunityViewHolder(view);
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    @Override
    public void onClick(View v) {

    }
    public static class OportunityViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        TextView time_result,name_result,question_result,deletebtn,replybtn,replybtn1;


        public OportunityViewHolder(@NonNull View itemView)
        {
            super(itemView);

            time_result = itemView.findViewById(R.id.time_que_item_tv);
            name_result = itemView.findViewById(R.id.name_que_item_tv);
            question_result = itemView.findViewById(R.id.que_item_tv);
            imageView = itemView.findViewById(R.id.iv_que_item);
            replybtn = itemView.findViewById(R.id.reply_item_que);


            //userName = itemView.findViewById(R.id.user_profile_name);
           // userStatus = itemView.findViewById(R.id.user_status);
            //profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}