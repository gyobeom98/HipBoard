package com.gyobeom29.hipboard.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gyobeom29.hipboard.FireBaseUser;
import com.gyobeom29.hipboard.MemberInfo;
import com.gyobeom29.hipboard.R;


public class BasicActivity extends AppCompatActivity {

    String basicAcprofilePath;
    String basicAcUserName;
    private final static String TAG = "BasicActivity";
    Button testBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(basicAcUserName==null){
            getUserInfos();
        }



    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
    }

    public void setActionBarTitle(String title){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(title);
        }
    }

    public void setActionBarTitle(@StringRes int stringRes){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(stringRes);
        }
    }

    @SuppressLint("ResourceAsColor")
    public void setNavigation(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setBackgroundResource(R.color.colorToolbar);

    }


    public void getUserInfos(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        basicAcUserName = documentSnapshot.getData().get("name").toString();
                        basicAcprofilePath = documentSnapshot.getData().get("photoUrl").toString();
                        setNavigationUserInfo();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    writeLog("user 가져오기 실패");
                }
            });
        }
    }

    public void setNavigationUserInfo(){
        ImageView navProfileImageView = findViewById(R.id.nav_user_profileImageView);
        TextView navUserNameTextView = findViewById(R.id.nav_userNameTextView);
        navUserNameTextView.setText(basicAcUserName);
        Glide.with(getApplicationContext()).load(basicAcprofilePath).centerCrop().override(0).into(navProfileImageView);
        Button logOutBtn = findViewById(R.id.nav_logOut_Btn);

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FireBaseUser.signOut();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }


    private void writeLog(String msg){
        Log.i(TAG,msg);
    }

    private void getButton(){
        testBtn = findViewById(R.id.nav_logOut_Btn);
    }


}
