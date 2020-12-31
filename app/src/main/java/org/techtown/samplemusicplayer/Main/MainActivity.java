package org.techtown.samplemusicplayer.Main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import org.techtown.samplemusicplayer.BroadcastActions;
import org.techtown.samplemusicplayer.Music.MusicApplication;
import org.techtown.samplemusicplayer.Music.MusicDto;
import org.techtown.samplemusicplayer.MusicService;
import org.techtown.samplemusicplayer.NotificationPlayer;
import org.techtown.samplemusicplayer.fragments.Player_MusicData;
import org.techtown.samplemusicplayer.R;
import org.techtown.samplemusicplayer.fragments.Allmusic_items;
import org.techtown.samplemusicplayer.fragments.Favorite_items;
import org.techtown.samplemusicplayer.fragments.Random_items;
import org.techtown.samplemusicplayer.fragments.Search_items;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static boolean IsMainRunning = false;

    LinearLayout mainLayout;

    static String[] permission_list = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    MyAdapter adapter;
    public static ArrayList<MusicDto> list;

    ImageView playerImage;
    TextView playerTitle;
    ImageButton playerPause_btn;

    Button btn_allMusic;
    Button btn_favorite;
    Button btn_randomMusic;

    Favorite_items favorite_items;
    Allmusic_items allmusic_items;
    Random_items random_items;
    Search_items search_items;

    Player_MusicData player_musicData;


    Toolbar toolbar;
    SearchView searchView;
    TextView toolbar_title;

    private long backKeyPressedTime = 0;
    private Toast toast;

    boolean searchView_isClosed = true;
    
    Handler handler = new Handler();


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IsMainRunning = true;

        checkPermission();

        while(checkCallingOrSelfPermission(permission_list[0]) != PackageManager.PERMISSION_GRANTED){
            if(checkCallingOrSelfPermission(permission_list[0]) == PackageManager.PERMISSION_GRANTED){
                break;
            }
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar_title = findViewById(R.id.toolbar_title);


        getMusicList();
        adapter = new MyAdapter(this, this,list);

        playerImage = (ImageView) findViewById(R.id.img_albumart);
        playerTitle = (TextView) findViewById(R.id.txt_title);
        playerPause_btn = (ImageButton) findViewById(R.id.btn_play_pause);
        findViewById(R.id.lin_miniplayer).setOnClickListener(this);
        findViewById(R.id.btn_rewind).setOnClickListener(this);
        playerPause_btn.setOnClickListener(this);
        findViewById(R.id.btn_forward).setOnClickListener(this);

        registerBroadcast();
        updateUI();

        allmusic_items = new Allmusic_items(adapter, list);
        btn_allMusic = findViewById(R.id.btn_allMusic);

        favorite_items = new Favorite_items(adapter);
        btn_favorite = findViewById(R.id.btn_favoriteMusic);

        random_items = new Random_items(adapter);
        btn_randomMusic = findViewById(R.id.btn_randomMusic);




        //기본 화면은 Allmusic_items
        getSupportFragmentManager().beginTransaction().replace(R.id.container, allmusic_items).commit();

        btn_allMusic.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                //모든 음악을 띄우는 기본 화면
                if(searchView_isClosed == false) {
                    searchView.onActionViewCollapsed();
                    searchView_isClosed = true;
                }
                toolbar_title.setText("모든음악");
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left).replace(R.id.container, allmusic_items).commit();
            }
        });

        btn_favorite.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                //favorite 프래그먼트로 이동, all_music위에 띄우는 방식
                if(searchView_isClosed == false) {
                    searchView.onActionViewCollapsed();
                    searchView_isClosed = true;
                }
                toolbar_title.setText("즐겨찾기");
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left).replace(R.id.container, favorite_items).commit();
            }
        });

        btn_randomMusic.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                // 랜덤 재생목록
                if(searchView_isClosed == false) {
                    searchView.onActionViewCollapsed();
                    searchView_isClosed = true;
                }
                toolbar_title.setText("랜덤재생");
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left).replace(R.id.container, random_items).commit();
            }
        });

        search_items = new Search_items(adapter);
        searchView = findViewById(R.id.searchView);

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.onActionViewCollapsed();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchView_isClosed = false;
                getSupportFragmentManager().beginTransaction().replace(R.id.container, search_items).commit();
                ArrayList<MusicDto> filteredList = adapter.filter(list, newText);
                adapter.setItems(filteredList);
                return false;
            }
        });

        // 미니 플레이어 터치시 화면 전환
        mainLayout = findViewById(R.id.mainLayout);

        //쓰레드 시작을 위해 미리 프래그먼트 추가
        player_musicData = new Player_MusicData(adapter);
        getSupportFragmentManager().beginTransaction().add(R.id.mainContainer, player_musicData, "player_View").commit();
        LinearLayout lin_miniplayer = findViewById(R.id.lin_miniplayer);
        lin_miniplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainLayout.setVisibility(View.GONE);
                ((Player_MusicData)getSupportFragmentManager().findFragmentByTag("player_View")).setFragment_Visible(true);
            }
        });

    }

    //즐겨찾기 목록 새로고침
    public void adapter_refreshFavorite(){
        getSupportFragmentManager().beginTransaction().detach(favorite_items).attach(favorite_items).commit();
    }

    //랜덤재생 목록 새로고침
    public void adapter_refreshRandom(){
        getSupportFragmentManager().beginTransaction().detach(random_items).attach(random_items).commit();
    }

    public void checkPermission(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        int chk = 0;

        for(String permission : permission_list){

            requestPermissions(permission_list,0);
            chk = checkCallingOrSelfPermission(permission);
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbaritems, menu);

        return true;
    }


    public void getMusicList(){
        list = new ArrayList<>();
        String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
        };

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);

        while(cursor.moveToNext()){
            MusicDto musicDto = new MusicDto();
            musicDto.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            musicDto.setAlbumId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
            musicDto.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            musicDto.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            musicDto.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
            list.add(musicDto);
        }
        cursor.close();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rewind:
                // 이전곡으로 이동
                MusicApplication.getInstance().getServiceInterface().rewind();
                break;
            case R.id.btn_play_pause:
                // 재생 또는 일시정지
                MusicApplication.getInstance().getServiceInterface().togglePlay();
                break;
            case R.id.btn_forward:
                // 다음곡으로 이동
                MusicApplication.getInstance().getServiceInterface().forward();
                break;
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    public void registerBroadcast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastActions.PLAY_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    public void unregisterBroadcast(){
        unregisterReceiver(mBroadcastReceiver);
    }

    private void updateUI() {
        Log.d("mainactivity", String.valueOf(MusicApplication.getInstance().getServiceInterface().isPlaying()));
        if (MusicApplication.getInstance().getServiceInterface().isPlaying()) {
            playerPause_btn.setImageResource(R.drawable.pause2);
        } else {
            playerPause_btn.setImageResource(R.drawable.play2);
        }
        MusicDto musicItem = MusicApplication.getInstance().getServiceInterface().getAudioItem();
        if (musicItem != null) {
            Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), musicItem.getAlbumId());
            Glide.with(this).load(albumArtUri).into(playerImage);

            playerTitle.setText(musicItem.getTitle());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ((Player_MusicData)getSupportFragmentManager().findFragmentByTag("player_View")).setting_player();
                    ((Player_MusicData)getSupportFragmentManager().findFragmentByTag("player_View")).setButtonImg();
                }
            });
        } else {
            playerImage.setImageResource(R.drawable.ic_launcher_foreground);
            playerTitle.setText("재생중인 음악이 없습니다.");
        }
    }
    public int checkVisible_fragment(){
        for(Fragment fragment : getSupportFragmentManager().getFragments()){
            if(fragment.isVisible()){
                if(fragment == allmusic_items){
                    return 1;
                }
                else if(fragment == favorite_items){
                    return 2;
                }
                else if(fragment == random_items){
                    return 3;
                }
            }
        }
        return 0;
    }

    @Override
    public void onBackPressed() {

        if(mainLayout.getVisibility() == View.GONE){
            ((Player_MusicData)getSupportFragmentManager().findFragmentByTag("player_View")).setFragment_Visible(false);
            mainLayout.setVisibility(View.VISIBLE);
            return;
        }

        searchView.onActionViewCollapsed();

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        else if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();


            try{
                Thread.sleep(1000);
            } catch (Exception e){
                e.printStackTrace();
            }


            if(!NotificationPlayer.IsNotificationRunning){
                println("강제종료합니다.");
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }

    public void println(String data){
        Log.d("MainActivity", data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();

        MusicApplication.getServiceInterface().setIsRunning(false);
    }
}

