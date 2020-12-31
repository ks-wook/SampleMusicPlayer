package org.techtown.samplemusicplayer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;


import androidx.annotation.RequiresApi;

import org.techtown.samplemusicplayer.Main.MainActivity;
import org.techtown.samplemusicplayer.Music.MusicDto;
import org.techtown.samplemusicplayer.fragments.Player_MusicData;
import org.techtown.samplemusicplayer.items.CommandActions;

import java.util.ArrayList;

public class MusicService extends Service {

    private ArrayList<Long> MusicIDList = new ArrayList<>();

    private final IBinder mBinder = new AudioServiceBinder();
    public MediaPlayer musicPlayer;
    static boolean isPrepared;

    private int mCurrentPosition;
    public MusicDto musicItem = new MusicDto();

    private NotificationPlayer notificationPlayer;


    public class AudioServiceBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public MusicService() {
    }

    public static boolean IsRunning = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        if(!MainActivity.IsMainRunning){
            stopSelf();
            android.os.Process.killProcess(android.os.Process.myPid());
        }


        IsRunning = true;


        musicPlayer = new MediaPlayer();
        musicPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        notificationPlayer = new NotificationPlayer(this);

        musicPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                isPrepared = true;

                updateNotificationPlayer();
                sendBroadcast(new Intent(BroadcastActions.PREPARED));
            }
        });
        musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPrepared = false;
                forward();

                updateNotificationPlayer();
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
            }
        });
        musicPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isPrepared = false;
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));

                updateNotificationPlayer();
                return false;
            }
        });
        musicPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) { }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    public void setPlayList(ArrayList<Long> audioIds) {
        if (MusicIDList.size() != audioIds.size()) {
            if (!MusicIDList.equals(audioIds)) {
                MusicIDList.clear();
                MusicIDList.addAll(audioIds);
            }
        }
    }

    private void queryAudioItem(int position) {
        mCurrentPosition = position;
        long audioId = MusicIDList.get(position);
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };
        String selection = MediaStore.Audio.Media._ID + " = ?";
        String[] selectionArgs = {String.valueOf(audioId)};
        Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                this.musicItem.setValues(cursor);
            }
            cursor.close();
        }
    }

    private void prepare() {
        musicPlayer.reset();
        IsRunning = true;
        try {
            musicPlayer.setDataSource(musicItem.DataPath);
            musicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            musicPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void play(int position) {

        queryAudioItem(position);
        if(isPrepared) {
            stop();
        }
        prepare();
        play();
    }

    public void play() {
        try{
            Thread.sleep(1000);
        } catch (Exception e){
            e.printStackTrace();
        }

        musicPlayer.start();

        updateNotificationPlayer();
        sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));

    }

    public void pause() {
        if (isPrepared) {
            musicPlayer.pause();

            updateNotificationPlayer();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
        }
    }

    private void stop() {
        musicPlayer.stop();
        musicPlayer.reset();
    }

    public void forward() {
        if (MusicIDList.size() - 1 > mCurrentPosition) {
            mCurrentPosition++; // 다음 포지션으로 이동.
        } else {
            mCurrentPosition = 0; // 처음 포지션으로 이동.
        }

        sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
        play(mCurrentPosition);
    }

    public void rewind() {
        if (mCurrentPosition > 0) {
            mCurrentPosition--; // 이전 포지션으로 이동.
        } else {
            mCurrentPosition = MusicIDList.size() - 1; // 마지막 포지션으로 이동.
        }

        sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
        play(mCurrentPosition);
    }

    public int getCurrentPosition(){
        while(true){
            if(musicPlayer.isPlaying()){
                break;
            }
        }
        return musicPlayer.getCurrentPosition();
    }


    public MusicDto getAudioItem() {
        return musicItem;
    }

    public void seekTo(int position){
        musicPlayer.seekTo(position);
    }

    public boolean isPlaying() {
        return musicPlayer.isPlaying();
    }

    private void updateNotificationPlayer() {
        if(notificationPlayer != null){
            notificationPlayer.updateNotificationPlayer();
        }
    }

    private void removeNotificationPlayer() {
        if(notificationPlayer != null){
            notificationPlayer.removeNotificationPlayer();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (CommandActions.TOGGLE_PLAY.equals(action)) {
                if (isPlaying()) {
                    pause();
                } else {
                    play();
                }
            } else if (CommandActions.REWIND.equals(action)) {
                rewind();
            } else if (CommandActions.FORWARD.equals(action)) {
                forward();
            } else if (CommandActions.CLOSE.equals(action)) {
                removeNotificationPlayer();
            }
        }
        return START_NOT_STICKY;
    }

    public void setIsRunning(boolean IsRunning){
        this.IsRunning = IsRunning;
    }

    public boolean getIsRunning(){
        return IsRunning;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.release();
            musicPlayer = null;
        }
    }
}