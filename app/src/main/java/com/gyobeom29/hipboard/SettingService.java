package com.gyobeom29.hipboard;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SettingService {

    private static final String TAG = "SettingService";

    public static Setting setting;

    public static void getSetting(final FirebaseUser user, final Context context){
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("setting").document("set").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.getData()!=null){
                    writeLog("setting 존재");
                    setting = new Setting(documentSnapshot.getBoolean("pushOn"));
                    if(setting.isPushOn()){
                        FirebasePushMessage.getMessage(context);
                    }
                }else{
                    writeLog("가져오기 성공  + setting 존재 X ");
                    writeSetting(user.getUid(),null ,context);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                writeLog("가져오기 실패  + setting 존재 X ");
            }
        });
    }

    private static void writeSetting(String userUid, @Nullable Setting mySetting,final Context context){
        if(mySetting == null){
            setting = new Setting();
            setting.setPushOn(true);
        }else{
            setting = mySetting;
        }

        FirebaseFirestore.getInstance().collection("users").document(userUid).collection("setting").document("set").set(setting).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    writeLog("셋팅 성공");
                    FirebasePushMessage.getMessage(context);
                }else{
                    writeLog("셋팅 실패");
                }

            }
        });
    }

    public static void UpdateSetting(FirebaseUser user, boolean isChecked){

        Map<String,Object> updateData = new HashMap<>();
        updateData.put("pushOn",isChecked);
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("setting").document("set").update(updateData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    writeLog("setting 변경 성공");
                }else{
                    writeLog("setting 변경 실패");
                }
            }
        });

    }



    private static void writeLog(String msg){
        Log.i(TAG,msg);
    }


}
