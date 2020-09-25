package com.gyobeom29.hipboard.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.CheckImageVideo;
import com.gyobeom29.hipboard.activity.DetailPostActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainPostAdapter extends RecyclerView.Adapter<MainPostAdapter.GalleryViewHolder> {
    private static final String TAG = "MainPostAdapter";

    private ArrayList<PostInfo> postInfoList;
    private Activity activity;
    private ArrayList<ImageView> likeImageViewList;
    private TextView moreImageTextView;
    private int moreImageCount;

    // Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout listLayout;
        public GalleryViewHolder(LinearLayout v) {
            super(v);
            listLayout = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MainPostAdapter(ArrayList<PostInfo> myDataset, Activity activity) {
        postInfoList = myDataset;
        this.activity = activity;
        likeImageViewList = new ArrayList<>();
        moreImageTextView = null;
        moreImageCount = 0;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MainPostAdapter.GalleryViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // create a new view
        LinearLayout listLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        final GalleryViewHolder galleryViewHolder = new GalleryViewHolder(listLayout);

        listLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String documentId = postInfoList.get(galleryViewHolder.getAdapterPosition()).getDocumentId();

                Intent intent = new Intent(activity, DetailPostActivity.class);
                intent.putExtra("documentId",documentId);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            }
        });

        return galleryViewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Log.i("MainAdap","여기 옴");

        LinearLayout listLayout = holder.listLayout;
        TextView titleTextView = listLayout.findViewById(R.id.item_post_textView);
        if(titleTextView.getText()==null || titleTextView.getText().toString().length()<=0){
            titleTextView.setText(postInfoList.get(position).getTitle());
        }
        TextView createAtTextview = listLayout.findViewById(R.id.post_date_textView);
        if(createAtTextview.getText()==null || createAtTextview.getText().toString().length()<=0){
            createAtTextview.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(postInfoList.get(position).getCreateAt()));
        }
        ImageView likeImageView = listLayout.findViewById(R.id.likeImageView);
        likeImageView.setContentDescription("test"+position);
        likeImageViewList.add(likeImageView);

        TextView viewsTextView = listLayout.findViewById(R.id.viewsTextView);
        TextView likeCountTextView = listLayout.findViewById(R.id.likeCountTextView);
        viewsTextView.setText(""+postInfoList.get(position).getViews());
        likeCountTextView.setText("" + postInfoList.get(position).getLikeCount());

        TextView publisherNameTextView = listLayout.findViewById(R.id.post_item_publisher_name_text_view);
        publisherNameTextView.setText(postInfoList.get(position).getPublisherName());

        setLikeImageVewSelect(postInfoList.get(position).getDocumentId(),position);

        ArrayList<String> contentList = (ArrayList<String>) postInfoList.get(position).getContents();
        RelativeLayout imageCountLayout = listLayout.findViewById(R.id.image_count_layout);

        TextView contentTextView = listLayout.findViewById(R.id.post_item_content_text_view);
        contentTextView.setText(contentList.get(0));
        if(contentList.size()>1){
            for(int i = 1; i< contentList.size(); i++){
                String content = contentList.get(i);
                if(Patterns.WEB_URL.matcher(content).matches()){
                    if(CheckImageVideo.isVideo(content) || CheckImageVideo.isImage(content)){
                        if(moreImageTextView == null){
                            imageCountLayout.removeAllViewsInLayout();
                            imageCountLayout.setBackgroundColor(R.color.colorBackTwo);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams.gravity = Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL;
                            moreImageTextView = new TextView(activity.getApplicationContext());
                            moreImageTextView.setTextColor(Color.WHITE);
                            moreImageTextView.setLayoutParams(layoutParams);

                            moreImageTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);

                            moreImageTextView.setText("More\n");
                            moreImageTextView.append(moreImageCount+"");

                            if(moreImageCount>0){
                                moreImageTextView.append("+");
                            }
                            imageCountLayout.addView(moreImageTextView);
                        }else{
                            moreImageTextView.setText("More\n");
                            moreImageTextView.append(moreImageCount+ "");
                            if(moreImageCount>0){
                                moreImageTextView.append("+");
                            }
                        }
                        moreImageCount++;
                    }
                }
            }
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return postInfoList.size();
    }

    private PlayerView getPlayerView(){
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        PlayerView playerView = (PlayerView) inflater.inflate(R.layout.view_content_player,null,false);
        return playerView;
    }

    private void setting(int position){
        Log.i("MainAdapter","contentDesc : " + likeImageViewList.get(position).getContentDescription());
    }

    private void setLikeImageVewSelect(String documentId, final int position){
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("likePost").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    if(documentSnapshot.getBoolean("isLike")!=null){
                        likeImageViewList.get(position).setSelected(documentSnapshot.getBoolean("isLike"));
                    }else{
                        likeImageViewList.get(position).setSelected(false);
                    }
                }else {
                    likeImageViewList.get(position).setSelected(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

}
