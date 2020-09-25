package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.gyobeom29.hipboard.R;

public class SignUpActivity extends NoActiveBasicActivity {

    private static final String TAG = "SignUpActivity";

    public static Activity instance;

    FirebaseAuth mAuth;

    RelativeLayout loaderLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        instance = this;

        setActionBarTitle("회원가입");

        findViewById(R.id.signUpButton).setOnClickListener(onClickListener);
        findViewById(R.id.go_to_loginBtn).setOnClickListener(onClickListener);
        loaderLayout = findViewById(R.id.loaderLayout);
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.signUpButton :
                    hideKeyBoard();
                    signup();

                    break;
                case R.id.go_to_loginBtn:
                    hideKeyBoard();
                    startNoFinishActivity(LoginActivity.class);
                    break;
            }
        }
    };

    private void signup() {

        String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
        String passwordCheck = ((EditText) findViewById(R.id.passwordCheckEditText)).getText().toString();
        if (email.length() > 0 && password.length() > 0 && passwordCheck.length() > 0) {
            if(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//                if (Pattern.matches("((?=.*[a-z])(?=.*[0-9]).{8,20})",password) && password.equals(passwordCheck)) {
                    loaderLayout.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    loaderLayout.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        startingSnackBar("회원 가입을 성공 하셨습니다.");
                                        Log.d(TAG, "createUserWithEmail:success");
                                        Log.i(TAG,mAuth.getUid());
                                        mAuth.signOut();
                                        ((EditText) findViewById(R.id.passwordCheckEditText)).setText("");
                                        ((EditText) findViewById(R.id.passwordEditText)).setText("");
                                        ((EditText) findViewById(R.id.emailEditText)).setText("");
                                        startNoFinishActivity(LoginActivity.class);

//                            updateUI(user);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        if (task.getException() != null)
                                            startingSnackBar(task.getException().toString());
                                    }

                                    // ...
                                }
                            });
//                } else {
//                    startingSnackBar("비밀번호가 일치 하지 않거나 규칙에 맞지 않는 비밀번호 입니다.");
//                    writeLog(password);
//                    writeLog(passwordCheck);
//
//                }
            }else{
                startingSnackBar("이메일을 다시 확인 하여 주세요");
            }
        } else {

            if(email.length()<=0){
                startingSnackBar("이메일을 입력하지 않았습니다. \n 이메일을 입력 해주세요");
                ((EditText)findViewById(R.id.emailEditText)).requestFocus();
            }else if(password.length()<=0){
                startingSnackBar("비밀번호를 입력하지 않았습니다. \n 비밀번호를 입력 해주세요");
                ((EditText)findViewById(R.id.passwordEditText)).requestFocus();
            }else if(passwordCheck.length()<=0){
                startingSnackBar("비밀번호 확인이 되지 않았습니다.\n 비밀번호를 다시 확인 해주세요");
                ((EditText)findViewById(R.id.passwordCheckEditText)).requestFocus();
            }


        }
    }


    private void startNoFinishActivity(Class c){
        Intent intent = new Intent(SignUpActivity.this, c);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);

    }

    private void writeLog(String msg){
        Log.i(TAG,msg);
    }

    private void startingSnackBar(String msg){
        Snackbar.make((Button)findViewById(R.id.signUpButton),msg, BaseTransientBottomBar.LENGTH_SHORT).show();
    }

    private void hideKeyBoard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(((EditText) findViewById(R.id.passwordCheckEditText)).isFocused()){
            imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.passwordCheckEditText)).getWindowToken(), 0);
        }else if(((EditText) findViewById(R.id.passwordEditText)).isFocused()){
            imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.passwordEditText)).getWindowToken(), 0);
        }else if(((EditText) findViewById(R.id.emailEditText)).isFocused()){
            imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.emailEditText)).getWindowToken(), 0);
        }
    }

}