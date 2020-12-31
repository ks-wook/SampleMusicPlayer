package org.techtown.samplemusicplayer.fragments;

import android.content.ContentUris;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.techtown.samplemusicplayer.Main.MainActivity;
import org.techtown.samplemusicplayer.Main.MyAdapter;
import org.techtown.samplemusicplayer.Music.MusicApplication;
import org.techtown.samplemusicplayer.Music.MusicDto;
import org.techtown.samplemusicplayer.R;


public class Player_MusicData extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    ViewGroup rootView;

    LinearLayout player_View;

    TextView Music_title;
    ImageView player_album;

    ImageView player_pre;
    ImageView player_play;
    ImageView player_pause;
    ImageView player_next;

    SeekBar seekBar;
    static Seekbar_Thread seekbar_thread;

    MediaPlayer musicPlayer;

    MusicDto musicItem;
    MyAdapter adapter;

    public Player_MusicData(MyAdapter adapter) {
        this.adapter = adapter;
    }

    public Player_MusicData() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_player__music_data, container, false);


        Music_title = rootView.findViewById(R.id.Music_title);
        player_View = rootView.findViewById(R.id.player_View);
        player_pre = rootView.findViewById(R.id.player_pre);
        player_pre.setOnClickListener(this);
        player_play = rootView.findViewById(R.id.player_play);
        player_play.setOnClickListener(this);
        player_next = rootView.findViewById(R.id.player_next);
        player_next.setOnClickListener(this);
        player_pause = rootView.findViewById(R.id.player_pause);
        player_pause.setOnClickListener(this);

        seekBar = rootView.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);


        return rootView;
    }

    public void setting_player(){
        musicPlayer = MusicApplication.getServiceInterface().getMusicPlayer();

        musicItem = MusicApplication.getServiceInterface().getAudioItem();
        player_album = rootView.findViewById(R.id.player_album);
        Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), musicItem.getAlbumId());
        Glide.with(getActivity()).load(albumArtUri).into(player_album);


        Music_title.setText(musicItem.getTitle());

        int duration = MusicApplication.getServiceInterface().getMusicPlayer().getDuration();
        seekBar.setMax(duration);
        seekBar.setProgress(0);


        if(seekbar_thread == null) {
            seekbar_thread = new Seekbar_Thread();
        }

        else{
            seekbar_thread.interrupt();
            seekbar_thread = new Seekbar_Thread();
        }
        //seekbar_thread.setDaemon(true);


        seekbar_thread.start();
        setButtonImg();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.player_pre:
                MusicApplication.getServiceInterface().rewind();
                break;
            case R.id.player_pause:
                MusicApplication.getServiceInterface().togglePlay();
                player_pause.setVisibility(View.GONE);
                player_play.setVisibility(View.VISIBLE);
                break;
            case R.id.player_play:
                MusicApplication.getServiceInterface().togglePlay();
                player_play.setVisibility(View.GONE);
                player_pause.setVisibility(View.VISIBLE);
                break;
            case R.id.player_next:
                MusicApplication.getServiceInterface().forward();
                break;
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int changed = seekBar.getProgress();
        MusicApplication.getServiceInterface().seekTo(changed);
    }

    class Seekbar_Thread extends Thread {
        @Override
        public void run() {
            try {
                println("스레드 시작");
                seekBar.setProgress(0);
                Thread.sleep(500);

                while (MusicApplication.getServiceInterface().isPlaying()) {

                    Thread.sleep(500);
                    seekBar.setProgress(MusicApplication.getServiceInterface().getCurrentPosition());
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            println("seekbar 스레드 종료");
        }
    }

    public static void InterruptThread() {
        if(seekbar_thread == null){
            return;
        }
        try {
            seekbar_thread.interrupt();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setButtonImg() {
        if(MusicApplication.getServiceInterface().isPlaying()){
            player_play.setVisibility(View.GONE);
            player_pause.setVisibility(View.VISIBLE);
        }
    }

    public void setFragment_Visible(boolean check){
        try {
            Thread.sleep(500);
        } catch (Exception e){
            e.printStackTrace();
        }
        if(check){
            player_View.setVisibility(View.VISIBLE);
        }
        else{
            player_View.setVisibility(View.GONE);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void println(String data){
        Log.d("Player_MusicData", data);
    }

}