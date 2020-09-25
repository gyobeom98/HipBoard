package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gyobeom29.hipboard.CheckImageVideo;
import com.gyobeom29.hipboard.Comment;
import com.gyobeom29.hipboard.FirebasePushMessage;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DetailPostActivity extends BasicActivity {

    private static final String TAG = "DetailPostActivity";

    private FirebaseFirestore firestore;
    private FirebaseUser user;
    private FirebaseAuth myAuth;
    private String documentId;
    private PostInfo postInfo;
    private StorageReference storageRef;
    private FirebaseStorage storage;
    private ArrayList<String> imageNames;
    private long views;
    private ImageView menuImageView, likeImageView;
    private String publusherName;
    private TextView titleTextView , detailTextView, viewsTextView, dateTextView, likeCountTextView,publisherTextView;
    private EditText postsCommentEditText;
    private ArrayList<SimpleExoPlayer> players;
    private LinearLayout contentLayout,commentLayout;
    private ArrayList<Long> playersPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_post);

        init();
        imageNames = new ArrayList<>();
        views = -1;

        myAuth = FirebaseAuth.getInstance();
        user = myAuth.getCurrentUser();
        writeLog("userUid : " + user.getUid());
        getName(user.getUid());
        loadContents(1);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadContents(2);
    }

    private void startActivityFinish(Class c){
        Intent intent = new Intent(this,c);
        startActivity(intent);
        finish();
    }

    private void writeLog(String msg){
        Log.i(TAG,msg);
    }

    private void startingTost(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    private void deletePost(){
        firestore.collection("posts").document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        writeLog("deleteSuccess");
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setNavigation(this.getClass());
    }

    private void init(){

        likeImageView = findViewById(R.id.detail_post_likeImageView);
        commentLayout = findViewById(R.id.comment_layout);
        postsCommentEditText = findViewById(R.id.posts_comment);
        publisherTextView = findViewById(R.id.detail_post_publisher);
        menuImageView = findViewById(R.id.toolbar_menu_imageView);
        titleTextView = findViewById(R.id.detail_post_title_textView);
        detailTextView = findViewById(R.id.detail_post_textView);
        viewsTextView = findViewById(R.id.detail_post_viewsTextView);
        dateTextView = findViewById(R.id.detail_post_date_textView);
        likeCountTextView = findViewById(R.id.detail_post_likeCountTextView);
        contentLayout = findViewById(R.id.detail_post_contents_layout);
        findViewById(R.id.posts_comment_btn).setOnClickListener(onClickListener);
        likeImageView.setOnClickListener(onClickListener);
        writeLog("Selected : " +likeImageView.isSelected());

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.posts_comment_btn :
                    String comment = postsCommentEditText.getText().toString();
                    if(comment.trim().length()>0){
                        boolean isWriter = false;
                        postsCommentEditText.setText("");
                        if(postInfo.getPublisher().equals(user.getUid())){
                            isWriter = true;
                        }
                        commentUpload(new Comment(comment,user.getUid(),isWriter,new Date(),publusherName));
                    }
                    break;
                case R.id.detail_post_likeImageView :
                    if(likeImageView.isSelected()){
                        likeImageView.setSelected(false);
                    }else{
                        likeImageView.setSelected(true);
                    }
                    updateUserPostLike();
                    break;
            }
        }
    };

    private void commentUpload(final Comment comment) {
        DocumentReference documentReference = firestore.collection("posts").document(documentId).collection("comments").document();
        documentReference.set(comment).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();

            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                writeLog("comment 올리기 성공");
                writeLog("Comment documentId : " + postInfo.getDocumentId());
                if(!comment.isPostWriter()){
                    writeLog("메시지 보내기 시작");
                    FirebasePushMessage.sendPush(postInfo,getApplicationContext());
                }

                loadContents(2);
            }
        });
    }

    private void deleteStorageImage(){
        final boolean[] isDel = new boolean[imageNames.size()];
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        writeLog("size : " + imageNames.size());
        for(int i =0; i <imageNames.size();i++){
            String imgName = imageNames.get(i);
            writeLog("imgName : " + imgName);
            final int finalI = i;
            writeLog("posts/"+documentId+"/"+imgName);
            storageRef.child("posts/"+documentId+"/"+imgName).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    boolean isCom = false;
                    if(task.isSuccessful()){
                        isDel[finalI] = true;
                        writeLog("이미지 삭제 성공");
                        if(finalI == imageNames.size()-1){

                            for (int i = 0; i < isDel.length;i++){
                                isCom = isDel[i];
                                if(!isDel[i]){
                                    break;
                                }
                            }
                            if(isCom){
                                deletePost();
                            }

                        }
                    }else{
                        writeLog("이미지 삭제 실패");
                        isDel[finalI] = false;
                    }
                }
            });

        }
    }
    private void loadContents(final int where){
        if(user !=null){
            Intent idIntent = getIntent();
            if(idIntent != null){
                players = new ArrayList<>();
                documentId = idIntent.getStringExtra("documentId");
                if(documentId!=null && documentId.length()>0){
                    setLikeImageViewSelect();
                    writeLog("documentId : " + documentId);
                    firestore = FirebaseFirestore.getInstance();
                    DocumentReference df =  firestore.collection("posts").document(documentId);
                    df.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                writeLog("success");
                                DocumentSnapshot document = task.getResult();
                                if(document !=null){
                                    if(document.exists()){
                                        writeLog("documentData " + document.getData());
                                        String title = document.getData().get("title").toString();
                                        ArrayList<String> contents = (ArrayList<String>) document.getData().get("contents");
                                        String publisher = document.getData().get("publisher").toString();
                                        Date createAt = new Date(document.getDate("createAt").getTime());
                                        long likeCnt = (long) document.getData().get("likeCount");
                                        String publisherName = document.getData().get("publisherName").toString();
                                        postInfo = new PostInfo(title,contents,publisher,views,likeCnt,createAt,publisherName);
                                        postInfo.setDocumentId(documentId);

                                        if(where==1) {
                                            views = (Long) document.getData().get("views");
                                            setActionBarTitle("게시글 : " + postInfo.getTitle());
                                            viewsUp();
                                        }
                                        if(postInfo.getPublisher().equals(user.getUid())){
                                            menuImageView.setVisibility(View.VISIBLE);
                                            menuImageView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    showPopup(v);
                                                }
                                            });
                                        }else{
                                            menuImageView.setVisibility(View.GONE);
                                        }
                                        if(postInfo.getPublisherName()!=null && postInfo.getPublisherName().length()>0){
                                            publisherTextView.setVisibility(View.VISIBLE);
                                            publisherTextView.setText(postInfo.getPublisherName());
                                        }else{
                                            publisherTextView.setVisibility(View.GONE);
                                        }
                                        titleTextView.setText(title);
                                        dateTextView.setText(new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.getDefault()).format(createAt));
                                        writeLog("views : " + views);
                                        viewsTextView.setText("조회수 : " + views);
                                        likeCountTextView.setText(""+likeCnt);
                                        detailTextView.setText(contents.get(0));
                                        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        int imgCnt = 0;
                                        int videoCnt = 0;
                                        contentLayout.removeAllViewsInLayout();
                                        for(int i = 0; i < contents.size();i++){
                                            String content = contents.get(i);
                                            if(Patterns.WEB_URL.matcher(content).matches()){
                                                writeLog(content);
                                                String[] pathList = content.split("\\.");
                                                String type = pathList[pathList.length-1];
                                                type = type.substring(0,type.indexOf('?'));
                                                writeLog(type);
                                                writeLog("imageName : " + imgCnt+"."+type);
                                                imageNames.add(imgCnt+"."+type);
                                                if(CheckImageVideo.isVideo(content)){
                                                    final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(getApplicationContext());
                                                    DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                                                            Util.getUserAgent(getApplicationContext(), getString(R.string.app_name)));
                                                    // This is the MediaSource representing the media to be played.
                                                    MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                                                            .createMediaSource(Uri.parse(content));

                                                    // Prepare the player with the source.
                                                    player.prepare(videoSource);
                                                    final PlayerView playerView = getPlayerView();

                                                    player.addVideoListener(new VideoListener() {
                                                        @Override
                                                        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                                                            playerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height));
                                                        }
                                                    });

//                                                    player.setPlayWhenReady(false);
                                                    player.getContentDuration();
                                                    players.add(player);
                                                    playerView.setPlayer(player);
                                                    playerView.setPadding(0,100,0,0);
                                                    contentLayout.addView(playerView);
                                                    if(where == 2){
                                                        if(playersPosition!=null) {
                                                            if (playersPosition.size() > 0 && videoCnt <= playersPosition.size()) {
                                                                player.seekTo(playersPosition.get(videoCnt));
                                                            }
                                                        }
                                                    }
                                                    videoCnt++;
                                                }else{
                                                    ImageView contentImageView = new ImageView(getApplicationContext());
                                                    contentImageView.setLayoutParams(layoutParams);
                                                    contentImageView.setAdjustViewBounds(true);
                                                    contentImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                                    contentImageView.setPadding(0,100,0,0);
                                                    contentLayout.addView(contentImageView);
                                                    Glide.with(getApplicationContext()).load(content).override(1000).thumbnail(0.1f).into(contentImageView);
                                                }
                                                writeLog(content);
                                                imgCnt++;
                                            }else {
                                                if (content.length() > 0 && i>0) {
                                                    TextView contentTextView = new TextView(getApplicationContext());
                                                    contentTextView.setLayoutParams(layoutParams);
                                                    contentTextView.setPadding(10,10,10,100);
                                                    contentLayout.addView(contentTextView);
                                                    contentTextView.setText(content);

                                                }
                                            }
                                        }
                                    }else{
                                        writeLog("noSuchData");
                                    }
                                }
                                firestore.collection("posts").document(documentId).collection("comments").orderBy("writeDate", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        commentLayout.removeAllViewsInLayout();
                                        for (QueryDocumentSnapshot qds: queryDocumentSnapshots) {
                                            writeLog("일단 받아봄 : " + qds.getData().toString());
                                            LinearLayout innerLayout = new LinearLayout(getApplicationContext());
                                            innerLayout.setOrientation(LinearLayout.VERTICAL);
                                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            innerLayout.setLayoutParams(layoutParams);
                                            innerLayout.setBackgroundResource(R.drawable.comment_inner_layout_shape);
                                            innerLayout.setPadding(0,15,0,15);

                                            LinearLayout innerTimeLayout = new LinearLayout(getApplicationContext());
                                            innerTimeLayout.setOrientation(LinearLayout.HORIZONTAL);
                                            innerTimeLayout.setLayoutParams(layoutParams);

                                            TextView commentPublisherTextView = new TextView(getApplicationContext());

                                            commentPublisherTextView.setText("작성자 : " + qds.getData().get("writePublisher").toString());

                                            if((boolean)qds.getData().get("postWriter")){
                                                commentPublisherTextView.append("\t 글쓴이");
                                            }
                                            innerTimeLayout.addView(commentPublisherTextView);

                                            TextView commentTimeTextView = new TextView(getApplicationContext());
                                            LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            textViewParams.weight = 1;
                                            commentPublisherTextView.setLayoutParams(textViewParams);
                                            textViewParams.weight = 1;
                                            commentTimeTextView.setLayoutParams(textViewParams);
                                            commentTimeTextView.setTextSize(12);
                                            commentTimeTextView.setGravity(Gravity.RIGHT);

                                            Date wrDate = new Date(qds.getDate("writeDate").getTime());
                                            commentTimeTextView.setText("작성일 : " +new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.getDefault()).format(wrDate));

                                            innerTimeLayout.addView(commentTimeTextView);

                                            innerLayout.addView(innerTimeLayout);



                                            TextView commentContentTextView = new TextView(getApplicationContext());
                                            commentContentTextView.setLayoutParams(layoutParams);
                                            innerLayout.addView(commentContentTextView);
                                            commentLayout.addView(innerLayout);
                                            commentContentTextView.setText("내용 : " + qds.getString("content"));
                                            commentContentTextView.setPadding(5,10,0,0);
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                            }else{
                                writeLog("failed");
                            }
                        }
                    });
                }
            }
        }


    }

    private void viewsUp(){
        views++;
        DocumentReference ref = firestore.collection("posts").document(documentId);
        ref.update("views",views).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    writeLog("조회수 올리기 성공");
                }else{
                    writeLog("조회수 올리기 실패");
                }
            }
        });
    }


    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.modifyPost : Intent intent = new Intent(DetailPostActivity.this,WritePostActivity.class);
                        writeLog("postInfo : " + postInfo.toString());
                        intent.putExtra("postInfo",postInfo);
                        intent.putExtra("createAt",postInfo.getCreateAt().getTime());
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        return true;
                    case R.id.deletePost :
                        if(imageNames.size()>0){
                            deleteStorageImage();
                        }else {
                            deletePost();
                        }

                        return true;
                    default: return  false;
                }
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.post_menu, popup.getMenu());
        popup.show();
    }

    private void getName(String publisher){
        FirebaseFirestore.getInstance().collection("users").document(publisher).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    publusherName =  documentSnapshot.get("name").toString();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                writeLog("getNamesError : " + e.toString());
            }
        });
    }

    private PlayerView getPlayerView(){
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        PlayerView playerView = (PlayerView) inflater.inflate(R.layout.view_content_player,null,false);
        return playerView;
    }

    @Override
    protected void onPause() {
        super.onPause();
        playersPosition = new ArrayList<>();
        if(players.size()>0){
            for (int i = 0; i < players.size();i++){
                writeLog("getCurrentPosition : " + players.get(i).getCurrentPosition());
                writeLog("getCurrentTimeLine : " + players.get(i).getCurrentTimeline());
                playersPosition.add(players.get(i).getCurrentPosition());
            }
        }
    }




    private void println(String msg){
        Log.i(TAG,msg);
    }

    private void updateUserPostLike(){
        Map<String,Boolean> data = new HashMap<>();
        if(likeImageView.isSelected()){
            data.put("isLike",true);
        }else{
            data.put("isLike",false);
        }
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("likePost").document(documentId).set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    writeLog("성공 !");
                    likeCountUp();
                }
            }
        });
    }

    private void likeCountUp(){
        FirebaseFirestore.getInstance().collection("posts").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    if(documentSnapshot.getData().get("likeCount")!=null){
                        int likeCount = Integer.parseInt(documentSnapshot.getData().get("likeCount").toString());
                        if(likeImageView.isSelected()){
                            likeCount++;
                        }else{
                            likeCount--;
                        }

                        FirebaseFirestore.getInstance().collection("posts").document(documentId).update("likeCount",likeCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    writeLog("likeCount 올리기 성공");

                                    FirebaseFirestore.getInstance().collection("posts").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if(documentSnapshot.exists()){
                                                if(documentSnapshot.getData().get("likeCount")!=null){
                                                    int likeCount = Integer.parseInt(documentSnapshot.getData().get("likeCount").toString());
                                                    likeCountTextView.setText(likeCount+"");
                                                }
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });

                                }
                            }
                        });
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void setLikeImageViewSelect(){
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("likePost").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists()){
                    if(documentSnapshot.getBoolean("isLike") != null){
                        likeImageView.setSelected(documentSnapshot.getBoolean("isLike"));
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }



}