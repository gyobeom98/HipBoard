package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
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

    private static final int IMAGE_PERMISSION_REQUST_CODE = 503;
    private static final int VIDEO_PERMISSION_REQUST_CODE = 701;
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

    private FloatingActionButton addContentFab , imageFab, videoFab;
    private Animation fabOpen , fabClose;

    private boolean isFabOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        setActionBarTitle("게시글 작성");

        init();

    }

    private void init(){

        user = FirebaseAuth.getInstance().getCurrentUser();

        layout = findViewById(R.id.contentsLayout);
        findViewById(R.id.imageModify).setOnClickListener(onClickListener);
        findViewById(R.id.videoModify).setOnClickListener(onClickListener);
        findViewById(R.id.deleteBtn).setOnClickListener(onClickListener);
        activityWriteLayout = findViewById(R.id.activity_write_post_layout);
        loadingTextView = findViewById(R.id.LoadingSTextView);
        loaderLayout = findViewById(R.id.loaderLayout);
        cardLayout = findViewById(R.id.cardLayout);

        addToolBarView();

        loadingTextView.append("\n동영상을 여러개 올리 시는 경우 \n잠시동안 안나올 수 있습니다.....");

        cardLayout.setOnClickListener(onClickListener);
        getName(user.getUid());
        postInfo = getIntent().getParcelableExtra("postInfo");
        if(postInfo != null){
            updateInit();
            writeLog(postInfo.toString());
            setActionBarTitle("게시글 수정 : " + postInfo.getTitle());
            postInfo.setCreateAt(new Date(getIntent().getLongExtra("createAt",0)));
        }
        showHome();

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.write_post_fab_anim_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.write_post_fab_anim_close);

        addContentFab = findViewById(R.id.add_content_floating_action_btn);
        imageFab = findViewById(R.id.image_floating_action_btn);
        videoFab = findViewById(R.id.video_floating_action_btn);

        addContentFab.setOnClickListener(fabOnClickListener);
        videoFab.setOnClickListener(fabOnClickListener);
        imageFab.setOnClickListener(fabOnClickListener);

    }

    View.OnClickListener fabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.add_content_floating_action_btn : toggleFab(); break;
                case R.id.image_floating_action_btn : toggleFab(); checkPermission("image"); break;
                case R.id.video_floating_action_btn : toggleFab(); checkPermission("video"); break;
            }


        }
    };


    @Override
    protected void onResume() {
        super.onResume();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View v) {
            switch (v.getId()){
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
        final String contentOne = ((EditText)findViewById(R.id.contentEd)).getText().toString();
        hideKeyBoard(new EditText[]{((EditText) findViewById(R.id.writePostTitleEd)),((EditText) findViewById(R.id.contentEd))});
        if (title.trim().length() > 0 && contentOne.trim().length()>0) {
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
            if(title.trim().length()<=0){
                Snackbar.make(((EditText) findViewById(R.id.writePostTitleEd)),"제목을 입력 해 주세요", BaseTransientBottomBar.LENGTH_SHORT).show();
            }else{
                Snackbar.make(((EditText) findViewById(R.id.contentEd)),"내용을 입력 해 주세요", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
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
                    imageView.setPadding(0,50,0,0);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case IMAGE_PERMISSION_REQUST_CODE :
                if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                    startActiNoFinish(GalleryActivity.class, "image",101);
                }else{
                    startingToast("어플 사용중 일부 서비스가 제한 됩니디.\n(어플 정보->권한 에서 설정 변경이 가능합니다.)");
                }
                break;
            case VIDEO_PERMISSION_REQUST_CODE :
                if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                    startActiNoFinish(GalleryActivity.class, "video",101);
                }else{
                    startingToast("어플 사용중 일부 서비스가 제한 됩니디.\n(어플 정보->권한 에서 설정 변경이 가능합니다.)");
                }
                break;
        }
    }

    private void checkPermission(String media){
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            switch (media){
                case "image" :if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_PERMISSION_REQUST_CODE);
                }
                break;
                case "video": if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, VIDEO_PERMISSION_REQUST_CODE);
                }
                break;
            }
        }else{
            startActiNoFinish(GalleryActivity.class, media,101);
        }
    }


    View.OnClickListener toolbarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            storageUpload();
        }
    };

    private void addToolBarView(){
        ImageView toolbarCheckImageView = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams toolbarLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toolbarLayoutParams.gravity = Gravity.RIGHT;
        toolbarLayoutParams.rightMargin = 25;
        toolbarCheckImageView.setLayoutParams(toolbarLayoutParams);
        toolbarCheckImageView.setImageResource(R.drawable.ic_baseline_check_circle_24);
        LinearLayout toolbarLayout = findViewById(R.id.toolbar_menu_lay);
        toolbarCheckImageView.setOnClickListener(toolbarOnClickListener);
        toolbarLayout.addView(toolbarCheckImageView);

    }


    private void toggleFab() {

        if (isFabOpen) {

            addContentFab.setImageResource(R.drawable.ic_baseline_add_24);

            imageFab.startAnimation(fabClose);

            videoFab.startAnimation(fabClose);

            imageFab.setClickable(false);

            videoFab.setClickable(false);

            isFabOpen = false;

        } else {

            addContentFab.setImageResource(R.drawable.ic_baseline_close_24);

            imageFab.startAnimation(fabOpen);

            videoFab.startAnimation(fabOpen);

            imageFab.setClickable(true);

            videoFab.setClickable(true);

            isFabOpen = true;

        }

    }


}