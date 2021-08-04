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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private EditText username,fullname,countryname;
    private Button saveinformationbutton;
    private CircleImageView profileimage;
    final  static int Gallery_pick = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    private FirebaseFirestore fstore;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        fstore = FirebaseFirestore.getInstance();
        mAuth =  FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        username = (EditText) findViewById(R.id.setup_username);
        fullname = (EditText) findViewById(R.id.setup_fullname);
        countryname = (EditText) findViewById(R.id.setup_country_name);
        saveinformationbutton = (Button) findViewById(R.id.setup_information_button);
        profileimage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        saveinformationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });
        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_pick);
            }
        });
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if (snapshot.hasChild("Profileimage")){
                        String image = snapshot.child("Profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileimage);
                    }
                    else{
                        Toast.makeText(SetupActivity.this, "Please Select Profile Image", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
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
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();
               final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                //FirebaseUser user = mAuth.getCurrentUser();
                                //DocumentReference db = fstore.collection("Users").document(user.getUid());
                                //Map<String,Object> userInfo = new HashMap<>();
                                //userInfo.put("profImageUri",downloadUrl);
                                //db.set(userInfo);

                                UserRef.child("Profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){   Toast.makeText(SetupActivity.this, "Image Stored", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
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

    private void saveAccountSetupInformation() {
        String Username = username.getText().toString();
        String Fullname = fullname.getText().toString();
        String country = countryname.getText().toString();

        if (TextUtils.isEmpty(Username)){
            Toast.makeText(this, "Username is Required!", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(Fullname)){
            Toast.makeText(this, "Full Name is Required!", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(country)){
            Toast.makeText(this, "Country Name is Required!", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Setting Up Account Information");
            loadingBar.setMessage("Please Wait While Your Information Id Being Saved...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);



            FirebaseUser user = mAuth.getCurrentUser();
            DocumentReference db = fstore.collection("Users").document(user.getUid());
            Map<String,Object> userInfo = new HashMap<>();
            userInfo.put("Uname",username.getText().toString());
            userInfo.put("Fname",fullname.getText().toString());
            userInfo.put("Cname",countryname.getText().toString());
            db.set(userInfo);

            HashMap userMap = new HashMap();
            userMap.put("Username",Username);
            userMap.put("Fullname",Fullname);
            userMap.put("country",country);
            userMap.put("Talent Name","Hello!!");
            userMap.put("Gender","None");
            userMap.put("DOB","Null");
            userMap.put("Residence","Nairobi");

            UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        senduserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your Information Has been Saved", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occured" + message, Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    }

    private void senduserToMainActivity() {
        Intent sendtoMain = new Intent(SetupActivity.this,MainActivity.class);
        sendtoMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendtoMain);
        finish();
    }
}