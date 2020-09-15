package com.gyobeom29.hipboard.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.gyobeom29.hipboard.MyNoti;
import com.gyobeom29.hipboard.activity.DetailPostActivity;
import com.gyobeom29.hipboard.activity.MainActivity;

import java.util.Map;

public class BoardFirebaseService extends FirebaseMessagingService {

    NotificationManager notiManager;

    public BoardFirebaseService(){}

    private static final  String TAG = "MyFirebaseMessaging";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d(TAG,"onNewToken 호출");
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG,"onReceived 호출");
        String from = remoteMessage.getFrom();
        Map<String,String> data = remoteMessage.getData();
        Log.i(TAG,from);
        Log.i(TAG,"data : " + data.toString());
        String documentId = data.get("documentId")+"";
        MyNoti.showNoti(documentId,getApplicationContext());

        sendtoActivity(getApplicationContext(),from);
    }

    private void sendtoActivity(Context context, String from) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("from",from);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        context.startActivity(intent);
    }

}

