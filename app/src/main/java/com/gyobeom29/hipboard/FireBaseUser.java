package com.gyobeom29.hipboard;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FireBaseUser {
    private final static String TAG = "FirebaseUser";
    private static MemberInfo memberInfo;

    public static void signIn(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            String token = FirebaseInstanceId.getInstance().getToken();
            Map<String,String> data = new HashMap<>();
            data.put("user_fcm_token",FirebaseInstanceId.getInstance().getToken());
                FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("token").document("userFCMToken").set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        writeLog("토큰 생성 완료");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        writeLog("토큰 생성 실패");
                        e.printStackTrace();
                    }
                });

        }
    }

    public static void signOut(){

        FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                        String token = FirebaseInstanceId.getInstance().getToken();
                        String Token = FirebaseInstanceId.getInstance().getToken();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            Map<String,String> data = new HashMap<>();
            data.put("user_fcm_token",null);
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("token").document("userFCMToken").set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    writeLog("토큰 변경 success");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    writeLog("토큰 변경 ㄴailed");
                }
            });

            FirebaseAuth.getInstance().signOut();
        }
    }

    private static void writeLog(String msg){
        Log.i(TAG,msg);
    }

    private static void getUserInfo(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        String name = documentSnapshot.getData().get("name").toString();
                        String phone = documentSnapshot.getData().get("phone").toString();
                        String birth = documentSnapshot.getData().get("birth").toString();
                        String address = documentSnapshot.getData().get("address").toString();
                        memberInfo = new MemberInfo(name,phone,birth,address);
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

}
