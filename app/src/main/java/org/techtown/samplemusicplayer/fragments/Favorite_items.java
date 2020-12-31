package org.techtown.samplemusicplayer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.techtown.samplemusicplayer.Music.MusicDto;
import org.techtown.samplemusicplayer.Main.MyAdapter;
import org.techtown.samplemusicplayer.R;

import java.util.ArrayList;


public class Favorite_items extends Fragment {
    private ArrayList<MusicDto> list = new ArrayList<MusicDto>();
    MyAdapter adapter;

    RecyclerView favoriteMusics;

    public Favorite_items(MyAdapter adapter) {
        this.adapter = adapter;
    }

    public Favorite_items() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_favorite_items, container, false);
        list = adapter.loadFavoriteMusics();

        favoriteMusics = (rootView).findViewById(R.id.favoriteMusics);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        favoriteMusics.setLayoutManager(layoutManager);

        RecyclerView.ItemAnimator animator = favoriteMusics.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        adapter.setItems(list);
        favoriteMusics.setAdapter(adapter);




        return rootView;
    }

}