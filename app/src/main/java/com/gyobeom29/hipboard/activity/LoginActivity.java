package com.gyobeom29.hipboard.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.gyobeom29.hipboard.FireBaseUser;
import com.gyobeom29.hipboard.R;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends NoActiveBasicActivity {

    private static final String TAG = "LoginActivity";

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        showHome();

        mAuth = FirebaseAuth.getInstance();

        Log.i("LogInAc","auth Uid : " + mAuth.getUid());

        findViewById(R.id.loginBtn).setOnClickListener(onClickListener);
        findViewById(R.id.go_to_passwordResetBtn).setOnClickListener(onClickListener);



        setActionBarTitle("로그인");

    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.loginBtn :
                    hideKeyBoard();
                    login();
                    break;
                case R.id.go_to_passwordResetBtn :
                    hideKeyBoard();
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
//                                            startingSnackBar("메일로 인증 링크가 갔습니다. 인증 링크를 클릭 후 다시 한번 로그인 버튼을 눌러주세요");
//                                        }
//                                    });
//                                    builder.setNegativeButton("취소",null);
//                                    AlertDialog alertDialog = builder.create();
//                                    alertDialog.show();
//                                    user.sendEmailVerification();
//                                }else {
//                                    Log.i(TAG, user.isEmailVerified() + "");
                                    startingSnackBar("로그인 성공");
                                    Log.i(TAG,"getToken : "  + FirebaseInstanceId.getInstance().getToken());
                                    FireBaseUser.signIn();
                                    goMainOrMemberInit(FirebaseAuth.getInstance().getCurrentUser());

//                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                if(task.getException()!=null) {
                                    startingSnackBar(task.getException().toString());
                                }

                                // ...
                            }

                            // ...
                        }
                    });

        } else {

            if(email.length()<=0){
                startingSnackBar("이메일을 입력하지 않았습니다. \n 이메일을 입력 해주세요");
                ((EditText)findViewById(R.id.emailEditText)).requestFocus();
            }else if(password.length()<=0){
                startingSnackBar("비밀번호를 입력하지 않았습니다. \n 비밀번호를 입력 해주세요");
                ((EditText)findViewById(R.id.passwordEditText)).requestFocus();
            }
        }
    }


    private void startingSnackBar(String msg){
        Snackbar.make((Button)findViewById(R.id.loginBtn),msg, BaseTransientBottomBar.LENGTH_SHORT).show();
    }

    private void startActivity(Class c){
        Intent intent = new Intent(LoginActivity.this,c);
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


    private void hideKeyBoard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(((EditText) findViewById(R.id.emailEditText)).isFocused()){
            imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.emailEditText)).getWindowToken(), 0);
        }else if(((EditText) findViewById(R.id.passwordEditText)).isFocused()){
            imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.passwordEditText)).getWindowToken(), 0);
        }
    }

    private void goMainOrMemberInit(FirebaseUser user){
        Log.i("LoginActivity","여기 옴");
        Log.i("LoginActivity","user : " + user);
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.getData()!=null) {
                    if (documentSnapshot.getData().get("name") == null) {
                        startActivity(MemberInitActivity.class);
                        SignUpActivity.instance.finish();
                        finish();
                    } else {
                        goMain();
                        SignUpActivity.instance.finish();
                        finish();
                    }
                }else{
                    startActivity(MemberInitActivity.class);
                    finish();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("LoginActivity","로그인성공 후 goMainOrOther 실패");
            }
        });

    }


}