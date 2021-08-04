package com.example.onyeshatalanta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    ImageView imageView;
    private Uri selectedUri;
    private  static final int PICK_FILE = 1;
    private ProgressDialog loadingBar;
    UploadTask uploadTask;
    EditText etdesc;
    Button btnchoosefile,btnuploadfile,btngoToMusic;
    VideoView videoView;
    String url,name;
    String TalentCategory;
    StorageReference storageReference;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference db1,db2,db3;
    private Toolbar mToolbar;

    MediaController mediaController;
    String type;
    Postmember postmember;

   // String [] TelentCategories = {"Art","Music","Comedy","Event Mc Activity","Dance","Sports","Others"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mToolbar = (Toolbar) findViewById(R.id.upload_talent);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Upload Talent");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        postmember = new Postmember();
        mediaController = new MediaController(this);
        loadingBar = new ProgressDialog(this);

        imageView = findViewById(R.id.iv_post);
        videoView= findViewById(R.id.vv_post);
        btnchoosefile = findViewById(R.id.btn_choosefile_post);
        btnuploadfile = findViewById(R.id.btn_uploadfile_post);
        btngoToMusic = findViewById(R.id.btn_upmusic_file);
        etdesc = findViewById(R.id.et_desc_post);

        storageReference = FirebaseStorage.getInstance().getReference("User posts");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = user.getUid();

        db1 = database.getReference("All images").child(currentuid);
        db2 = database.getReference("All videos").child(currentuid);
        db3 = database.getReference("All posts");




        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<>();
        categories.add("Art");
        categories.add("Music");
        categories.add("Comedy");
        categories.add("Event MC Activity");
        categories.add("Dance");
        categories.add("Sports");
        categories.add("Others");

        ArrayAdapter<String> dataAdpter = new ArrayAdapter<>(this , android.R.layout.simple_spinner_item, categories);

        dataAdpter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdpter);

        btnuploadfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dopost();
            }
        });

        btnchoosefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        btngoToMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendToMusicUpload();
            }
        });


    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        TalentCategory = adapterView.getItemAtPosition(i).toString();


    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private void SendToMusicUpload() {
        Intent sendtoMusicUpload = new Intent(PostActivity.this,MusicUploading.class);
        sendtoMusicUpload.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendtoMusicUpload);
        finish();
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/* video/*");
        // intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE || resultCode == RESULT_OK ||

                data != null || data.getData() != null){

            selectedUri = data.getData();


            if (selectedUri.toString().contains("image")){
                Picasso.get().load(selectedUri).into(imageView);
                imageView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.INVISIBLE);
                type = "iv";
            }else if (selectedUri.toString().contains("video")){
                videoView.setMediaController(mediaController);
                videoView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                videoView.setVideoURI(selectedUri);
                videoView.start();
                type = "vv";
            }else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }

        }

    }
    private String getFileExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType((contentResolver.getType(uri)));

    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("user").document(currentuid);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("All Users");

        //reference.addValueEventListener(new ValueEventListener() {
           // @Override
            //public void onDataChange(@NonNull DataSnapshot snapshot) {

               // name = snapshot.child("Fullname").getValue().toString();
                //url = snapshot.child("Profileimage").getValue().toString();
           // }

           // @Override
           // public void onCancelled(@NonNull DatabaseError error) {

            //}
       // });

        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.getResult().exists()) {
                            name = task.getResult().getString("Fname");
                            url = task.getResult().getString("Profileimage");



                        } else {
                            Toast.makeText(PostActivity.this, "error", Toast.LENGTH_SHORT).show();

                        }

                    }
                });


    }

    void Dopost(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String currentuid = user.getUid();

        final String desc = etdesc.getText().toString();


        Calendar cdate = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMMM-yyyy");
        final  String savedate = currentdate.format(cdate.getTime());

        Calendar ctime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
        final String savetime = currenttime.format(ctime.getTime());


        final String time = savedate +":"+ savetime;


        if (TextUtils.isEmpty(desc) || selectedUri != null){
            loadingBar.setTitle("UPLOAD TALENT");
            loadingBar.setMessage("Uploading Talent,please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            final StorageReference reference = storageReference.child(System.currentTimeMillis()+ "."+getFileExt(selectedUri));
            uploadTask = reference.putFile(selectedUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }

                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();

                        if (type.equals("iv")){
                            postmember.setDesc(desc);
                            postmember.setName(name);
                            postmember.setPostUri(downloadUri.toString());
                            postmember.setTime(time);
                            postmember.setUid(currentuid);
                            postmember.setUrl(url);
                            postmember.setCategory(TalentCategory);
                            postmember.setType("iv");

                            // for image
                            String id = db1.push().getKey();
                            db1.child(id).setValue(postmember);
                            // for both
                            String id1 = db3.push().getKey();
                            db3.child(id1).setValue(postmember);
                            loadingBar.dismiss();
                            Toast.makeText(PostActivity.this, "Post uploaded", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }else if (type.equals("vv")){

                            postmember.setDesc(desc);
                            postmember.setName(name);
                            postmember.setPostUri(downloadUri.toString());
                            postmember.setTime(time);
                            postmember.setUid(currentuid);
                            postmember.setUrl(url);
                            postmember.setCategory(TalentCategory);
                            postmember.setType("vv");

                            // for video
                            String id3 = db2.push().getKey();
                            db2.child(id3).setValue(postmember);

                            // for both
                            String id4 = db3.push().getKey();
                            db3.child(id4).setValue(postmember);
                            loadingBar.dismiss();
                            sendUserToMainActivity();
                            Toast.makeText(PostActivity.this, "Post Uploaded", Toast.LENGTH_SHORT).show();

                        }else {
                            Toast.makeText(PostActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }


                    }

                }
            });

        }else {
            Toast.makeText(this, "Please fill all Fields", Toast.LENGTH_SHORT).show();
        }

    }

    private void sendUserToMainActivity() {
        Intent sendtoMain = new Intent(PostActivity.this,MainActivity.class);
        sendtoMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendtoMain);
        finish();
    }


}