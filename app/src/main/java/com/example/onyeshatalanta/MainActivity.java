package com.example.onyeshatalanta;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.os.IResultReceiver;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class  MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private RecyclerView postList;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,postsRef,likeref;
    private DocumentReference reference;
    private FirebaseFirestore firestore;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    Boolean likesChecker = false;

    String currentUserID;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("All Users");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userid = user.getUid();

        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("user").document(userid);

        postsRef = database.getReference("All posts");
        likeref = database.getReference("post likes");

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        postList = (RecyclerView) findViewById(R.id.all_users_post_list);

        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout ,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);


        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.user_navigation_name);

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });

        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if(snapshot.hasChild("name")){
                        String User_fName = snapshot.child("name").getValue().toString();
                        NavProfileUserName.setText(User_fName);
                    }
                    if (snapshot.hasChild("url")){
                        String image = snapshot.child("url").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Profile Name Do Not Exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                    return false;
            }
        });
        DisplayAllUsersPosts();
    }

    private void DisplayAllUsersPosts()
    {
        FirebaseRecyclerOptions<Postmember> options =
                new FirebaseRecyclerOptions.Builder<Postmember>()
                        .setQuery(postsRef,Postmember.class)
                        .build();

        FirebaseRecyclerAdapter<Postmember,PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Postmember, PostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull final Postmember model) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final String currentUserid = user.getUid();

                        final  String postkey = getRef(position).getKey();
                        holder.setPost(getBaseContext(), model.getName(),model.getUrl(),model.getPostUri(),model.getTime()
                                ,model.getUid(),model.getType(),model.getDesc());

                        final String url = getItem(position).getPostUri();
                        final String name = getItem(position).getName();
                        //   final String url = getItem(position).getUrl();
                        final  String time = getItem(position).getTime();
                        final String type = getItem(position).getType();
                        final String userid = getItem(position).getUid();

                        holder.likesChecker(postkey);
                        holder.menuoptions.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDialog(name,url,time,userid);
                            }
                        });
                        holder.likebtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                likesChecker = true;

                                likeref.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (likesChecker.equals(true)){
                                            if (snapshot.child(postkey).hasChild(currentUserid)){
                                                likeref.child(postkey).child(currentUserid).removeValue();
                                                //delete(time);
                                               // Toast.makeText(MainActivity.this, "Removed!!", Toast.LENGTH_SHORT).show();
                                                likesChecker = false;
                                            }else {


                                                likeref.child(postkey).child(currentUserid).setValue(true);

                                                likesChecker = false;


                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.all_post_layout,parent,false);

                        return new PostsViewHolder(view);



                    }
                };
        firebaseRecyclerAdapter.startListening();

        postList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewprofile,iv_post;
        TextView tv_name,tv_desc,tv_likes,tv_comment,tv_time,tv_nameprofile;
        ImageButton likebtn,menuoptions,commentbtn;
        DatabaseReference likesref;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        int likescount;

        public PostsViewHolder(View itemView) {
            super(itemView);

        }

        public void setPost(Context activity, String name, String url,String postUri,String time,
                            String uid,String type,String desc){
            SimpleExoPlayer exoPlayer;
            imageViewprofile = itemView.findViewById(R.id.ivprofile_item);
            iv_post = itemView.findViewById(R.id.iv_post_item);
            //    tv_comment = itemView.findViewById(R.id.commentbutton_posts);
            tv_desc = itemView.findViewById(R.id.tv_desc_post);
            commentbtn = itemView.findViewById(R.id.commentbutton_posts);
            likebtn = itemView.findViewById(R.id.likebutton_posts);
            tv_likes = itemView.findViewById(R.id.tv_likes_post);
            menuoptions = itemView.findViewById(R.id.morebutton_posts);
            tv_time = itemView.findViewById(R.id.tv_time_post);
            tv_nameprofile = itemView.findViewById(R.id.tv_name_post);



            PlayerView playerView = itemView.findViewById(R.id.exoplayer_item_post);


            if (type.equals("iv")) {

                Picasso.get().load(url).into(imageViewprofile);
                Picasso.get().load(postUri).into(iv_post);
                tv_desc.setText(desc);
                tv_time.setText(time);
                tv_nameprofile.setText(name);
                playerView.setVisibility(View.INVISIBLE);


            } else if (type.equals("vv")) {

                iv_post.setVisibility(View.INVISIBLE);
                tv_desc.setText(desc);
                tv_time.setText(time);
                tv_nameprofile.setText(name);
                Picasso.get().load(url).into(imageViewprofile);

                try {
                    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(activity).build();
                    TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                    exoPlayer = (SimpleExoPlayer) ExoPlayerFactory.newSimpleInstance(activity);
                    Uri video = Uri.parse(postUri);
                    DefaultHttpDataSourceFactory df = new DefaultHttpDataSourceFactory("video");
                    ExtractorsFactory ef = new DefaultExtractorsFactory();
                    MediaSource mediaSource = new ExtractorMediaSource(video, df, ef, null, null);
                    playerView.setPlayer(exoPlayer);
                    exoPlayer.prepare(mediaSource);
                    exoPlayer.setPlayWhenReady(false);


                } catch (Exception e) {
                    Toast.makeText(activity, "Error Loading Video", Toast.LENGTH_SHORT).show();

                }

            }


        }

        public void likesChecker(final String postkey) {
            likebtn = itemView.findViewById(R.id.likebutton_posts);

            likesref = database.getReference("post likes");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final String uid = user.getUid();

            likesref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.child(postkey).hasChild(uid)) {
                        likebtn.setImageResource(R.drawable.ic_like);
                        likescount = (int)snapshot.child(postkey).getChildrenCount();
                        tv_likes.setText(Integer.toString(likescount)+"Likes");

                    } else {
                        likebtn.setImageResource(R.drawable.ic_dislike);
                        likescount = (int)snapshot.child(postkey).getChildrenCount();
                        tv_likes.setText(Integer.toString(likescount)+"Likes");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }


    private void sendUserToPostActivity() {
        Intent addnewpostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(addnewpostIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            sendUserToLoginActivity();
        }
        else {
            checkUserExistance();
        }

    }

    private void checkUserExistance() {
       // final String current_user_id = mAuth.getCurrentUser().getUid();
        //UserRef.addValueEventListener(new ValueEventListener() {
           // @Override
            //public void onDataChange(@NonNull DataSnapshot snapshot) {
                //if (!snapshot.hasChild(current_user_id)){
                   // sendUserToSetupActivity();
                //}

           // }

            //@Override
            //public void onCancelled(@NonNull DatabaseError error) {

            //}
        //});
        reference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.getResult().exists()){

                            //sendToMainActivity();

                        }else {
                            Intent profileIntent = new Intent(MainActivity.this,CreateProfile.class);
                            startActivity(profileIntent);
                            finish();
                        }
                    }
                });



    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_post:
                sendUserToPostActivity();
                break;
            case R.id.nav_profile:
                sendUserToProfileActivity();
                Toast.makeText(this,"Profile",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                sendToMainActivity();
                Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                sendToPeople();
                Toast.makeText(this,"Oportunity Providers List",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                sendUserToSettingsActivity();
                break;
            case R.id.nav_opportunities:
                sendUserToOpportunitiesActivity();
                break;
            case R.id.nav_Logout:
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
        }
    }

    private void sendToMainActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,MainActivity.class);
        startActivity(settingsIntent);

    }

    private void sendUserToOpportunitiesActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,OpportunityResponse.class);
        startActivity(settingsIntent);

    }

    private void sendToPeople() {
        Intent settingsIntent = new Intent(MainActivity.this,People.class);
        startActivity(settingsIntent);

    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
    }
     private void sendUserToProfileActivity() {
        Intent profileActivity = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(profileActivity);
    }
    void showDialog(String name,String Url,String time,String userid){
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.post_options,null);
        TextView download = view.findViewById(R.id.download_tv_post);
        TextView share = view.findViewById(R.id.share_tv_post);
        TextView delete = view.findViewById(R.id.delete_tv_post);
        TextView copyurl = view.findViewById(R.id.copyurl_tv_post);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        alertDialog.show();

    }
}