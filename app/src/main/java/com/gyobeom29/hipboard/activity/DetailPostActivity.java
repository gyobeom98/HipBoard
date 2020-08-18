package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

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
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;

import java.util.ArrayList;
import java.util.Date;

public class DetailPostActivity extends AppCompatActivity {

    private static final String TAG = "DetailPostActivity";

    private FirebaseFirestore firestore;
    private FirebaseUser user;
    private FirebaseAuth myAuth;
    private ImageView menuImageView;
    private String documentId;
    private PostInfo postInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_post);

        menuImageView = findViewById(R.id.moreImageView);
        menuImageView.setOnClickListener(onClickListener);


        myAuth = FirebaseAuth.getInstance();
        user = myAuth.getCurrentUser();
        if(user !=null){
            Intent idIntent = getIntent();
            if(idIntent != null){
                documentId = idIntent.getStringExtra("documentId");
                if(documentId.length()>0){
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
        }else{

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(user !=null){
            if(postInfo.getPublisher().equals(user.getUid())){
                if(menuImageView.getVisibility()!=View.VISIBLE){
                    menuImageView.setVisibility(View.VISIBLE);
                }
            }
        }else{
            finish();
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
                    case R.id.modifyPost : startingTost("modify");  return true;
                    case R.id.deletePost :
                        delete();
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

    private void delete(){
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


}