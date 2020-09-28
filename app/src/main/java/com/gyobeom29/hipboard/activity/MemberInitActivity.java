package com.gyobeom29.hipboard.activity;


import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.gyobeom29.hipboard.MemberInfo;
import com.gyobeom29.hipboard.R;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

public class MemberInitActivity extends NoActiveBasicActivity {

    private static final int REQUEST_CODE_MEMBER_INIT_ACTIVITY = 0;

    private static final String TAG = "MemberInitActivity";

    private static final int CAMERA_PERMISSION_REQUST_CODE = 105;
    private static final int GALLERY_PERMISSION_REQUST_CODE = 53;

    FirebaseAuth mAuth;

    private ImageView profileImageView;

    private RelativeLayout loaderLayout;

    private  String profilePath;

    private FirebaseUser user;

    private TextView loadingTextView;

    CardView cardView;

    private Intent getMemberInfoIntent;
    private MemberInfo myMemberInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);

        setActionBarTitle("회원 정보");

        mAuth = FirebaseAuth.getInstance();

        profileImageView = findViewById(R.id.profileImageView);
        loaderLayout = findViewById(R.id.loaderLayout);
        profileImageView.setOnClickListener(onClickListener);

        findViewById(R.id.galleryBtn).setOnClickListener(onClickListener);
        findViewById(R.id.pictureBtn).setOnClickListener(onClickListener);
        cardView = findViewById(R.id.btnsCardView);

        addToolBarView();

        getMemberInfoIntent = getIntent();

        if(getMemberInfoIntent.getParcelableExtra("memberInfo")!=null){
            myMemberInfo = (MemberInfo)getMemberInfoIntent.getParcelableExtra("memberInfo");
            ((EditText) findViewById(R.id.member_nameEditText)).setText(myMemberInfo.getName());
            ((EditText) findViewById(R.id.member_nameEditText)).setEnabled(false);
            ((EditText) findViewById(R.id.member_phoneEditText)).setText(myMemberInfo.getPhone());
            ((EditText) findViewById(R.id.member_birthEditText)).setText(myMemberInfo.getBirth());
            ((EditText) findViewById(R.id.member_addressEditText)).setText(myMemberInfo.getAddress());
            int randLoad = (int)Math.random()+1*10;
            Glide.with(this).load(myMemberInfo.getPhotoUrl()+"?v="+randLoad).centerCrop().override(500).into(profileImageView);
            showHome();
            loadingTextView = findViewById(R.id.LoadingSTextView);
            loadingTextView.append("\n프로필 이미지 변경시 적용 까지 시간이 걸립니다.");
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    View.OnClickListener toolbarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            storageUploader(1);
        }
    };


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.profileImageView :
                    if (cardView.getVisibility() == View.VISIBLE){
                        cardView.setVisibility(View.GONE);
                    }else{
                        cardView.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.galleryBtn :
                    if (ContextCompat.checkSelfPermission(
                            getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {

                        writeLog("일로 넘어옴");
                        // You can use the API that requires the permission.
                        if(ActivityCompat.shouldShowRequestPermissionRationale(MemberInitActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                            ActivityCompat.requestPermissions(MemberInitActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},GALLERY_PERMISSION_REQUST_CODE);
                            writeLog("여기 넘어 옴 code : " + GALLERY_PERMISSION_REQUST_CODE);
                        }else{
                            ActivityCompat.requestPermissions(MemberInitActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},GALLERY_PERMISSION_REQUST_CODE);
                            writeLog("여기 누름 2");
                        }


                    }else {
                        writeLog("여기로 넘어옴");
                        cardView.setVisibility(View.GONE);
                        Intent intent = new Intent(getApplicationContext(),GalleryActivity.class);
                        intent.putExtra("media","image");
                        startActivityForResult(intent,REQUEST_CODE_MEMBER_INIT_ACTIVITY);
                    }
                    break;
                case R.id.pictureBtn :
                    if (ContextCompat.checkSelfPermission(
                            getApplicationContext(), Manifest.permission.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED) {
                        // You can use the API that requires the permission.
                        if(ActivityCompat.shouldShowRequestPermissionRationale(MemberInitActivity.this,Manifest.permission.CAMERA)){
                            ActivityCompat.requestPermissions(MemberInitActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_REQUST_CODE);
                            writeLog("여기 누름 1");
                        }else{
                            ActivityCompat.requestPermissions(MemberInitActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_REQUST_CODE);
                            writeLog("여기 누름 2");
                        }
                    }else {
                        startActitivyNoFinish(CameraActivity.class);
                        cardView.setVisibility(View.GONE);
                    }

                    break;
            }
        }
    };


    private void storageUploader(final int where) {

        final String name = ((EditText) findViewById(R.id.member_nameEditText)).getText().toString();
        final String phone = ((EditText) findViewById(R.id.member_phoneEditText)).getText().toString();
        final String birth = ((EditText) findViewById(R.id.member_birthEditText)).getText().toString();
        final String address = ((EditText) findViewById(R.id.member_addressEditText)).getText().toString();
        Log.i("MemberInit","profilePath : " + profilePath);
        if (name.length() > 0 && phone.length()>0 && birth.length()>0 && address.length()>0 &&((profilePath!=null && profilePath.length()>0)|| getMemberInfoIntent.getParcelableExtra("memberInfo")!=null)) {
            loaderLayout.setVisibility(View.VISIBLE);
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseStorage storage = FirebaseStorage.getInstance();

            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();

            // Create a reference to 'images/mountains.jpg'
            final StorageReference mountainImagesRef = storageRef.child("users/"+user.getUid()+"/profileImages.jpg");
            if(profilePath==null || profilePath.length()<=0){
                if(getMemberInfoIntent.getParcelableExtra("memberInfo") != null){
                    MemberInfo memberInfo = new MemberInfo(name,phone,birth,address);
                    memberInfo.setPhotoUrl(myMemberInfo.getPhotoUrl());
                    storeUploader(memberInfo);
                }
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
                            loaderLayout.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                Log.i(TAG, "이미지 업로드 성공 URI:" + downloadUri);
                                MemberInfo memberInfo = new MemberInfo(name, phone, birth, address, downloadUri.toString());

                                if(where == 1)
                                 storeUploader(memberInfo);
//                                else
//                                 databaseUploader(memberInfo);

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

    private void storeUploader(MemberInfo memberInfo){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reference = db.collection("users").document(user.getUid());
        reference.set(memberInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startingToast("회원 정보 등록 성공");
                if(getMemberInfoIntent.getParcelableExtra("memberInfo")!=null){
                    finish();
                }else{
                    startActivity(MainActivity.class);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                startingToast("회원 정보 등록 실패");
            }
        });

    }

//    private void databaseUploader(MemberInfo memberInfo){
//        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
//        mDatabase.child("users").child(user.getUid()).setValue(memberInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                startingToast("회원 정보 등록 성공(dataBase)");
//                startActivity(MainActivity.class);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                startingToast("회원 정보 등록 실패");
//            }
//        });
//    }


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
        writeLog("activity Result 받음");
        writeLog("activity Result 받음 , requestCode : " + requestCode);
        switch (requestCode){
            case REQUEST_CODE_MEMBER_INIT_ACTIVITY : {
                writeLog("일단 여기 까지 옴");
                if(resultCode == Activity.RESULT_OK){
                    profilePath = data.getStringExtra("profilePath");
                    writeLog("profilePath : " + profilePath);
                    Glide.with(this).load(profilePath).centerCrop().override(500).into(profileImageView);
                }

            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        writeLog("이 액티비티 사라짐");
    }

    private void writeLog(String msg){
        Log.i(TAG,msg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_PERMISSION_REQUST_CODE : if(grantResults.length>0){
                startActitivyNoFinish(CameraActivity.class);
                cardView.setVisibility(View.GONE);
            }else{
                startingToast("어플 사용중 일부 서비스가 제한 됩니디.\n(어플 정보->권한 에서 설정 변경이 가능합니다.)");
            }
            break;

            case GALLERY_PERMISSION_REQUST_CODE :
                if(ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED){
                    startActitivyNoFinish(GalleryActivity.class);
                    cardView.setVisibility(View.GONE);
                }else{
                    startingToast("어플 사용중 일부 서비스가 제한 됩니디.\n(어플 정보->권한 에서 설정 변경이 가능합니다.)");
                }
                break;
        }
    }

    private void addToolBarView(){

        ImageView toolbarCheckImageView = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams toolbarLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toolbarLayoutParams.gravity = Gravity.RIGHT;
        toolbarLayoutParams.rightMargin = 25;
        toolbarCheckImageView.setLayoutParams(toolbarLayoutParams);
        toolbarCheckImageView.setImageResource(R.drawable.ic_baseline_check_circle_24);
        LinearLayout toolbarLayout = findViewById(R.id.toolbar_menu_lay);
        toolbarCheckImageView.setOnClickListener(toolbarOnClickListener);
        toolbarLayout.addView(toolbarCheckImageView);
    }
}