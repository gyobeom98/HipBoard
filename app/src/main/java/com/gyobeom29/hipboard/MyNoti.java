package com.gyobeom29.hipboard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.gyobeom29.hipboard.activity.DetailPostActivity;

public class MyNoti {

    private static NotificationManager notiManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showNoti(final String documentId, final Context context){
        notiManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                NotificationCompat.Builder builder = null;
                notiManager.createNotificationChannel(new NotificationChannel("channelId","channelName",notiManager.IMPORTANCE_DEFAULT));
                Intent intent = new Intent(context, DetailPostActivity.class);
                builder = new NotificationCompat.Builder(context,"channelId");
                builder.setContentTitle("회원님의 게시글에 댓글이 달렸어요!");
                builder.setContentText("자세히 보시려면 알림을 눌러주세요");
                builder.setSmallIcon(android.R.drawable.ic_menu_upload);
                builder.setAutoCancel(true);
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
                if(!documentId.equals("")){
                    intent.putExtra("documentId",documentId);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context,501,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(pendingIntent);
                }
                Notification notification = builder.build();
                notiManager.notify(1,notification);
            }
        }).start();


    }

}
