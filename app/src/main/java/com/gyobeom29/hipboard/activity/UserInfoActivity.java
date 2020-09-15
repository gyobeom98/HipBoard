package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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
import com.gyobeom29.hipboard.MemberInfo;
import com.gyobeom29.hipboard.R;

public class UserInfoActivity extends BasicActivity {

    private static final String TAG ="UserInfoActivity";

    FirebaseUser user;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    MemberInfo memberInfo;
    TextView userNameTextView, userPhoneTextView, userBirthTextView, userAddressTextView;
    ImageView userProfileImageView;


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
        userNameTextView = findViewById(R.id.myPageUserName);
        userPhoneTextView = findViewById(R.id.myPageUserPhone);
        userBirthTextView = findViewById(R.id.myPageUserBirth);
        userAddressTextView = findViewById(R.id.myPageUserAddress);
        userProfileImageView = findViewById(R.id.myPageProfileImageView);
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
                    Glide.with(getApplicationContext()).load(imagePath).centerCrop().override(500).into(userProfileImageView);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

}
