package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.adapter.MainPostAdapter;
import com.gyobeom29.hipboard.adapter.MyPostAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostListActivity extends BasicActivity {

    private RecyclerView postListRecyclerView;
    private FirebaseUser user;
    private ArrayList myPostList;
    private static final String TAG = "PostListActivity";
    private MainPostAdapter myAdapter;
    private LinearLayout wrapLayout,noItemLayout;
    private FloatingActionButton myFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPosts();
        setNavigation(this.getClass());
    }

    private void init(){
        setActionBarTitle("Post List");
        user = FirebaseAuth.getInstance().getCurrentUser();
        postListRecyclerView = findViewById(R.id.post_list_ac_recyclerView);
        noItemLayout = findViewById(R.id.no_item_layout);
        noItemLayout.setVisibility(View.GONE);
        postListRecyclerView.setHasFixedSize(true);
        postListRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false));
        wrapLayout = findViewById(R.id.post_list_ac_wrap_layout);
        myFloatingActionButton = findViewById(R.id.post_list_floating_btn);
        myFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goActivity(WritePostActivity.class);
            }
        });
    }

    private void getPosts(){
        myPostList = new ArrayList<>();
        wrapLayout.removeAllViewsInLayout();
       FirebaseFirestore.getInstance().collection("posts").orderBy("createAt", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
           @Override
           public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
               if(queryDocumentSnapshots.size()>0){
                   for (DocumentSnapshot dost : queryDocumentSnapshots) {
                       String title = dost.getData().get("title").toString();
                       String publusherName = dost.getData().get("publisherName").toString();
                       Date createAt = dost.getDate("createAt");
                       String likeCnt = dost.getData().get("likeCount").toString();
                       ArrayList<String> content = (ArrayList<String>)dost.getData().get("contents");
                       PostInfo postInfo = new PostInfo();
                       postInfo.setTitle(title);
                       postInfo.setContents(content);
                       postInfo.setPublisherName(publusherName);
                       postInfo.setCreateAt(createAt);
                       postInfo.setLikeCount(Integer.parseInt(likeCnt));
                       postInfo.setDocumentId(dost.getId());
                       myPostList.add(postInfo);
                   }
                   myAdapter = new MainPostAdapter(myPostList,PostListActivity.this);
                   postListRecyclerView.setAdapter(myAdapter);
                   myAdapter.notifyDataSetChanged();
               }else{
                   noItemLayout.setVisibility(View.VISIBLE);
               }

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


    private void goActivity(Class c){
        Intent intent = new Intent(getApplicationContext(),c);
        startActivity(intent);
    }

}