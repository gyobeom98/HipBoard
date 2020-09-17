package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.internal.zzp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.gyobeom29.hipboard.MemberInfo;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class UserInfoActivity extends BasicActivity {

    private static final String TAG ="UserInfoActivity";

    FirebaseUser user;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    MemberInfo memberInfo;
    TextView userNameTextView, userPhoneTextView, userBirthTextView, userAddressTextView;
    ImageView userProfileImageView;
    private ArrayList<PostInfo> postInfos = new ArrayList<>();
    LinearLayout innerPostLayout;


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

    private void writeLog(String msg){
        Log.i(TAG,msg);
    }

    private void init(){
        setNavigation();
        userNameTextView = findViewById(R.id.myPageUserName);
        userPhoneTextView = findViewById(R.id.myPageUserPhone);
        userBirthTextView = findViewById(R.id.myPageUserBirth);
        userAddressTextView = findViewById(R.id.myPageUserAddress);
        userProfileImageView = findViewById(R.id.myPageProfileImageView);
        innerPostLayout = findViewById(R.id.innerPostLayout);
        getDocumentId();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setInfo();
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
                    userPhoneTextView.setText(phone);
                    userBirthTextView.setText(birth);
                    userAddressTextView.setText(address);
                    writeLog("imagePath : "  + imagePath);
                    Glide.with(getApplicationContext()).load(imagePath).centerCrop().override(0).into(userProfileImageView);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void getDocumentId(){
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("likePost").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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

        FirebaseFirestore.getInstance().collection("posts").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot docst) {
                if(docst.exists()){
                    if(docst.getData()!=null){
                        String title = docst.getData().get("title").toString();
                        String publusherName = docst.getData().get("publisherName").toString();
                        Date createAt = docst.getDate("createAt");
                        String createAtStr = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.getDefault()).format(createAt);
                        String likeCnt = docst.getData().get("likeCount").toString();
                        String smallContent = ((ArrayList<String>)docst.getData().get("contents")).get(0);

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        LinearLayout innerPostInfoLayout = new LinearLayout(getApplicationContext());
                        innerPostInfoLayout.setLayoutParams(layoutParams);
                        innerPostInfoLayout.setOrientation(LinearLayout.VERTICAL);
                        innerPostInfoLayout.setBackgroundResource(R.color.colorPink);

                        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        TextView postTitleTextView = new TextView(getApplicationContext());
                        postTitleTextView.setLayoutParams(layoutParams);
                        postTitleTextView.setTextSize(30);
                        postTitleTextView.setTypeface(Typeface.DEFAULT_BOLD);
                        postTitleTextView.setText(title);

                        TextView postSmallContentTextView = new TextView(getApplicationContext());
                        postSmallContentTextView.setLayoutParams(layoutParams);
                        postSmallContentTextView.setText(smallContent);

                        TextView postPublisherNameTextView = new TextView(getApplicationContext());
                        postPublisherNameTextView.setLayoutParams(layoutParams);
                        postPublisherNameTextView.setText(publusherName);

                        TextView postCreateAyTextView = new TextView(getApplicationContext());
                        postCreateAyTextView.setLayoutParams(layoutParams);
                        postCreateAyTextView.setText(createAtStr);

                        TextView postLikeCountTextView = new TextView(getApplicationContext());
                        postLikeCountTextView.setLayoutParams(layoutParams);
                        postLikeCountTextView.setText(likeCnt);

                        innerPostInfoLayout.addView(postTitleTextView);
                        innerPostInfoLayout.addView(postSmallContentTextView);
                        innerPostInfoLayout.addView(postPublisherNameTextView);
                        innerPostInfoLayout.addView(postCreateAyTextView);
                        innerPostInfoLayout.addView(postLikeCountTextView);

                        innerPostLayout.addView(innerPostInfoLayout,0);

                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

}
