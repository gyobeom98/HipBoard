package com.gyobeom29.hipboard.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
    // Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView cardView;
        public GalleryViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MainPostAdapter(ArrayList<PostInfo> myDataset, Activity activity) {
        postInfoList = myDataset;
        this.activity = activity;
        likeImageViewList = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MainPostAdapter.GalleryViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // create a new view
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        final GalleryViewHolder galleryViewHolder = new GalleryViewHolder(cardView);

        cardView.setOnClickListener(new View.OnClickListener() {
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
    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        CardView cardView = holder.cardView;
        TextView titleTextView = cardView.findViewById(R.id.item_post_textView);
        titleTextView.setText(postInfoList.get(position).getTitle());

        TextView createAtTextview = cardView.findViewById(R.id.post_date_textView);
        createAtTextview.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(postInfoList.get(position).getCreateAt()));
        ImageView likeImageView = cardView.findViewById(R.id.likeImageView);
        likeImageView.setContentDescription("test"+position);
        likeImageViewList.add(likeImageView);

        setting(position);
        setLikeImageVewSelect(postInfoList.get(position).getDocumentId(),position);

        LinearLayout contentsLayout = cardView.findViewById(R.id.post_contents_layout);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ArrayList<String> contentList = (ArrayList<String>) postInfoList.get(position).getContents();
        TextView viewsTextView = cardView.findViewById(R.id.viewsTextView);
        TextView likeCountTextView = cardView.findViewById(R.id.likeCountTextView);
        viewsTextView.setText("조회수 : " + postInfoList.get(position).getViews());
        likeCountTextView.setText("" + postInfoList.get(position).getLikeCount());

        for(int i = 0; i < contentList.size();i++){
            if(i<3){
                String content = contentList.get(i);
                if(Patterns.WEB_URL.matcher(content).matches()){

                    if(CheckImageVideo.isVideo(content)){
                        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(activity);
                        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(activity,
                                Util.getUserAgent(activity, "yourApplicationName"));

                        // This is the MediaSource representing the media to be played.
                        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(Uri.parse(content));

                        // Prepare the player with the source.
                        player.prepare(videoSource);
                        final PlayerView playerView = getPlayerView();
                        playerView.setPlayer(player);
                        player.addVideoListener(new VideoListener() {
                            @Override
                            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                                playerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height));
                            }
                        });
                        contentsLayout.addView(playerView);
                    }else {
                        ImageView contentImageView = new ImageView(activity);
                        contentImageView.setLayoutParams(layoutParams);
                        contentImageView.setAdjustViewBounds(true);
                        contentImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        contentsLayout.addView(contentImageView);
                        Glide.with(activity).load(content).override(1000).thumbnail(0.1f).into(contentImageView);
                    }
                }else {
                    if (content.length() > 0) {
                        TextView contentTextView = new TextView(activity);
                        contentTextView.setLayoutParams(layoutParams);
                        contentTextView.setPadding(10,10,10,100);
                        contentsLayout.addView(contentTextView);
                        contentTextView.setText(content);

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
