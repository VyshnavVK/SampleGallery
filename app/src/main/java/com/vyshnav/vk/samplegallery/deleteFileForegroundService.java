package com.vyshnav.vk.samplegallery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

public class deleteFileForegroundService extends Service {
IBinder binder = new LocalBinder();
    JSONArray jsonArray;
int progress = 0;
int itemCount = 0;
    public boolean startDelete(){

        for(int i = 0;i < jsonArray.length();i++){
            Log.d("TAG", "startDelete: "+jsonArray.toString());
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                File file = new File(jsonObject.getString("path"));
                if(file.delete()) {
                    progress = i;
                }
                if(jsonArray.length()==i+1){
                    stopForeground(true);
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }


    @Override
    public void onCreate() {
        super.onCreate();



    }

    public boolean startForeground() {
        String channelId ="deleteImageChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId);
        } else {

        }

        NotificationCompat.Builder notificationBuilder =new NotificationCompat.Builder(this, channelId );
        Notification notification = notificationBuilder.setOngoing(true)
                .setProgress(itemCount, progress,false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Deleting images...")
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(101, notification);

       if(startDelete()){
            return true;
        }
       return false;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId){
        NotificationChannel chan =new NotificationChannel(channelId,
                "DeleteImageNotification", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.GREEN);
        NotificationManager service = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    public class LocalBinder extends Binder {
        deleteFileForegroundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return deleteFileForegroundService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        try {
            jsonArray = new JSONArray(intent.getStringExtra("paths"));


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(jsonArray!=null) {
            itemCount = jsonArray.length();
            startForeground();
        }
        return  binder;
    }


}
