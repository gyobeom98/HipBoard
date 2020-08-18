package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DetailPostActivity extends AppCompatActivity {

    private static final String TAG = "DetailPostActivity";

    private FirebaseFirestore firestore;
    private FirebaseUser user;
    private FirebaseAuth myAuth;
    private ImageView menuImageView;
    private String documentId;
    private PostInfo postInfo;
    private StorageReference storageRef;
    private FirebaseStorage storage;
    private ArrayList<String> imageNames;

    private TextView titleTextView , detailTextView, viewsTextView, dateTextView, likeCountTextView;

    private LinearLayout contentLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_post);

        init();
        imageNames = new ArrayList<>();

        menuImageView.setOnClickListener(onClickListener);
        myAuth = FirebaseAuth.getInstance();
        user = myAuth.getCurrentUser();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(user !=null){
            Intent idIntent = getIntent();
            if(idIntent != null){
                documentId = idIntent.getStringExtra("documentId");
                if(documentId.length()>0){
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
                                        long views = (Long) document.getData().get("views");
                                        long likeCnt = (long) document.getData().get("likeCount");
                                        postInfo = new PostInfo(title,contents,publisher,views,likeCnt,createAt);
                                        postInfo.setDocumentId(documentId);
                                        titleTextView.setText(title);
                                        dateTextView.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(createAt));
                                        viewsTextView.setText("조회수 : " + views);
                                        likeCountTextView.setText(""+likeCnt);
                                        detailTextView.setText(contents.get(0));
                                        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        int imgCnt = 0;
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
                                                ImageView contentImageView = new ImageView(getApplicationContext());
                                                contentImageView.setLayoutParams(layoutParams);
                                                contentImageView.setAdjustViewBounds(true);
                                                contentImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                                contentLayout.addView(contentImageView);
                                                Glide.with(getApplicationContext()).load(content).override(1000).thumbnail(0.1f).into(contentImageView);
                                                writeLog(content);
                                                imgCnt++;
                                            }else {
                                                if (content.length() > 0) {
                                                    TextView contentTextView = new TextView(getApplicationContext());
                                                    contentTextView.setLayoutParams(layoutParams);
                                                    contentTextView.setPadding(10,10,10,100);
                                                    contentLayout.addView(contentTextView);
                                                    contentTextView.setText(content);

                                                }
                                            }
                                        }

                                        if(user.equals(postInfo.getPublisher())){
                                            if(menuImageView.getVisibility()!=View.VISIBLE){
                                                menuImageView.setVisibility(View.VISIBLE);
                                            }else{
                                                menuImageView.setVisibility(View.GONE);
                                            }
                                        }
                                    }else{
                                        writeLog("noSuchData");
                                    }
                                }


                            }else{
                                writeLog("failed");
                            }
                        }
                    });
                }
            }
        }

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.moreImageView : showPopup(v); break;
            }

        }
    };


    private void startActivityFinish(Class c){
        Intent intent = new Intent(this,c);
        startActivity(intent);
        finish();
    }

    private void writeLog(String msg){
        Log.i(TAG,msg);
    }

    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.modifyPost : startingTost("modify"); Intent intent = new Intent(DetailPostActivity.this,WritePostActivity.class);
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


    private void init(){
        menuImageView = findViewById(R.id.moreImageView);
        titleTextView = findViewById(R.id.detail_post_title_textView);
        detailTextView = findViewById(R.id.detail_post_textView);
        viewsTextView = findViewById(R.id.detail_post_viewsTextView);
        dateTextView = findViewById(R.id.detail_post_date_textView);
        likeCountTextView = findViewById(R.id.detail_post_likeCountTextView);
        contentLayout = findViewById(R.id.detail_post_contents_layout);
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


}