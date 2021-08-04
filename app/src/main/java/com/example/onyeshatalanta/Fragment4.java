package com.example.onyeshatalanta;

import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.squareup.picasso.Picasso;

import java.security.Permission;
import java.security.Permissions;
import java.util.List;

public class Fragment4 extends Fragment implements View.OnClickListener{

    //Button button;
    RecyclerView recyclerView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference,likeref;
    Boolean likechecker = false;
    DatabaseReference db1,db2,db3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment4,container,false);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //button = getActivity().findViewById(R.id.createpost_f4);
        reference = database.getReference("All posts");
        likeref = database.getReference("post likes");
        recyclerView = getActivity().findViewById(R.id.rv_posts);
        recyclerView.setHasFixedSize(true);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = user.getUid();

        db1 = database.getReference("All images").child(currentuid);
        db2 = database.getReference("All videos").child(currentuid);
        db3 = database.getReference("All posts");

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

       // button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.createpost_f4:
                //Intent intent = new Intent(getActivity(), PostActivity.class);
                //startActivity(intent);
                break;


        }
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Postmember> options =
                new FirebaseRecyclerOptions.Builder<Postmember>()
                        .setQuery(reference,Postmember.class)
                        .build();

        FirebaseRecyclerAdapter<Postmember,PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Postmember,PostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull final Postmember model) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final String currentUserid = user.getUid();

                        final  String postkey = getRef(position).getKey();
                        holder.setPost(getActivity(),model.getName(), model.getPostUri(), model.getTime(), model.getUid(), model.getType(), model.getDesc());final String name = getItem(position).getName();



                        final String url = getItem(position).getPostUri();
                        //final String name = getItem(position).getName();
                        //   final String url = getItem(position).getUrl();
                        final  String time = getItem(position).getTime();
                        final String type = getItem(position).getType();
                        final String userid = getItem(position).getUid();




                        holder.likeschecker(postkey);

                        holder.menuoptions.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showDialog(name,url,time,userid,type);
                            }
                        });
                        holder.likebtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                likechecker = true;

                                likeref.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (likechecker.equals(true)){
                                            if (snapshot.child(postkey).hasChild(currentUserid)){
                                                likeref.child(postkey).child(currentUserid).removeValue();
                                                //  delete(time);
                                                //  Toast.makeText(getActivity(), "Removed from favourite", Toast.LENGTH_SHORT).show();
                                                likechecker = false;
                                            }else {


                                                likeref.child(postkey).child(currentUserid).setValue(true);

                                                likechecker= false;

                                                //       Toast.makeText(getActivity(), "Added to favourite", Toast.LENGTH_SHORT).show();
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

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }
    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProfile, iv_post;
        TextView tv_name, tv_desc, tv_likes, tv_comment, tv_time, tv_nameprofile;
        ImageButton likebtn, menuoptions, commentbtn;
        DatabaseReference likesref;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        int likescount;

        public PostsViewHolder(View itemView) {
            super(itemView);

        }

        public void setPost(Context activity, String name, String postUri, String time, String uid, String type, String desc) {
            iv_post = itemView.findViewById(R.id.iv_post_item);
            //tv_comment = itemView.findViewById(R.id.commentbutton_posts);
            tv_desc = itemView.findViewById(R.id.tv_desc_post);
            commentbtn = itemView.findViewById(R.id.commentbutton_posts);
            likebtn = itemView.findViewById(R.id.likebutton_posts);
            tv_likes = itemView.findViewById(R.id.tv_likes_post);
            menuoptions = itemView.findViewById(R.id.morebutton_posts);
            tv_time = itemView.findViewById(R.id.tv_time_post);
            tv_nameprofile = itemView.findViewById(R.id.tv_name_post);

            SimpleExoPlayer exoPlayer;
            PlayerView playerView = itemView.findViewById(R.id.exoplayer_item_post);

            if (type.equals("iv")) {

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

        public void likeschecker(final String postkey) {
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

    void showDialog(String name,String url,String time,String userid,String type){

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.post_options,null);
        TextView download = view.findViewById(R.id.download_tv_post);
        TextView share = view.findViewById(R.id.share_tv_post);
        TextView delete = view.findViewById(R.id.delete_tv_post);
        TextView copyurl = view.findViewById(R.id.copyurl_tv_post);


        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();

        alertDialog.show();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserid = user.getUid();

        if (userid.equals(currentUserid)){
            delete.setVisibility(View.VISIBLE);
        }else {
            delete.setVisibility(View.INVISIBLE);
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Query query = db1.orderByChild("time").equalTo(time);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                            dataSnapshot1.getRef().removeValue();

                            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Query query2 = db2.orderByChild("time").equalTo(time);
                query2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                            dataSnapshot1.getRef().removeValue();

                            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Query query3 = db3.orderByChild("time").equalTo(time);
                query3.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                            dataSnapshot1.getRef().removeValue();

                            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                reference.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                            }
                        });

                alertDialog.dismiss();

            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                PermissionListener permissionListener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {

                        if (type.equals("iv")){

                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                    DownloadManager.Request.NETWORK_MOBILE);
                            request.setTitle("Download");
                            request.setDescription("Downloading image....");
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,name+System.currentTimeMillis() + ".jpg");
                            DownloadManager manager = (DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                            manager.enqueue(request);

                            Toast.makeText(getActivity(), "Downloading", Toast.LENGTH_SHORT).show();

                            alertDialog.dismiss();
                        }else {
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                    DownloadManager.Request.NETWORK_MOBILE);
                            request.setTitle("Download");
                            request.setDescription("Downloading video....");
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,name+System.currentTimeMillis() + ".mp4");
                            DownloadManager manager = (DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                            manager.enqueue(request);

                            Toast.makeText(getActivity(), "Downloading", Toast.LENGTH_SHORT).show();

                            alertDialog.dismiss();
                        }

                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {

                        Toast.makeText(getActivity(), "No permissions", Toast.LENGTH_SHORT).show();
                    }
                };
                TedPermission.with(getActivity())
                        .setPermissionListener(permissionListener)
                        .setPermissions(Manifest.permission.INTERNET,Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check();




            }
        });


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sharetext = name +"\n" +"\n"+ url;
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_TEXT,sharetext);
                intent.setType("text/plain");
                startActivity(intent.createChooser(intent,"share via"));

                alertDialog.dismiss();
            }
        });

        copyurl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager cp = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("String",url);
                cp.setPrimaryClip(clip);
                clip.getDescription();
                Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();

                alertDialog.dismiss();

            }
        });



    }
}