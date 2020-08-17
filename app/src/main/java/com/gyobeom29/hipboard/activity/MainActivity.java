package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.gyobeom29.hipboard.R;

public class MainActivity extends BasicActivity {

    private static final String TAG = "MainActivity";

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            findViewById(R.id.logoutButton).setVisibility(View.GONE);
            findViewById(R.id.member_in_it_btn).setVisibility(View.GONE);
            startActiNoFinish(SignUpActivity.class);
        }else{
            findViewById(R.id.logoutButton).setVisibility(View.VISIBLE);
            findViewById(R.id.member_in_it_btn).setVisibility(View.VISIBLE);
            Log.e("UId" , user.getUid());
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(user.getUid());
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
        }

        findViewById(R.id.logoutButton).setOnClickListener(onClickListener);
        findViewById(R.id.member_in_it_btn).setOnClickListener(onClickListener);
        findViewById(R.id.mainFloatBtn).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.logoutButton : mAuth.signOut(); startActiNoFinish(SignUpActivity.class); break;
                case R.id.member_in_it_btn : startActiNoFinish(MemberInitActivity.class); break;
                case R.id.mainFloatBtn : startActiNoFinish(WritePostActivity.class); break;
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

}