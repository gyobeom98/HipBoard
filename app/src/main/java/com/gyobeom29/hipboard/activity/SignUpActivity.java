package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.gyobeom29.hipboard.R;

public class SignUpActivity extends BasicActivity {

    private static final String TAG = "SignUpActivity";

    FirebaseAuth mAuth;

    RelativeLayout loaderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.signUpButton).setOnClickListener(onClickListener);
        findViewById(R.id.go_to_loginBtn).setOnClickListener(onClickListener);
        loaderLayout = findViewById(R.id.loaderLayout);
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.signUpButton :
                    signup();
                    break;
                case R.id.go_to_loginBtn:
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
            if (password.equals(passwordCheck)) {
                loaderLayout.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                loaderLayout.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    startingToast("회원가입을 성공 했습니다.");
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    ((EditText) findViewById(R.id.passwordCheckEditText)).setText("");
                                    ((EditText) findViewById(R.id.passwordEditText)).setText("");
                                    ((EditText) findViewById(R.id.emailEditText)).setText("");

//                            updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    if (task.getException() != null)
                                        startingToast(task.getException().toString());
                                }

                                // ...
                            }
                        });
            } else {
                startingToast("비밀번호가 일치 하지 않습니다.");
            }
        } else {
            startingToast("입력하지 않은 값이 있습니다. \n이메일 또는 비밀번호를 입력 해주세요.");
        }
    }

    private void startingToast(String msg){
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
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
}