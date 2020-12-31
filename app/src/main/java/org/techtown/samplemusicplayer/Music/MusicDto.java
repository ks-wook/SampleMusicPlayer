package org.techtown.samplemusicplayer.Music;

import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;

public class MusicDto implements Serializable {
    private long id;
    private long albumId;
    private String title;
    private String artist;
    private String album;
    private long duration;
    public String DataPath;

    public MusicDto() {
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDataPath() {
        return DataPath;
    }

    public void setDataPath(String mDataPath) {
        this.DataPath = mDataPath;
    }

    public MusicDto(long id, long albumId, String title, String artist, String album, long duration) {
        this.id = id;
        this.albumId = albumId;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "MusicDto{" +
                "id='" + id + '\'' +
                ", albumId='" + albumId + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public MusicDto setValues(Cursor cursor) {
        this.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
        this.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
        this.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
        this.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
        this.album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
        this.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));
        this.DataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
        return MusicDto.this;
    }
}


