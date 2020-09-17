package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gyobeom29.hipboard.CheckImageVideo;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.WritePostImageViewTag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

public class WritePostActivity extends BasicActivity {

    private final String TAG = "WritePostActivity";
    private FirebaseUser user;
    private ArrayList<String> pathList = new ArrayList<>();
    private ArrayList<String> updatePathList = new ArrayList<>();
    private LinearLayout layout;
    int pathCount;
    int successCount;
    private RelativeLayout cardLayout;
    private View selectImageView;
    private RelativeLayout loaderLayout;
    PostInfo postInfo;
    DocumentReference documentReference;
    private int imageNumber;
    private int imagviewCount;
    private String publisherName;
    private FirebaseDatabase firebaseDatabase;
    private TextView loadingTextView;
    private RelativeLayout activityWriteLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        setActionBarTitle("게시글 작성");
        setNavigation();
        user = FirebaseAuth.getInstance().getCurrentUser();
        layout = findViewById(R.id.contentsLayout);
        findViewById(R.id.checkBtn).setOnClickListener(onClickListener);
        findViewById(R.id.writePostImagBtn).setOnClickListener(onClickListener);
        findViewById(R.id.writePostVideoBtn).setOnClickListener(onClickListener);
        findViewById(R.id.imageModify).setOnClickListener(onClickListener);
        findViewById(R.id.videoModify).setOnClickListener(onClickListener);
        findViewById(R.id.deleteBtn).setOnClickListener(onClickListener);
        activityWriteLayout = findViewById(R.id.activity_write_post_layout);
        loadingTextView = findViewById(R.id.LoadingSTextView);
        loadingTextView.append("\n동영상을 여러개 올리 시는 경우 \n잠시동안 안나올 수 있습니다.....");
        loaderLayout = findViewById(R.id.loaderLayout);
        cardLayout = findViewById(R.id.cardLayout);
        cardLayout.setOnClickListener(onClickListener);
        getName(user.getUid());
        postInfo = getIntent().getParcelableExtra("postInfo");
        if(postInfo != null){
            updateInit();
            writeLog(postInfo.toString());
            setActionBarTitle("게시글 수정 : " + postInfo.getTitle());
            postInfo.setCreateAt(new Date(getIntent().getLongExtra("createAt",0)));
        }

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.checkBtn:

                    storageUpload();
                    break;
                case R.id.writePostImagBtn:
                    startActiNoFinish(GalleryActivity.class, "image",101);
                    break;
                case R.id.writePostVideoBtn:
                    startActiNoFinish(GalleryActivity.class, "video",101);
                    break;
                case R.id.cardLayout:
                    if(cardLayout.getVisibility() == View.VISIBLE){
                        cardLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.imageModify: startActiNoFinish(GalleryActivity.class,"image",102);  break;
                case R.id.videoModify: startActiNoFinish(GalleryActivity.class,"video",102); break;
                case R.id.deleteBtn:
                    if(((LinearLayout)selectImageView.getParent()).getChildCount()<=2){
                        layout.removeView((View) selectImageView.getParent());
                    }else {
                        layout.removeView(selectImageView);

                    }
                    break;
            }
        }
    };

    private void storageUpload() {

        final String title = ((EditText) findViewById(R.id.writePostTitleEd)).getText().toString();
        if (title.trim().length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            activityWriteLayout.setEnabled(false);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            documentReference = firebaseFirestore.collection("posts").document();
            if(postInfo!=null) documentReference = firebaseFirestore.collection("posts").document(postInfo.getDocumentId());
            final ArrayList<String> contentList = new ArrayList<>();
            Date createAt = new Date();
            long views = 0;
            long likeCount = 0;
            int childCount = layout.getChildCount();
            writeLog("childCount : " + childCount);
            if(postInfo!=null){
//                childCount = childCount-1;
                createAt = postInfo.getCreateAt();
                views = postInfo.getViews();
                likeCount = postInfo.getLikeCount();
                writeLog("childCount : " + childCount);
            }
            imagviewCount = 0;
            for (int i = 0; i < childCount; i++) {
                writeLog("pathCount R : " + pathCount);
                View view = layout.getChildAt(i);
                if (view instanceof EditText) {
                    String text = ((EditText) view).getText().toString();
                    if (text.length() > 0) {
                        contentList.add(text);
                    }
                } else {
                    contentList.add(pathList.get(pathCount));
                    imagviewCount++;
                    for (String str: contentList) {
                        writeLog("str : " + str);
                    }
                    String[] pathArray =  pathList.get(pathCount).split("\\.");
                    writeLog("pathArray : " + pathArray);
                    String type = pathArray[pathArray.length-1];
                    if(postInfo!=null){
                        if(type.indexOf('?') >0)
                            type = type.substring(0,type.indexOf("?"));
                    }
                    String documentId = documentReference.getId();
                    if(postInfo != null)
                        documentId = postInfo.getDocumentId();
                    boolean isEquals = false;
                    for (int j=0; j < updatePathList.size();j++){
                        if(updatePathList.get(j).equals(pathList.get(pathCount))){
                            isEquals = true;
                            break;
                        }else{
                            isEquals = false;
                        }
                    }
                    if(postInfo==null) isEquals = false;
                    final StorageReference mountainImagesRef = storageRef.child("posts/" + documentId + "/" + pathCount + "." + type);
                    if(!isEquals) {
                        try {
                            InputStream stream = new FileInputStream(new File(pathList.get(pathCount)));
                            StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", "" + (contentList.size() - 1)).build();
                            UploadTask uploadTask = mountainImagesRef.putStream(stream, metadata);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                    // ...
                                    final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));
                                    mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            writeLog("uri : " + uri);
                                            writeLog("index : " + index);
                                            contentList.set(index, uri.toString());
                                            writeLog("pathCount : " + pathCount);
                                            writeLog("successCount : " + successCount);
                                            successCount++;
                                            writeLog("imageViewCount : " + imagviewCount);
                                            if (pathCount == successCount) {
                                                // 완료
                                                for (String str : contentList) {
                                                    writeLog("contentList : " + str);
                                                }
                                                long vies = 0;
                                                long likeC = 0;
                                                Date create = new Date();
                                                if(postInfo != null){
                                                    vies = postInfo.getViews();
                                                    likeC = postInfo.getLikeCount();
                                                    create = postInfo.getCreateAt();
                                                }
                                                PostInfo postInfos = new PostInfo(title, contentList, user.getUid(), vies,likeC,create,publisherName);
                                                writeLog(postInfos.toString());
                                                storeUpload(documentReference, postInfos);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            writeLog(e.toString());
                                            loaderLayout.setVisibility(View.GONE);
                                            activityWriteLayout.setEnabled(true);
                                            e.printStackTrace();
                                        }
                                    });


                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("로그", "에러 : " + e.toString());
                        }
                    }else{
                        for (String strs : contentList) {
                            writeLog("strs : " + strs);
                        }
                        PostInfo postInfos = new PostInfo(title, contentList, user.getUid(), views, likeCount, createAt,publisherName);
                        writeLog(postInfos.toString());
                        storeUpload(documentReference, postInfos);
                    }
                    pathCount++;
                }
            }
            if(pathList.size() == 0){
                for (String strs : contentList) {
                    writeLog("strs : " + strs);
                }
                PostInfo postInfos = new PostInfo(title, contentList, user.getUid(),views,likeCount,createAt,publisherName);
                writeLog(postInfos.toString());
                storeUpload(documentReference, postInfos);
            }
        } else {
            startingToast("회원 정보를 입력 해주세요");
        }
    }

    private void storeUpload(final DocumentReference documentReference, PostInfo postInfos) {

        documentReference.set(postInfos)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        loaderLayout.setVisibility(View.GONE);
                        activityWriteLayout.setEnabled(true);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        loaderLayout.setVisibility(View.GONE);
                        activityWriteLayout.setEnabled(true);
                    }
                });


    }

    private void writeLog(String msg) {
        Log.i(TAG, msg);
    }

    private void startingToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        writeLog("onActivitResult 넘어옴");
        writeLog(requestCode + ", " + resultCode);
        switch (requestCode) {
            case 101: {
                if (resultCode == RESULT_OK) {
                    String profilePath = data.getStringExtra("profilePath");
                    writeLog("profilePath : " + profilePath);
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

//                    LinearLayout linearLayout = new LinearLayout(WritePostActivity.this);
//                    linearLayout.setLayoutParams(layoutParams);
//                    linearLayout.setOrientation(LinearLayout.VERTICAL);
//                    layout.addView(linearLayout);

                    ImageView imageView = new ImageView(WritePostActivity.this);
                    imageView.setLayoutParams(layoutParams);
                    WritePostImageViewTag writePostImageViewTag = new WritePostImageViewTag(pathList.size(),profilePath);
                    imageView.setTag(writePostImageViewTag);
                    pathList.add(profilePath);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cardLayout.setVisibility(View.VISIBLE);
                            selectImageView =v;

                        }
                    });
                    Glide.with(this).load(profilePath).override(1000).into(imageView);
                    layout.addView(imageView);

                    EditText editText = new EditText(WritePostActivity.this);
                    editText.setLayoutParams(layoutParams);
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
                    editText.setHint("내용");
                    layout.addView(editText);
                    writeLog("pathListSize : " + pathList.size());
                }
            } break;
            case 102: {
                if(resultCode==RESULT_OK){
                    String path = data.getStringExtra("profilePath");
                    if(selectImageView instanceof PlayerView){
                        int selectIndex = layout.indexOfChild(selectImageView);
                        writeLog("selectIndex : " + selectIndex);
                        layout.removeViewAt(selectIndex);
                        ImageView defaultImageView = new ImageView(getApplicationContext());
                        defaultImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        defaultImageView.setAdjustViewBounds(true);
                        layout.addView(defaultImageView,selectIndex);
                        Glide.with(this).load(path).override(1000).into( defaultImageView);
                        pathList.set(selectIndex-1,path);
                    }else {
                        Glide.with(this).load(path).override(1000).into((ImageView) selectImageView);
                        int selectIndex = layout.indexOfChild(selectImageView);
                        writeLog("selectIndex : " + selectIndex);
                        pathList.set(selectIndex-1, path);
                    }
                }
            }
        }
    }

    private void startActiNoFinish(Class c, String media,int requestCode) {
        Intent intent = new Intent(WritePostActivity.this, c);
        intent.putExtra("media", media);
        startActivityForResult(intent, requestCode);
    }

    private void updateInit(){
        ((Button)findViewById(R.id.checkBtn)).setText("수정");
        ((EditText)findViewById(R.id.writePostTitleEd)).setText(postInfo.getTitle());
        ArrayList<String> contents = (ArrayList<String>) postInfo.getContents();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        writeLog("contentSize : " + contents.size());
//        LinearLayout linearLayout = new LinearLayout(WritePostActivity.this);
//        linearLayout.setLayoutParams(layoutParams);
//        linearLayout.setOrientation(LinearLayout.VERTICAL);
//        linearLayout.setTag("la");
//        layout.addView(linearLayout);
        for(int i = 0; i < contents.size();i++) {

            String content = contents.get(i);
            if (Patterns.WEB_URL.matcher(content).matches()) {
                pathList.add(content);
                updatePathList.add(content);

                if(CheckImageVideo.isVideo(content)){
                    writeLog("여기 옴 ");
                    final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(getApplicationContext());
                    DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                            Util.getUserAgent(getApplicationContext(), getString(R.string.app_name)));
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
                    playerView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cardLayout.setVisibility(View.VISIBLE);
                            selectImageView = v;

                        }
                    });
                    layout.addView(playerView);
                }else {
                    ImageView contentImageView = new ImageView(getApplicationContext());
                    contentImageView.setLayoutParams(layoutParams);
                    contentImageView.setAdjustViewBounds(true);
                    layout.addView(contentImageView);
                    Glide.with(getApplicationContext()).load(content).override(1000).thumbnail(0.1f).into(contentImageView);
                    final int finalI = i;
                    contentImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cardLayout.setVisibility(View.VISIBLE);
                            selectImageView = (ImageView) v;
                        }
                    });
                }
            }else {
                if (i !=0) {
                    writeLog("0이 아닐때 까지 옴");
                    if (content.trim().length() > 0) {
                        EditText contentEditView = new EditText(getApplicationContext());
                        contentEditView.setLayoutParams(layoutParams);
                        contentEditView.setPadding(10, 10, 10, 100);
                        layout.addView(contentEditView);
                        contentEditView.setText(content);
                    }
                }else{
                    ((EditText)findViewById(R.id.contentEd)).setText(content);
                }
            }

        }
        writeLog("layoutChildCount : " +layout.getChildCount());
        writeLog("pathListAfterInit : " + pathList.size());
        writeLog("contentSize : " + contents.size());
    }

    private PlayerView getPlayerView(){
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        PlayerView playerView = (PlayerView) inflater.inflate(R.layout.view_content_player,null,false);
        return playerView;
    }

    private void getName(String publisher){
        FirebaseFirestore.getInstance().collection("users").document(publisher).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    publisherName = documentSnapshot.get("name").toString();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                writeLog("getNamesError : " + e.toString());
            }
        });
    }

}