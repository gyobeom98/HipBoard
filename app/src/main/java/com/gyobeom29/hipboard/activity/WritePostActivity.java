package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;

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
    private LinearLayout layout;
    int pathCount;
    int successCount;
    private RelativeLayout cardLayout;
    private ImageView selectImageView;
    private RelativeLayout loaderLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        layout = findViewById(R.id.contentsLayout);
        findViewById(R.id.checkBtn).setOnClickListener(onClickListener);
        findViewById(R.id.writePostImagBtn).setOnClickListener(onClickListener);
        findViewById(R.id.writePostVideoBtn).setOnClickListener(onClickListener);
        findViewById(R.id.imageModify).setOnClickListener(onClickListener);
        findViewById(R.id.videoModify).setOnClickListener(onClickListener);
        findViewById(R.id.deleteBtn).setOnClickListener(onClickListener);
        loaderLayout = findViewById(R.id.loaderLayout);
        cardLayout = findViewById(R.id.cardLayout);
        cardLayout.setOnClickListener(onClickListener);
        user = FirebaseAuth.getInstance().getCurrentUser();
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
                    writeLog(selectImageView.toString());
                    ((View)selectImageView.getParent()).setBackgroundColor(R.color.colorBlack);
                    layout.removeView((View)selectImageView.getParent());
                    break;
            }
        }
    };

    private void storageUpload() {

        final String title = ((EditText) findViewById(R.id.writePostTitleEd)).getText().toString();

        if (title.trim().length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            final DocumentReference documentReference = firebaseFirestore.collection("posts").document();
            final ArrayList<String> contentList = new ArrayList<>();

            for (int i = 0; i < layout.getChildCount(); i++) {
                View view = layout.getChildAt(i);
                if (view instanceof EditText) {
                    String text = ((EditText) view).getText().toString();
                    if (text.length() > 0) {
                        contentList.add(text);
                    }
                } else {
                    contentList.add(pathList.get(pathCount));
                    String[] pathArray =  pathList.get(pathCount).split("\\.");
                    writeLog("pathArray : " +pathArray[pathArray.length-1].trim());
                    final StorageReference mountainImagesRef = storageRef.child("posts/" + documentReference.getId() + "/" + pathCount + "." + pathArray[pathArray.length-1].trim());
                    try {
                        InputStream stream = new FileInputStream(new File(pathList.get(pathCount)));
                        StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", "" + (contentList.size() - 1)).build();
                        UploadTask uploadTask = mountainImagesRef.putStream(stream, metadata);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                loaderLayout.setVisibility(View.GONE);
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
                                        contentList.set(index, uri.toString());
                                        successCount++;
                                        if (pathCount == successCount) {
                                            // 완료
                                            for (String str : contentList) {
                                                writeLog("contentList : " + str);
                                            }
                                            PostInfo postInfo = new PostInfo(title, contentList, user.getUid(), new Date());
                                            writeLog(postInfo.toString());
                                            storeUpload(documentReference, postInfo);
                                        }
                                    }
                                });
                            }
                        });

                    } catch (IOException e) {
                        Log.e("로그", "에러 : " + e.toString());
                    }
                    pathCount++;
                }
            }
            if(pathList.size() == 0){
                PostInfo postInfo = new PostInfo(title, contentList, user.getUid(), new Date());
                writeLog(postInfo.toString());
                storeUpload(documentReference, postInfo);
            }
        } else {
            startingToast("회원 정보를 입력 해주세요");
        }
    }

    private void storeUpload(DocumentReference documentReference, PostInfo postInfo) {

        documentReference.set(postInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        loaderLayout.setVisibility(View.GONE);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        loaderLayout.setVisibility(View.GONE);
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
                    pathList.add(profilePath);
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    LinearLayout linearLayout = new LinearLayout(WritePostActivity.this);
                    linearLayout.setLayoutParams(layoutParams);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    layout.addView(linearLayout);

                    ImageView imageView = new ImageView(WritePostActivity.this);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cardLayout.setVisibility(View.VISIBLE);
                            selectImageView = (ImageView)v;
                        }
                    });
                    Glide.with(this).load(profilePath).override(1000).into(imageView);
                    linearLayout.addView(imageView);


                    EditText editText = new EditText(WritePostActivity.this);
                    editText.setLayoutParams(layoutParams);
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
                    editText.setHint("내용");
                    linearLayout.addView(editText);
                }
            } break;
            case 102: {
                if(resultCode==RESULT_OK){
                    String path = data.getStringExtra("profilePath");
                    Glide.with(this).load(path).override(1000).into(selectImageView);
                }
            }
        }
    }

    private void startActiNoFinish(Class c, String media,int requestCode) {
        Intent intent = new Intent(WritePostActivity.this, c);
        intent.putExtra("media", media);
        startActivityForResult(intent, requestCode);
    }

}