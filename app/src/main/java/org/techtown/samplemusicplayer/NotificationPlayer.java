package org.techtown.samplemusicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import org.techtown.samplemusicplayer.fragments.Player_MusicData;
import org.techtown.samplemusicplayer.items.CommandActions;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;

import org.techtown.samplemusicplayer.Main.MainActivity;

import javax.net.ssl.SNIHostName;

public class NotificationPlayer {
    private final static int NOTIFICATION_PLAYER_ID = 0x342;
    private MusicService mService;
    private NotificationManager mNotificationManager;
    private NotificationManagerBuilder mNotificationManagerBuilder;
    static private boolean isForeground = false;

    private NotificationChannel mChannel;
    public String ChannelId = "channel";
    public String ChannelName = "channel name";
    int importance = NotificationManager.IMPORTANCE_LOW;

    static public boolean IsNotificationRunning = false;



    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationPlayer(MusicService service) {
        mService = service;
        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel();
        mNotificationManager.createNotificationChannel(mChannel);
    }

    public void createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(
                    ChannelId, ChannelName, importance);
        }
    }

    public void updateNotificationPlayer() {
        cancel();
        mNotificationManagerBuilder = new NotificationManagerBuilder();
        mNotificationManagerBuilder.execute();
    }

    public void removeNotificationPlayer() {
        cancel();

        Player_MusicData.InterruptThread();
        mService.musicPlayer.pause();
        mService.sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));

        isForeground = false;
        IsNotificationRunning = false;
        mService.stopForeground(true);

        if(!mService.getIsRunning()){
            try{
                Thread.sleep(1000);
            } catch (Exception e){
                e.printStackTrace();
            }
            println("강제종료합니다.");
            mService.stopSelf();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    private void cancel() {
        if (mNotificationManagerBuilder != null) {
            mNotificationManagerBuilder.cancel(false);
            mNotificationManagerBuilder = null;
        }
    }

    //포어그라운드 서비스를 위한 클래스
    private class NotificationManagerBuilder extends AsyncTask<Void, Void, Notification> {
        private RemoteViews mRemoteViews;
        private NotificationCompat.Builder mNotificationBuilder;
        private PendingIntent mMainPendingIntent;



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Intent mainActivity = new Intent(mService, MainActivity.class);
            mainActivity.setAction(Intent.ACTION_MAIN);
            mainActivity.addCategory(Intent.CATEGORY_LAUNCHER);
            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mMainPendingIntent = PendingIntent.getActivity(mService, 0, mainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
            mRemoteViews = createRemoteView(R.layout.player_notification);
            mNotificationBuilder = new NotificationCompat.Builder(mService, ChannelId);
            mNotificationBuilder.setSmallIcon(R.drawable.ic_launcher)
                    .setOngoing(false)
                    .setContentIntent(mMainPendingIntent)
                    .setContent(mRemoteViews);

            Notification notification = mNotificationBuilder.build();
            notification.priority = Notification.PRIORITY_MAX;
            notification.contentIntent = mMainPendingIntent;
            //notification.flags = Notification.FLAG_AUTO_CANCEL;
            if (!isForeground) {
                isForeground = true;
                // 서비스를 Foreground 상태로 만든다
                mService.startForeground(NOTIFICATION_PLAYER_ID, notification);
                IsNotificationRunning = true;
            }
        }

        @Override
        protected Notification doInBackground(Void... params) {
            mNotificationBuilder.setContent(mRemoteViews);
            mNotificationBuilder.setContentIntent(mMainPendingIntent);
            mNotificationBuilder.setPriority(Notification.PRIORITY_MAX);
            Notification notification = mNotificationBuilder.build();
            updateRemoteView(mRemoteViews, notification);
            return notification;
        }

        @Override
        protected void onPostExecute(Notification notification) {
            super.onPostExecute(notification);
            try {
                mNotificationManager.notify(NOTIFICATION_PLAYER_ID, notification);

            } catch (Exception e) {
                e.printStackTrace();
            }
            println("onPostExecute 호출됨.");
        }

        private RemoteViews createRemoteView(int layoutId) {
            RemoteViews remoteView = new RemoteViews(mService.getPackageName(), layoutId);
            Intent actionTogglePlay = new Intent(CommandActions.TOGGLE_PLAY);
            Intent actionForward = new Intent(CommandActions.FORWARD);
            Intent actionRewind = new Intent(CommandActions.REWIND);
            Intent actionClose = new Intent(CommandActions.CLOSE);
            PendingIntent togglePlay = PendingIntent.getService(mService, 0, actionTogglePlay, 0);
            PendingIntent forward = PendingIntent.getService(mService, 0, actionForward, 0);
            PendingIntent rewind = PendingIntent.getService(mService, 0, actionRewind, 0);
            PendingIntent close = PendingIntent.getService(mService, 0, actionClose, 0);

            remoteView.setOnClickPendingIntent(R.id.btn_play_pause, togglePlay);
            remoteView.setOnClickPendingIntent(R.id.btn_forward, forward);
            remoteView.setOnClickPendingIntent(R.id.btn_rewind, rewind);
            remoteView.setOnClickPendingIntent(R.id.btn_close, close);



            return remoteView;
        }

        private void updateRemoteView(RemoteViews remoteViews, Notification notification) {
            if (mService.isPlaying()) {
                remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.pause2);
            } else {
                remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.play2);
            }

            String title = mService.getAudioItem().getTitle();
            remoteViews.setTextViewText(R.id.txt_title, title);

            NotificationTarget notificationTarget = new NotificationTarget(
                    mService,
                    remoteViews,
                    R.id.img_albumart,
                    notification,
                    NOTIFICATION_PLAYER_ID
            );

            Handler uihandler = new Handler(Looper.getMainLooper());
            uihandler.post(new Runnable() {
                @Override
                public void run() {
                    Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), mService.getAudioItem().getAlbumId());
                    Glide.with(mService).load(albumArtUri).asBitmap().into(notificationTarget);
                }
            });
        }
    }


    public void println(String data){
        Log.d("Notification" , data);
    }
}
