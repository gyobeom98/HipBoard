package com.gyobeom29.hipboard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gyobeom29.hipboard.activity.DetailPostActivity;
import com.gyobeom29.hipboard.service.BoardFirebaseService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebasePushMessage {


    private final static String serverKey = "AAAAxyC0KEU:APA91bG9uXi9-kyUwd3X3ZN2aqHE7Ap-EKgG-GYwSYNtwpZyy38KDK3oJ60vLSf7WgY6tqJPJd33Quwht1WzfuXloA9Kd6YCBeZD2s-UvK5QIwrcBbWXcDL8-C7LfHXn5qDdCDqaWE8l";
    private static RequestQueue requestQueue;

    private final static String TAG = "FirebasePushMessage";


    private static void sendFCM(PostInfo postInfo, Context context,String fcmToken) {
        if(requestQueue==null){
            requestQueue = Volley.newRequestQueue(context);
        }
        JSONObject requestData = new JSONObject();
        try{
            // 파이어베이스에 보낼 객체 셋팅
            requestData.put("priority","high");

            // 보낼 데이터 셋팅
            JSONObject dataObj = new JSONObject();
            dataObj.put("documentId",postInfo.getDocumentId());

            requestData.put("data",dataObj);
            JSONArray idArray = new JSONArray();

            idArray.put(0,fcmToken);
            requestData.put("registration_ids",idArray);

        }catch (Exception e){
            e.printStackTrace();
        }
        sendData(requestData, new SendResponseListenner(){

            @Override
            public void onRequestStarted() {
                writeLog("onRequestStarted() 호출");
            }

            @Override
            public void onRequestCompleted() {
                writeLog("onRequestCompleted() 호출");
            }

            @Override
            public void onRequestWithError(VolleyError e) {
                writeLog("onRequestWithError() 호출");
            }
        });
    }

    public interface SendResponseListenner {
        public void onRequestStarted();
        public void onRequestCompleted();
        public void onRequestWithError(VolleyError e);

    }

    private static void sendData(JSONObject requestData, final SendResponseListenner listenner){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", requestData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                listenner.onRequestCompleted();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listenner.onRequestWithError(error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                headers.put("Authorization","key="+serverKey);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        request.setShouldCache(false);
        listenner.onRequestStarted();
        requestQueue.add(request);
    }

    private static void writeLog(String msg){
        Log.i(TAG,msg);
    }

    public static void sendPush(final PostInfo postInfo, final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeLog("publisher : "+postInfo.getPublisher());
                FirebaseFirestore.getInstance().collection("users").document(postInfo.getPublisher()).collection("token").document("userFCMToken").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            if(documentSnapshot.getData().get("user_fcm_token")!=null) {
                                writeLog("유저 토큰 : " + documentSnapshot.getData().get("user_fcm_token").toString());
                                writeLog("유저 토큰 있음");
                                String fcmToken = documentSnapshot.getData().get("user_fcm_token").toString();
                                writeLog(fcmToken);
                                sendFCM(postInfo, context, fcmToken);
                            }else{
                                writeLog("유저 토큰 없음");
                                sendMessage(postInfo.getPublisher(),postInfo.getDocumentId(),context);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }).start();


    }

    private static void sendMessage(String uid,String documentId,Context context){
        Map<String,String> data = new HashMap<>();
        data.put("documentId",documentId);
        FirebaseFirestore.getInstance().collection("users").document(uid).collection("message").document("noReadMessage").set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                writeLog("메시지 보내기 성공");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                writeLog("메시지 보내기 실패");
            }
        });
    }




    public static void getMessage(final Context context){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user !=null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("message").document("noReadMessage").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onSuccess(DocumentSnapshot ds) {
                            if(ds.exists()){
                                if( ds.getData().get("documentId")!=null){
                                    String message = ds.getData().get("documentId").toString();
                                    writeLog(message);
                                    MyNoti.showNoti(message,context);
                                    deleteMessage();
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }).start();

        }
    }

    private static void deleteMessage(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null){
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("message").document("noReadMessage").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        writeLog("삭제 성공");
                    }else{
                        writeLog("삭제 실패");
                    }
                }
            });
        }

    }

}
