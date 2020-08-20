package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.internal.zzp;
import com.google.firebase.database.DatabaseReference;
import com.gyobeom29.hipboard.R;

public class UserInfoActivity extends AppCompatActivity {

    private static final String TAG ="UserInfoActivity";

    FirebaseUser user;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    TextView textView;
    private String getUId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        textView = findViewById(R.id.textView2);
        user.getEmail();

    }

    private void writeLog(String msg){
        Log.i(TAG,msg);
    }

}