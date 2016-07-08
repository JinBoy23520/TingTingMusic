package com.music.rptang.tingtingmusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.music.rptang.tingtingmusic.R;
import com.music.rptang.tingtingmusic.utils.MediaUtils;
import com.music.rptang.tingtingmusic.vo.Mp3Info;

import java.util.ArrayList;


public class LocalSongsListAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<Mp3Info> mp3Infos;

    public LocalSongsListAdapter(Context context,ArrayList<Mp3Info> mp3Infos){
        this.context = context;
        this.mp3Infos = mp3Infos;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos){
        this.mp3Infos = mp3Infos;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_music_list,null);
            vh = new ViewHolder();
            vh.tv_song_name = (TextView)convertView.findViewById(R.id.tv_song_name);
            vh.tv_song_artist = (TextView)convertView.findViewById(R.id.tv_song_artist);
            vh.tv_song_album = (TextView)convertView.findViewById(R.id.tv_song_album);
            vh.tv_song_duration = (TextView)convertView.findViewById(R.id.tv_song_duration);
            convertView.setTag(vh);
        }else{
            vh = (ViewHolder)convertView.getTag();
        }
        //给控件赋值要写在if语句外面，否则第一次加载数据失败
        Mp3Info mp3Info = mp3Infos.get(position);
        vh.tv_song_name.setText(mp3Info.getTitle());
        vh.tv_song_artist.setText(mp3Info.getArtist());
        vh.tv_song_album.setText(mp3Info.getAlbum());
        vh.tv_song_duration.setText(MediaUtils.formatTime(mp3Info.getDuration()));

        return convertView;
    }

    static class ViewHolder{
        TextView tv_song_name;
        TextView tv_song_artist;
        TextView tv_song_album;
        TextView tv_song_duration;
    }
}
