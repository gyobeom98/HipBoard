package com.gyobeom29.hipboard.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.gyobeom29.hipboard.R;

public class PasswordResetActivity extends NoActiveBasicActivity {

    private static final String TAG = "PasswordResetActivity";

    FirebaseAuth mAuth;

    RelativeLayout loaderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        mAuth = FirebaseAuth.getInstance();
        setActionBarTitle("비밀번호 재설정");

        loaderLayout = findViewById(R.id.loaderLayout);
        showHome();
        addToolBarView();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            send();
        }
    };

    private void send() {
        String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
        if (email.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loaderLayout.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                startingToast("이메일로 메시지 보냄");
                            }else{
                                startingToast("회원가입이 되어 있지 않은 이메일 입니다.");
                            }
                        }
                    });
        } else {
                startingToast("이메일을 입력 해주세요.");
        }
    }

    public void startingToast(String msg){
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
    }

    private void startMainActivity(){
        Intent intent = new Intent(PasswordResetActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }



    private void addToolBarView(){

        ImageView toolbarCheckImageView = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams toolbarLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toolbarLayoutParams.gravity = Gravity.RIGHT;
        toolbarLayoutParams.rightMargin = 25;
        toolbarCheckImageView.setLayoutParams(toolbarLayoutParams);
        toolbarCheckImageView.setImageResource(R.drawable.ic_baseline_check_circle_24);
        LinearLayout toolbarLayout = findViewById(R.id.toolbar_menu_lay);
        toolbarCheckImageView.setOnClickListener(onClickListener);
        toolbarLayout.addView(toolbarCheckImageView);
    }


}