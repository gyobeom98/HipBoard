package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.gyobeom29.hipboard.MemberInfo;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.SettingService;

import java.util.ArrayList;

public class UserInfoActivity extends BasicActivity {

    private static final String TAG ="UserInfoActivity";

    FirebaseUser user;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    MemberInfo memberInfo;
    TextView userNameTextView;
    ImageView userProfileImageView;
    private Switch pushSwitch;

    GridLayout goToMyPostLayout, goToMyLikePostLayout,goToUpdateMyInfoLayout,goToDeleteUserLayout;

    TextView myPostCountTextView, myLikePostCountTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        setActionBarTitle("My Page");


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        user.getEmail();
        init();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.go_to_update_my_info : goToWhere(MemberInitActivity.class,memberInfo,"memberInfo"); break;
                case R.id.go_to_my_post_layout : goToWhere(MyPostListActivity.class,"myPost","Distinction"); break;
                case R.id.go_to_my_like_post_layout : goToWhere(MyPostListActivity.class,"myLikePost","Distinction"); break;
                case R.id.go_to_delete_user : goToWhere(DeleteUserActivity.class); break;
            }
        }
    };

    private void goToWhere(Class c , Object o, String intentTag){
        Intent intent = new Intent(UserInfoActivity.this,c);
        if(o instanceof MemberInfo){
            intent.putExtra(intentTag,(MemberInfo) o);
        }else{
            intent.putExtra(intentTag,(String) o);
        }
        startActivity(intent);
    }

    private void goToWhere(Class c){
        Intent intent = new Intent(UserInfoActivity.this,c);
        startActivity(intent);
    }

    private void writeLog(String msg){
        Log.i(TAG,msg);
    }

    private void init(){

        userNameTextView = findViewById(R.id.myPageUserName);
        myPostCountTextView = findViewById(R.id.my_posts_count_textView);
        myLikePostCountTextView = findViewById(R.id.my_like_posts_count_textView);
        userProfileImageView = findViewById(R.id.myPageProfileImageView);

        goToMyPostLayout = findViewById(R.id.go_to_my_post_layout);
        goToMyLikePostLayout = findViewById(R.id.go_to_my_like_post_layout);
        goToUpdateMyInfoLayout = findViewById(R.id.go_to_update_my_info);
        goToDeleteUserLayout = findViewById(R.id.go_to_delete_user);

        pushSwitch = findViewById(R.id.switch_push);
        if(SettingService.setting == null){
            pushSwitch.setChecked(true);
        }else{
            pushSwitch.setChecked(SettingService.setting.isPushOn());
        }


        goToUpdateMyInfoLayout.setOnClickListener(onClickListener);
        goToMyPostLayout.setOnClickListener(onClickListener);
        goToMyLikePostLayout.setOnClickListener(onClickListener);
        goToDeleteUserLayout.setOnClickListener(onClickListener);

        pushSwitch.setOnCheckedChangeListener(onCheckedChangeListener);

    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            switch (buttonView.getId()){
                case R.id.switch_push :  writeLog("isCheckEd : " + isChecked);  SettingService.setting.setPushOn(isChecked); SettingService.UpdateSetting(user,isChecked); break;
            }

        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        setNavigation(this.getClass());
        setInfo();
        getMyPosts();
        getDocumentId();
    }

    private void setInfo(){

        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot ds) {
                if(ds.exists()){
                    String name = ds.getData().get("name").toString();
                    String phone = ds.getData().get("phone").toString();
                    String birth = ds.getData().get("birth").toString();
                    String address = ds.getData().get("address").toString();
                    String imagePath = ds.getData().get("photoUrl").toString();
                    memberInfo = new MemberInfo(name,phone,birth,address,imagePath);
                    userNameTextView.setText(name);
                    writeLog("imagePath : "  + imagePath);
                    int imgRndInt = ((int)Math.random()+1)*10;
                    Glide.with(getApplicationContext()).load(imagePath+"?v="+imgRndInt).centerCrop().override(0).into(userProfileImageView);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }



    private void getMyPosts(){
        FirebaseFirestore.getInstance().collection("posts").whereEqualTo("publisher",user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null){
                    writeLog("size : " + queryDocumentSnapshots.size());
                    int myPostCount = queryDocumentSnapshots.size();
                    String myPostCountStr = myPostCount+"";
                    if(myPostCount>10000){
                        myPostCountStr = (myPostCount/1000) + " K";
                    }else if(myPostCount>1000){
                        myPostCountStr = (myPostCount/1000)+"";
                        if(myPostCount%1000>100){
                            myPostCountStr+="."+(myPostCount%1000)/100;
                        }
                        myPostCountStr += " K";
                    }

                    myPostCountTextView.setText(myPostCountStr);
                } else {
                    writeLog("없음");
                    myPostCountTextView.setText("0");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                writeLog("실패!");
                myPostCountTextView.setText("0");
            }
        });
        }

    private void getDocumentId(){
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("likePost").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots!=null){
                    writeLog("document size : " + queryDocumentSnapshots.getDocuments().size());
                    int likePostCount = queryDocumentSnapshots.size();
                    String likePostCountStr = likePostCount+"";

                    if(likePostCount>10000){
                     likePostCountStr = (likePostCount/1000) + " K";
                    }if(likePostCount>1000){
                        likePostCountStr= likePostCount/1000+"";
                        if(likePostCount%1000>100){
                            likePostCountStr+="."+(likePostCount%1000)/100;
                        }
                         likePostCountStr+=" K";
                     }
                    myLikePostCountTextView.setText(likePostCountStr);
                }else{
                    myLikePostCountTextView.setText("0");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                myLikePostCountTextView.setText("0");
            }
        });
    }



}

