package com.music.rptang.tingtingmusic;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class MyMusicFragment extends Fragment implements View.OnClickListener{
    private LinearLayout layout_local_songs;
    private MainActivity mainActivity;
    /**
     * 初始化myMusicFragment,在MainActivity中调用
     * @return
     */
    public static MyMusicFragment newInstance(){
        MyMusicFragment myMusicFragment = new MyMusicFragment();
        return myMusicFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_music_fragment,null);
        layout_local_songs = (LinearLayout)view.findViewById(R.id.ll_local_songs);
        layout_local_songs.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_local_songs:
                Intent intent = new Intent(getActivity(),LocalSongsActivity.class);
                startActivity(intent);
                break;
        }
    }
}
