package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.gyobeom29.hipboard.MemberInfo;
import com.gyobeom29.hipboard.R;

import java.util.ArrayList;

public class DeleteUserActivity extends NoActiveBasicActivity {

    private static final String TAG = "DeleteUserActivity";
    private FirebaseUser user;

    private MemberInfo myInfo;

    private EditText deleteNameEd, deleteEmailEd, deletePhoneEd;

    private String userUid;

    private RelativeLayout loaderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_user);

        init();

    }

    private void init(){
        showHome();
        setActionBarTitle("회원 계정 탈퇴");
        user = FirebaseAuth.getInstance().getCurrentUser();
        userUid = user.getUid();
        deleteNameEd = findViewById(R.id.deleteUserNameEditTextView);
        deleteEmailEd = findViewById(R.id.deleteUserEmailEditTextView);
        deletePhoneEd = findViewById(R.id.deleteUserPhoneEditTextView);
        loaderLayout = findViewById(R.id.loaderLayout);
        addToolBarView();
        getMyInfo();
    }


    View.OnClickListener toolbarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myInfo !=null){
                hideKeyBoard();
                String name = deleteNameEd.getText().toString();
                String email = deleteEmailEd.getText().toString();
                String phone = deletePhoneEd.getText().toString();

                if(name.length()>0 && email.length()>0 && phone.length()>0){
                    if(name.equals(myInfo.getName()) && email.equals(user.getEmail()) && phone.equals(myInfo.getPhone())){
                        loaderLayout.setVisibility(View.VISIBLE);
                        deleteUser();
                    }
                }else{
                    Snackbar.make(v,"입력 하지 않은 값이 있습니다.", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(),"정보 로딩이 완료 되지 않았습니다.\n잠시 후 다시 시도 해주세요",Toast.LENGTH_SHORT).show();
            }


        }
    };

    private void getMyInfo(){

        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String name = documentSnapshot.getData().get("name").toString();
                    String phone = documentSnapshot.getData().get("phone").toString();
                    String address = documentSnapshot.getData().get("address").toString();
                    String birth = documentSnapshot.getData().get("birth").toString();
                    String photoUrl = documentSnapshot.getData().get("photoUrl").toString();
                    myInfo = new MemberInfo(name,phone,birth,address,photoUrl);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }

    private void getUserComment(){
        FirebaseFirestore.getInstance().collection("posts").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                writeLog("size : " + queryDocumentSnapshots.size());
                for (DocumentSnapshot dost:queryDocumentSnapshots) {
                    deleteComments(dost.getId());
                }
                deletePosts();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                writeLog("가져오기 실패");
                e.printStackTrace();
            }
        });

    }

    private void deleteUser(){
        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                getUserComment();
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

    private void deleteComments(final String dostId){
        FirebaseFirestore.getInstance().collection("posts").document(dostId).collection("comments").whereEqualTo("writer",userUid).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot dos:queryDocumentSnapshots) {
                    FirebaseFirestore.getInstance().collection("posts").document(dostId).collection("comments").document(dos.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                writeLog("댓글 삭제 성공");
                            }else{
                                writeLog("댓글 삭제 실패");
                                task.getException().printStackTrace();
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


    private void deletePosts(){
        FirebaseFirestore.getInstance().collection("posts").whereEqualTo("publisher",userUid).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                writeLog("posts Size :" + queryDocumentSnapshots.size());
                for (final DocumentSnapshot dst: queryDocumentSnapshots) {
                    writeLog("dsts : " + dst.getData());
                    deletePostImage(dst.getId());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void deleteUserProfileImage(){

        FirebaseStorage.getInstance().getReference().child("users/"+userUid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    writeLog("유저 프로필 삭제 ");
                    deleteUserInfo();
                }else{
                    writeLog("유저 프로필 삭제 실패");
                }
            }
        });

    }

    private void deleteUserInfo(){
        FirebaseFirestore.getInstance().collection("users").document(userUid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loaderLayout.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    writeLog("userInfo 삭제");
                }else{
                    writeLog("userInfo 삭제 실패");
                }
                goToSignUp();
            }
        });
    }
    private void goToSignUp(){
        Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void deletePost(String documentId){
        FirebaseFirestore.getInstance().collection("posts").document(documentId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    writeLog("post 삭제 성공");
                    deleteUserProfileImage();
                }else{
                    writeLog("post 삭제 실패");
                    task.getException().printStackTrace();
                }
            }
        });
    }

    private void deletePostImage(final String documentId){
        FirebaseStorage.getInstance().getReference(documentId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                deletePost(documentId);
            }
        });
    }

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

    private void hideKeyBoard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(deleteNameEd.isFocused()){
            imm.hideSoftInputFromWindow(deleteNameEd.getWindowToken(), 0);
        }else if(deleteEmailEd.isFocused()){
            imm.hideSoftInputFromWindow(deleteEmailEd.getWindowToken(), 0);
        }else if(deletePhoneEd.isFocused()){
            imm.hideSoftInputFromWindow(deletePhoneEd.getWindowToken(), 0);
        }
    }


}