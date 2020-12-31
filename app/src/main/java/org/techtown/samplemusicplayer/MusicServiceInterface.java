package org.techtown.samplemusicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;

import org.techtown.samplemusicplayer.Music.MusicDto;
import org.techtown.samplemusicplayer.fragments.Player_MusicData;

import java.util.ArrayList;

public class MusicServiceInterface {
    private ServiceConnection mServiceConnection;
    private MusicService musicService;

    public MusicServiceInterface(Context context) {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicService = ((MusicService.AudioServiceBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceConnection = null;
                musicService = null;
            }
        };
        context.bindService(new Intent(context, MusicService.class)
                .setPackage(context.getPackageName()), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public MediaPlayer getMusicPlayer() {
        return musicService.musicPlayer;
    }

    public void togglePlay() {
        if (isPlaying()) {
            musicService.pause();
        } else {
            musicService.play();
        }
    }

    public boolean isPlaying() {
        if (musicService != null) {
            return musicService.isPlaying();
        }
        return false;
    }

    public MusicDto getAudioItem() {
        if (musicService != null) {
            return musicService.getAudioItem();
        }
        return null;
    }


    public void setPlayList(ArrayList<Long> audioIds) {
        if (musicService != null) {
            musicService.setPlayList(audioIds);
        }
    }

    public void play(int position) {
        if (musicService != null) {
            musicService.play(position);
        }
    }

    public void play() {
        if (musicService != null) {
            musicService.play();
        }
    }

    public void pause() {
        if (musicService != null) {
            musicService.pause();
        }
    }

    public void forward() {
        if (musicService != null) {
            musicService.forward();
        }
    }

    public void rewind() {
        if (musicService != null) {
            musicService.rewind();
        }
    }

    public int getCurrentPosition(){
        return musicService.getCurrentPosition();
    }

    public void setIsRunning(boolean IsRunning){
        musicService.setIsRunning(IsRunning);
    }


    public void seekTo(int position){
        musicService.seekTo(position);
    }

    public void onDestroy() {
        this.musicService.onDestroy();
    }
}

