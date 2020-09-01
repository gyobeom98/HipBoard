package com.gyobeom29.hipboard.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.gyobeom29.hipboard.R;

public class LoginActivity extends BasicActivity {

    private static final String TAG = "LoginActivity";

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.loginBtn).setOnClickListener(onClickListener);
        findViewById(R.id.go_to_passwordResetBtn).setOnClickListener(onClickListener);

        setActionBarTitle("로그인");

    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.loginBtn :
                    login();
                    break;
                case R.id.go_to_passwordResetBtn :
                    startActivity(PasswordResetActivity.class);
                    break;

            }
        }
    };

    private void login() {
        final String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
        if (email.length() > 0 && password.length() > 0 ) {
            final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
            loaderLayout.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loaderLayout.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                final FirebaseUser user = mAuth.getCurrentUser();
                                Log.i(TAG,user.isEmailVerified()+"");
//                                if(!user.isEmailVerified()){
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//                                    builder.setTitle("이메일 인증 계정").setMessage("이메일 인증 계정이 아닙니다.\n이메일 인증을 하시겠습니까?");
//                                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            emailCertification(email,user);
//                                            startingToast("메일로 인증 링크가 갔습니다. 인증 링크를 클릭 후 다시 한번 로그인 버튼을 눌러주세요");
//                                        }
//                                    });
//                                    builder.setNegativeButton("취소",null);
//                                    AlertDialog alertDialog = builder.create();
//                                    alertDialog.show();
//                                    user.sendEmailVerification();
//                                }else {
//                                    Log.i(TAG, user.isEmailVerified() + "");
                                    startingToast("로그인 성공");
                                    goMain();
//                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                if(task.getException()!=null) {
                                    startingToast(task.getException().toString());
                                }

                                // ...
                            }

                            // ...
                        }
                    });

        } else {
            startingToast("입력하지 않은 값이 있습니다. \n이메일 또는 비밀번호를 입력 해주세요.");
        }
    }

    public void startingToast(String msg){
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
    }

    private void startActivity(Class c){
        Intent intent = new Intent(LoginActivity.this,c);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private void goMain(){
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void emailCertification(String email,FirebaseUser user){
        user.sendEmailVerification();
    }


}