package com.gyobeom29.hipboard.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Random;


public class BasicActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String basicAcprofilePath;
    String basicAcUserName;
    private final static String TAG = "BasicActivity";
    Button testBtn;
    private Class thiClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
//        getWindow().setExitTransition(new Explode());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(basicAcUserName==null){
            getUserInfos();
        }

    }

    public void showHome(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    public void setNavigation(Class c){
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        if(navigationView != null) {
            navigationView.setBackgroundResource(R.color.colorToolbar);
            navigationView.setNavigationItemSelectedListener(this);
            thiClass = c;
            writeLog("thiClass Name : " + thiClass.getName());
            if (thiClass.getName().equals(MainActivity.class.getName())) {
                navigationView.setCheckedItem(R.id.nav_main_ac_item);
            } else if (thiClass.getName().equals(UserInfoActivity.class.getName())) {
                if (navigationView.getCheckedItem() != null)
                    navigationView.getCheckedItem().setChecked(false);
            }else if(thiClass.getName().equals(PostListActivity.class.getName())){
                navigationView.setCheckedItem(R.id.nav_post_list_item);
            }else if(thiClass.getName().equals(NewsListActivity.class.getName())){
                navigationView.setCheckedItem(R.id.nav_news_list_item);
            }
        }
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
        if(navUserNameTextView != null)
        navUserNameTextView.setText(basicAcUserName);
        if(navProfileImageView!=null){
            int imgrandomInt = ((int)Math.random()+1)*10;
            Glide.with(getApplicationContext()).load(basicAcprofilePath+"?v="+imgrandomInt).centerCrop().override(0).into(navProfileImageView);
        }
        Button logOutBtn = findViewById(R.id.nav_logOut_Btn);
        Button myPageBtn = findViewById(R.id.nav_myPage_Btn);

        if(myPageBtn!=null)
        myPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeLog("className : " + getApplicationContext().getClass().getName());
                Intent intent = new Intent(getApplicationContext(),UserInfoActivity.class);
                startActivity(intent);

            }
        });
        if(logOutBtn!=null)
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home : finish(); break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_main_ac_item :
                if(!thiClass.getName().equals(MainActivity.class.getName())){
                   startAc(MainActivity.class);
                }
            break;
            case R.id.nav_post_list_item :
                if(!thiClass.getName().equals(PostListActivity.class.getName()))
                    startAc(PostListActivity.class);
                break;

            case R.id.nav_news_list_item :
                if(!thiClass.getName().equals(NewsListActivity.class.getName()))
                startAc(NewsListActivity.class);
                break;
        }

        return true;
    }

    private void startAc(Class c){
        Intent intent = new Intent(getApplicationContext(),c);
        startActivity(intent);
    }

    public void hideKeyBoard(EditText[] editTexts){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        for (EditText ed:editTexts) {
            if(ed.isFocused()){
                imm.hideSoftInputFromWindow(ed.getWindowToken(), 0);
            }
        }
    }

}
