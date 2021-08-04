package com.example.onyeshatalanta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private TextView Username,userprofname,userstatus,usercountry,usergender,userRelation,userdob;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private String CurrentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserId);

        mToolbar = (Toolbar) findViewById(R.id.profileToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        Username = (TextView) findViewById(R.id.myprofile_Username);
        userprofname = (TextView) findViewById(R.id.myprofile_Full_Name);
        userstatus = (TextView) findViewById(R.id.myprofile_status);
        usercountry = (TextView) findViewById(R.id.mycountry);
        usergender = (TextView) findViewById(R.id.mygender);
        userRelation = (TextView) findViewById(R.id.relationshipStatus);
        userdob = (TextView) findViewById(R.id.mydob);
        userProfileImage = (CircleImageView) findViewById(R.id.settings_profile_image);

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String myProfileImage = snapshot.child("Profileimage").getValue().toString();
                    String myUserName = snapshot.child("Username").getValue().toString();
                    String myUserFullName = snapshot.child("Fullname").getValue().toString();
                    String myprofileStatus = snapshot.child("Talent Name").getValue().toString();
                    String myDOB = snapshot.child("DOB").getValue().toString();
                    String myCounry = snapshot.child("country").getValue().toString();
                    String myGender = snapshot.child("Gender").getValue().toString();
                    String myRelationshipStatus = snapshot.child("Residence").getValue().toString();

                   // Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    Username.setText("@" + myUserName);
                    userprofname.setText(myUserFullName);
                    userstatus.setText(myprofileStatus);
                    userdob.setText("Date Of Birth: "+myDOB);
                    usercountry.setText("Country: "+myCounry);
                    usergender.setText("Gender: "+myGender);
                    userRelation.setText("Relationship: "+myRelationshipStatus);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}