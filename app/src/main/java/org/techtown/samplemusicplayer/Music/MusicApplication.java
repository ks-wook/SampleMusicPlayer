package org.techtown.samplemusicplayer.Music;

import android.app.Application;

import org.techtown.samplemusicplayer.MusicServiceInterface;

public class MusicApplication extends Application {
    private static MusicApplication mInstance;
    private static MusicServiceInterface mInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mInterface = new MusicServiceInterface(getApplicationContext());
    }

    public static MusicApplication getInstance() {
        return mInstance;
    }

    public static MusicServiceInterface getServiceInterface() {
        return mInterface;
    }
}
