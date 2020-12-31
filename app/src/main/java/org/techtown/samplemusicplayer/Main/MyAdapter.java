package org.techtown.samplemusicplayer.Main;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import org.techtown.samplemusicplayer.DatabaseHelper;
import org.techtown.samplemusicplayer.Music.MusicApplication;
import org.techtown.samplemusicplayer.Music.MusicDto;
import org.techtown.samplemusicplayer.MusicService;
import org.techtown.samplemusicplayer.OnMusicItemClickListener;
import org.techtown.samplemusicplayer.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>
implements OnMusicItemClickListener {

    public static String tableName = "FAVORITE_LIST_TABLE";

    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;


    ArrayList<MusicDto> list;
    ArrayList<MusicDto> FavoriteList = new ArrayList<MusicDto>();
    ArrayList<MusicDto> list_backup = new ArrayList<MusicDto>();

    LayoutInflater inflater;
    OnMusicItemClickListener listener;

    Activity activity;
    public MainActivity mainActivity;

    ImageView imageView;
    TextView title;
    TextView artist;
    TextView duration;

    android.text.format.DateFormat df = new android.text.format.DateFormat();

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {

        Bitmap albumImage = getAlbumImage(activity, String.valueOf(list.get(position).getAlbumId()), 100);

        imageView = (ImageView) holder.itemView.findViewById(R.id.album);
        title = (TextView) holder.itemView.findViewById(R.id.title);
        artist = (TextView) holder.itemView.findViewById(R.id.artist);
        duration = (TextView) holder.itemView.findViewById(R.id.duration);

        imageView.setImageBitmap(albumImage);
        title.setText(list.get(position).getTitle());
        artist.setText(list.get(position).getArtist());
        duration.setText(df.format("mm:ss", list.get(position).getDuration()));

        if(executeFavorite(String.valueOf(list.get(position).getTitle()))) {
            holder.favoriteButton.setImageResource(R.drawable.star);
            if(!FavoriteList.contains(list.get(position))) {
                FavoriteList.add(list.get(position));
            }
        }
        else {
            holder.favoriteButton.setImageResource(R.drawable.empty_star);
        }
    }


    public MyAdapter(Activity activity, MainActivity mainActivity, ArrayList<MusicDto> list) {
        this.list = list;
        this.activity = activity;
        this.mainActivity = mainActivity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        list_backup.addAll(list);
        createDatabase();
        createTable();

        if(FavoriteList == null){
            FavoriteList = loadFavoriteMusics();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public void setItems(ArrayList<MusicDto> items) {
        this.list = new ArrayList<MusicDto>();
        if(items != null) {
            this.list.addAll(items);
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.recyclerview_item, viewGroup, false);

        return new ViewHolder(itemView, this);
    }

    private static final BitmapFactory.Options options = new BitmapFactory.Options();

    private static Bitmap getAlbumImage(Context context, String album_id, int MAX_IMAGE_SIZE) {
        ContentResolver res = context.getContentResolver();
        Uri uri = Uri.parse("content://media/external/audio/albumart/" + album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, options);
                int scale = 0;
                if (options.outHeight > MAX_IMAGE_SIZE || options.outWidth > MAX_IMAGE_SIZE) {
                    scale = (int) Math.pow(2, (int) Math.round(Math.log(MAX_IMAGE_SIZE / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
                }
                options.inJustDecodeBounds = false;
                options.inSampleSize = scale;

                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, options);

                if (b != null) {
                    if (options.outWidth != MAX_IMAGE_SIZE || options.outHeight != MAX_IMAGE_SIZE) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE, true);
                        b.recycle();
                        b = tmp;
                    }
                }

                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }

    public ArrayList<Long> getAudioIds() {
        int count = getItemCount();
        ArrayList<Long> audioIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            audioIds.add(list.get(i).getId());
        }
        return audioIds;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageButton favoriteButton;


        public ViewHolder(View itemView, final OnMusicItemClickListener listener) {
            super(itemView);

            this.itemView = itemView;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    MusicApplication.getInstance().getServiceInterface().setPlayList(getAudioIds());
                    MusicApplication.getInstance().getServiceInterface().play(position);
                }
            });

            favoriteButton = itemView.findViewById(R.id.favoriteCheck);
            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!executeFavorite(list.get(getAdapterPosition()).getTitle())) {
                        favoriteButton.setImageResource(R.drawable.star);
                        insertRecord(list.get(getAdapterPosition()));
                        FavoriteList.add(list.get(getAdapterPosition()));
                        Toast.makeText(mainActivity, "즐겨찾기에 추가되었습니다", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        favoriteButton.setImageResource(R.drawable.empty_star);

                        if(mainActivity.checkVisible_fragment() == 1){
                            FavoriteList.remove(FavoriteList.indexOf(list_backup.get(getAdapterPosition())));
                            resetDatabase();
                            refreshDatabase();
                        }
                        else if(mainActivity.checkVisible_fragment() == 2){
                            FavoriteList.remove(FavoriteList.indexOf(FavoriteList.get(getAdapterPosition())));
                            resetDatabase();
                            refreshDatabase();
                            mainActivity.adapter_refreshFavorite();
                        }
                        else if(mainActivity.checkVisible_fragment() == 3){
                            FavoriteList.remove(FavoriteList.indexOf(randomList.get(getAdapterPosition())));
                            resetDatabase();
                            refreshDatabase();
                        }
                    }
                }
            });
        }
    }

    public void createDatabase() {
        dbHelper = new DatabaseHelper(activity);
        database = dbHelper.getWritableDatabase();
        MusicService.IsRunning = true;
    }

    private void createTable() {

        if (database == null) {
            return;
        }

        database.execSQL("create table if not exists " + tableName + "("
                + "_id integer PRIMARY KEY autoincrement, "
                + " title text)");

    }

    //즐겨찾기 데이터베이스에 삽입
    private void insertRecord(MusicDto favorite){
        if(database == null){
            return;
        }

        ContentValues values = new ContentValues();
        values.put("title", favorite.getTitle());
        database.insert(tableName, null, values);

    }

    public boolean executeFavorite(String title_data) {

        cursor = database.rawQuery("select _id, title from FAVORITE_LIST_TABLE", null);
        int recordCount = cursor.getCount();

        for(int i = 0; i<recordCount; i++){
            cursor.moveToNext();

            String title = cursor.getString(cursor.getColumnIndex("title"));
            if(title.equals(title_data)) {
                cursor.close();
                return true;
            }
        }
        cursor.close();
        return false;
    }

    private void resetDatabase() {
        database.execSQL("DELETE FROM FAVORITE_LIST_TABLE");
    }

    private void refreshDatabase() {
        for(int i = 0; i<FavoriteList.size(); i++){
            insertRecord(FavoriteList.get(i));
        }
    }

    //즐겨찾기 목록 가져오기
    public ArrayList<MusicDto> loadFavoriteMusics(){
        return FavoriteList;
    }



    public static ArrayList<MusicDto> filteredList = new ArrayList<MusicDto>();
    //검색 필터
    public static ArrayList<MusicDto> filter(ArrayList<MusicDto>list, String query){

        if(query != null) {
            final String lowercase = query.toLowerCase();
            filteredList.clear();
            for (MusicDto data : list) {
                String title = data.getTitle().toLowerCase();
                if (title.contains(lowercase)) {
                    filteredList.add(data);
                }
            }
        }

        return filteredList;
    }

    ArrayList<MusicDto> randomList = new ArrayList<MusicDto>();

    public ArrayList<MusicDto> loadRandomList() {

        setItems(randomList);
        randomList.clear();
        int[] random_arr = new int[10];
        boolean[] overlapCheck = new boolean[list_backup.size()];

        int count = 0;
        while(count < 10){
            int temp = (int) ((Math.random() * 10000) / list_backup.size() + 1);
            if(!overlapCheck[temp]){
                overlapCheck[temp] = true;
                random_arr[count] = temp;
                count++;
            }
        }

        for(int i = 0; i<10; i++){
            randomList.add(list_backup.get(random_arr[i]));
        }

        return randomList;
    }
}




