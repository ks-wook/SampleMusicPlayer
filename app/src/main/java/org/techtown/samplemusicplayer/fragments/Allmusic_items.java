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


public class Allmusic_items extends Fragment {
    private ArrayList<MusicDto> list = new ArrayList<MusicDto>();
    MyAdapter adapter;

    RecyclerView musicRecyclerView;

    public Allmusic_items(MyAdapter adapter, ArrayList<MusicDto> list) {
        this.adapter = adapter;
        this.list = list;
    }

    public Allmusic_items() { }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_allmusic_items, container, false);

        musicRecyclerView = (rootView).findViewById(R.id.musicRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        musicRecyclerView.setLayoutManager(layoutManager);

        RecyclerView.ItemAnimator animator = musicRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        adapter.setItems(list);
        musicRecyclerView.setAdapter(adapter);


        return rootView;
    }
}