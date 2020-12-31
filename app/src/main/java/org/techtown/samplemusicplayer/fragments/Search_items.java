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


public class Search_items extends Fragment {

    ArrayList<MusicDto> filteredList = new ArrayList<MusicDto>();
    MyAdapter adapter;

    RecyclerView searchRecyclerView;


    public Search_items(MyAdapter adapter) {
        this.adapter = adapter;
    }

    public Search_items() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search__items, container, false);

        searchRecyclerView = rootView.findViewById(R.id.searchRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        searchRecyclerView.setLayoutManager(layoutManager);

        RecyclerView.ItemAnimator animator = searchRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        searchRecyclerView.setAdapter(adapter);

        return rootView;
    }
}