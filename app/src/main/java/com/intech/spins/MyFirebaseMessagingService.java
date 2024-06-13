package com.intech.spins;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "张飞";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle FCM messages here.
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification());
            sendNotification(remoteMessage.getNotification());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "onNewToken: " + token);
    }

    private void sendNotification(RemoteMessage.Notification message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(message.getTitle())
                .setContentText(message.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // 设置图标
        if (message.getIcon() != null && !message.getIcon().isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                IconCompat iconCompat = IconCompat.createWithContentUri(message.getIcon());
                notificationBuilder.setSmallIcon(iconCompat);
            } else {
                // 在较旧的设备上不能直接使用Icon对象，可以选择其他替代方案
                notificationBuilder.setSmallIcon(R.mipmap.ic_launcher); // 或者使用默认图标
            }
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        int notificationId = 0;
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
