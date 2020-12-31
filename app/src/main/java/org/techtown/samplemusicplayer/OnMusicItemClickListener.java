package org.techtown.samplemusicplayer;

import android.view.View;

import org.techtown.samplemusicplayer.Main.MyAdapter;

public interface OnMusicItemClickListener {
    public void onItemClick(MyAdapter.ViewHolder holder, View view, int position);
}
