package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.adapter.MainPostAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends BasicActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private MainPostAdapter mainPostAdapter;
    FirebaseUser user;
    FirebaseFirestore firestore;


    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActionBarTitle(R.string.app_name);
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = findViewById(R.id.mainRecyclerView);
        findViewById(R.id.logoutButton).setOnClickListener(onClickListener);
        findViewById(R.id.member_in_it_btn).setOnClickListener(onClickListener);
        findViewById(R.id.mainFloatBtn).setOnClickListener(onClickListener);
        findViewById(R.id.userInfo_btn).setOnClickListener(onClickListener);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this,RecyclerView.VERTICAL,false));

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            findViewById(R.id.logoutButton).setVisibility(View.GONE);
            findViewById(R.id.member_in_it_btn).setVisibility(View.GONE);
            startActiNoFinish(SignUpActivity.class);
        }else{
            findViewById(R.id.logoutButton).setVisibility(View.VISIBLE);
            findViewById(R.id.member_in_it_btn).setVisibility(View.VISIBLE);
            Log.e("UId" , user.getUid());
            firestore = FirebaseFirestore.getInstance();
            DocumentReference docRef = firestore.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document != null) {
                            Log.d("document" , ""+document);
                            Log.e("Boolean",""+document.exists());
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                if(document.getData() == null) {
                                    startActi(MemberInitActivity.class);
                                }else{
                                    Log.e("documentData", "data" + document.getData());
                                }
                            } else {
                                startActi(MemberInitActivity.class);
                                Log.d(TAG, "No such document");
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
            final ArrayList<PostInfo> postList = new ArrayList<>();
            firestore.collection("posts").orderBy("createAt", Query.Direction.DESCENDING).limit(10).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        Log.i("documentId", document.getId());
                                        String documentId = document.getId();
                                        String title = document.getData().get("title").toString();
                                        ArrayList<String> contents = (ArrayList<String>) document.getData().get("contents");
                                        String publisher = document.getData().get("publisher").toString();
                                        Date createAt = new Date(document.getDate("createAt").getTime());
                                        long views = (Long) document.getData().get("views");
                                        long likeCnt = (long) document.getData().get("likeCount");
                                        PostInfo info = new PostInfo(title, contents, publisher, views, likeCnt, createAt);
                                        info.setDocumentId(documentId);
                                        postList.add(info);
                                        writeLog(info.toString());

                                }
                                mainPostAdapter = new MainPostAdapter(postList,MainActivity.this);
                                recyclerView.setAdapter(mainPostAdapter);
                                mainPostAdapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });

        }

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.logoutButton : mAuth.signOut(); startActiNoFinish(SignUpActivity.class); break;
                case R.id.member_in_it_btn : startActiNoFinish(MemberInitActivity.class); break;
                case R.id.mainFloatBtn : startActiNoFinish(WritePostActivity.class); break;
                case R.id.userInfo_btn : startActiNoFinish(UserInfoActivity.class); break;
            }
        }
    };

    private void startActi(Class c){
        Intent intent = new Intent(MainActivity.this,c);
        startActivity(intent);
        finish();
    }

    private void startActiNoFinish(Class c){
        Intent intent = new Intent(MainActivity.this,c);
        startActivity(intent);
    }


    private void startingToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    private void writeLog(String msg){
        Log.i(TAG,msg);
    }

    private Date getPreviousMonth(){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH,-1);
        Date date = c.getTime();
        return date;
    }

}