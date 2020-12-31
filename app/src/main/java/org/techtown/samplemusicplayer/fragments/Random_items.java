package org.techtown.samplemusicplayer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.techtown.samplemusicplayer.Main.MyAdapter;
import org.techtown.samplemusicplayer.Music.MusicDto;
import org.techtown.samplemusicplayer.R;

import java.util.ArrayList;


public class Random_items extends Fragment {
    private ArrayList<MusicDto> list = new ArrayList<MusicDto>();
    MyAdapter adapter;

    RecyclerView randomRecyclerView;

    ImageButton btn_renew;

    public Random_items(MyAdapter adapter) {
        this.adapter = adapter;
    }

    public Random_items() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_random_items, container, false);
        list = adapter.loadRandomList();

        randomRecyclerView = (rootView).findViewById(R.id.randomRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        randomRecyclerView.setLayoutManager(layoutManager);

        RecyclerView.ItemAnimator animator = randomRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        adapter.setItems(list);
        randomRecyclerView.setAdapter(adapter);

        btn_renew = (rootView).findViewById(R.id.btn_renew);
        btn_renew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.mainActivity.adapter_refreshRandom();
            }
        });

        return rootView;
    }


}