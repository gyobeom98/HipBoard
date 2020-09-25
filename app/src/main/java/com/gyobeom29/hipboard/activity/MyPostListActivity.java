package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.adapter.MyPostAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyPostListActivity extends BasicActivity {

    ArrayList<PostInfo> myPostList;

    private static final String TAG = "MyPostListActivity";

    MyPostAdapter myPostAdapter;
    RecyclerView myRecyclerView;
    FirebaseUser user;

    LinearLayout noItemLayout;
    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post_list);
        init();
    }

    @SuppressLint("ResourceType")
    private void init(){

        Intent getMyIntent =  getIntent();
        user = FirebaseAuth.getInstance().getCurrentUser();
        myRecyclerView = findViewById(R.id.my_post_list_recyclerView);
        myRecyclerView.setHasFixedSize(true);
        animation = new AlphaAnimation(0, 1); animation.setDuration(1000);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false));
        String distinction = getMyIntent.getStringExtra("Distinction");
        noItemLayout = findViewById(R.id.no_item_layout);
        noItemLayout.setVisibility(View.GONE);
        showHome();

        switch (distinction){
            case "myPost" :  setActionBarTitle("My Post"); getMyPosts(); break;
            case "myLikePost" : setActionBarTitle("My Like Post"); getDocumentId(); break;
        }


    }



    private void getMyPosts(){
        myPostList = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("posts").whereEqualTo("publisher",user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots != null)
                    writeLog("size : " + queryDocumentSnapshots.size());else writeLog("없음");
                    if(queryDocumentSnapshots.size()<=0){
                        noItemLayout.setVisibility(View.VISIBLE);
                        noItemLayout.setAnimation(animation);
                    }
                for (DocumentSnapshot dost : queryDocumentSnapshots) {
                    String title = dost.getData().get("title").toString();
                    String publusherName = dost.getData().get("publisherName").toString();
                    Date createAt = dost.getDate("createAt");
                    String likeCnt = dost.getData().get("likeCount").toString();
                    String smallContent = ((ArrayList<String>)dost.getData().get("contents")).get(0);
                    PostInfo postInfo = new PostInfo();
                    postInfo.setTitle(title);
                    List<String> contentLs = new ArrayList<>();
                    contentLs.add(smallContent);
                    postInfo.setContents(contentLs);
                    postInfo.setPublisherName(publusherName);
                    postInfo.setCreateAt(createAt);
                    postInfo.setLikeCount(Integer.parseInt(likeCnt));
                    postInfo.setDocumentId(dost.getId());
                    myPostList.add(postInfo);
                }
                myPostAdapter = new MyPostAdapter(myPostList,getApplicationContext());
                myRecyclerView.setAdapter(myPostAdapter);
                myPostAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                writeLog("실패!");
            }
        });

    }

    private void getDocumentId(){
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("likePost").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.size()<=0){
                    noItemLayout.setVisibility(View.VISIBLE);
                    noItemLayout.setAnimation(animation);
                }
                for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()) {
                    writeLog("query : " + doc);
                    writeLog("document : " + doc.getData().get("users"));
                    writeLog("asd : " + doc.getId());
                    getLikePostsIsLike(doc.getId());
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void getLikePostsIsLike(final String documentId){
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("likePost").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    if(documentSnapshot.getBoolean("isLike") != null){
                        if(documentSnapshot.getBoolean("isLike")){
                            getPosts(documentId);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void getPosts(String documentId){
        myPostList = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("posts").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot docst) {

                if(docst.exists()){
                    if(docst.getData()!=null){
                        String title = docst.getData().get("title").toString();
                        String publusherName = docst.getData().get("publisherName").toString();
                        Date createAt = docst.getDate("createAt");
                        String likeCnt = docst.getData().get("likeCount").toString();
                        String smallContent = ((ArrayList<String>)docst.getData().get("contents")).get(0);
                        PostInfo myPostInfo = new PostInfo();

                        ArrayList<String> contents = new ArrayList<>();
                        contents.add(smallContent);

                        myPostInfo.setDocumentId(docst.getId());
                        myPostInfo.setTitle(title);
                        myPostInfo.setPublisherName(publusherName);
                        myPostInfo.setCreateAt(createAt);
                        myPostInfo.setContents(contents);
                        myPostInfo.setLikeCount(Integer.parseInt(likeCnt));
                        myPostList.add(myPostInfo);
                    }
                }
                writeLog("myPostListSize : " + myPostList.size());
                myPostAdapter = new MyPostAdapter(myPostList,getApplicationContext());
                myRecyclerView.setAdapter(myPostAdapter);
                myPostAdapter.notifyDataSetChanged();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }




    private void writeLog(String msg){
        Log.i(TAG,msg);
    }


}