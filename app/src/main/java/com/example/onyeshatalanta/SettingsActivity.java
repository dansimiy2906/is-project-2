package com.example.onyeshatalanta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText Username,userprofname,userstatus,usercountry,usergender,userRelation,userdob;
    private Button updateusersettingsbtn;
    private CircleImageView userProfImage;
    private DatabaseReference SettingsUserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;
    final  static int Gallery_pick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        loadingBar = new ProgressDialog(this);

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Username = (EditText) findViewById(R.id.settings_username);
        userprofname = (EditText) findViewById(R.id.settings_profile_full_name);
        userstatus = (EditText) findViewById(R.id.settings_status);
        usercountry = (EditText) findViewById(R.id.settings_Country);
        usergender = (EditText) findViewById(R.id.settings_gender);
        userRelation = (EditText) findViewById(R.id.settings_relationship_status);
        userdob = (EditText) findViewById(R.id.settings_dob);
        userProfImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        updateusersettingsbtn =(Button)  findViewById(R.id.update_account_settings);

        SettingsUserRef.addValueEventListener(new ValueEventListener() {
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

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
                    Username.setText(myUserName);
                    userprofname.setText(myUserFullName);
                    userstatus.setText(myprofileStatus);
                    userdob.setText(myDOB);
                    usercountry.setText(myCounry);
                    usergender.setText(myGender);
                    userRelation.setText(myRelationshipStatus);
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateusersettingsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VallidateAccountInfo();
            }
        });
        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_pick);
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_pick && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please Wait While We are Updating Your Profile Image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                SettingsUserRef.child("Profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent selfIntent =     new Intent(SettingsActivity.this,SettingsActivity.class);
                                            startActivity(selfIntent);
                                            Toast.makeText(SettingsActivity.this, "Image Stored", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }

                        });

                    }

                });

            }
            else{

                Toast.makeText(this, "Error Occured: Image Can't be Cropped,Please Try Again", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void VallidateAccountInfo() {
        String username = Username.getText().toString();
        String profilename = userprofname.getText().toString();
        String status = userstatus.getText().toString();
        String country = usercountry.getText().toString();
        String gender = usergender.getText().toString();
        String relation = userRelation.getText().toString();
        String Dob = userdob.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Username Required", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(profilename)){
            Toast.makeText(this, "Profile Name Is Required", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(status)){
            Toast.makeText(this, "Status Is Required", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Country is Required", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(gender)){
            Toast.makeText(this, "Gender is Required", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(relation)){
            Toast.makeText(this, "Relation Is Required", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Dob)){
            Toast.makeText(this, "Date Of Birth Is Required", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please Wait While We are Updating Your Profile Image...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            UpdateAccountInfo(username,profilename,status,country,gender,relation,Dob);
        }
    }

    private void UpdateAccountInfo(String username, String profilename, String status, String country, String gender, String relation, String dob) {
        HashMap userMap = new HashMap();
        userMap.put("Username",username);
        userMap.put("Fullname",profilename);
        userMap.put("Talent Name",status);
        userMap.put("country",country);
        userMap.put("Gender",gender);
        userMap.put("DOB",dob);
        userMap.put("Residence",relation);

        SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    Toast.makeText(SettingsActivity.this, "Account Settings Updated Successfully", Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                    loadingBar.dismiss();
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Error Occured While Updating Account Settings Infomation", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }

            }
        });

    }
    private void sendUserToMainActivity() {
        Intent sendtoMain = new Intent(SettingsActivity.this,MainActivity.class);
        sendtoMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendtoMain);
        finish();
    }
}