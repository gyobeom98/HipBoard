package com.gyobeom29.hipboard.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.gyobeom29.hipboard.MemberInfo;
import com.gyobeom29.hipboard.R;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MemberInitActivity extends BasicActivity {

    private static final int REQUEST_CODE_MEMBER_INIT_ACTIVITY = 0;

    private static final String TAG = "MemberInitActivity";

    FirebaseAuth mAuth;

    private ImageView profileImageView;

    private  String profilePath;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);
        startingToast("memeber init create()");

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.addInfoBtn).setOnClickListener(onClickListener);

        profileImageView = findViewById(R.id.profileImageView);

        profileImageView.setOnClickListener(onClickListener);

        findViewById(R.id.galleryBtn).setOnClickListener(onClickListener);
        findViewById(R.id.pictureBtn).setOnClickListener(onClickListener);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.addInfoBtn :
                    profileUpdate();
                    break;

                case R.id.profileImageView :
                    CardView cardView = findViewById(R.id.btnsCardView);
                    if (cardView.getVisibility() == View.VISIBLE){
                        cardView.setVisibility(View.GONE);
                    }else{
                        cardView.setVisibility(View.VISIBLE);
                    }
//                    startActitivyNoFinish(CameraActivity.class);
                    break;
                case R.id.galleryBtn :
                    if (ContextCompat.checkSelfPermission(
                            getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        // You can use the API that requires the permission.
                        if(ActivityCompat.shouldShowRequestPermissionRationale(MemberInitActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                            ActivityCompat.requestPermissions(MemberInitActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                        }else{
                            ActivityCompat.requestPermissions(MemberInitActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                            startingToast("권한을 허용 해주세요");
                        }
                    }else {
//                        startActitivyNoFinish(GalleryActivity.class);
                    }
                    break;
                case R.id.pictureBtn :
//                    startActitivyNoFinish(CameraActivity.class);
                    break;
            }
        }
    };


    public void onRequestPermissionsResults(int requestCode, String[] permissions,int[] grantResults) {
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    startActitivyNoFinish(GalleryActivity.class);
                }  else {
                    startingToast("권한을 허용 해주세요");
                }

        }
    }



    private void profileUpdate() {

        final String name = ((EditText) findViewById(R.id.member_nameEditText)).getText().toString();
        final String phone = ((EditText) findViewById(R.id.member_phoneEditText)).getText().toString();
        final String birth = ((EditText) findViewById(R.id.member_birthEditText)).getText().toString();
        final String address = ((EditText) findViewById(R.id.member_addressEditText)).getText().toString();

        if (name.length() > 0 && phone.length()>0 && birth.length()>0 && address.length()>0) {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseStorage storage = FirebaseStorage.getInstance();

            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();

            // Create a reference to 'images/mountains.jpg'
            final StorageReference mountainImagesRef = storageRef.child("users/"+user.getUid()+"/profileImages.jpg");
            if(profilePath==null){
                MemberInfo memberInfo = new MemberInfo(name, phone, birth, address);
                uploader(memberInfo);
            }else {
                try {
                    InputStream stream = new FileInputStream(new File(profilePath));

                    UploadTask uploadTask = mountainImagesRef.putStream(stream);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                Log.e(TAG, "이미지 업로드 실패1");
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return mountainImagesRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                Log.i(TAG, "이미지 업로드 성공 URI:" + downloadUri);
                                MemberInfo memberInfo = new MemberInfo(name, phone, birth, address, downloadUri.toString());
                                uploader(memberInfo);

                            } else {
                                // Handle failures
                                // ...
                                startingToast("회원 정보를 서버에 올리는데 실패 했습니다.");
                            }
                        }
                    });

                } catch (IOException e) {
                    Log.e("로그", "에러 : " + e.toString());
                }
            }

        } else {
            startingToast("회원 정보를 입력 해주세요");

        }
    }

    private void uploader(MemberInfo memberInfo){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reference = db.collection("users").document(user.getUid());
        reference.set(memberInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startingToast("회원 정보 등록 성공");
                startActivity(MainActivity.class);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                startingToast("회원 정보 등록 실패");
            }
        });

    }

    public void startingToast(String msg){
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
    }

    private void startActivity(Class c){
        Intent intent = new Intent(MemberInitActivity.this,c);
        startActivity(intent);
        finish();
    }

    private void startActitivyNoFinish(Class c){
        Intent intent = new Intent(MemberInitActivity.this,c);
        intent.putExtra("media","image");
        startActivityForResult(intent,REQUEST_CODE_MEMBER_INIT_ACTIVITY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CODE_MEMBER_INIT_ACTIVITY : {

                if(resultCode == Activity.RESULT_OK){

                    profilePath = data.getStringExtra("profilePath");
                    Log.e("로그 : ","profilePath : " + profilePath);
                    Glide.with(this).load(profilePath).centerCrop().override(500).into(profileImageView);
                }

            }
        }

    }

}