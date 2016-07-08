package com.music.rptang.tingtingmusic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.music.rptang.tingtingmusic.adapter.LocalSongsListAdapter;
import com.music.rptang.tingtingmusic.utils.MediaUtils;
import com.music.rptang.tingtingmusic.vo.Mp3Info;

import java.util.ArrayList;

public class LocalSongsActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{

    private ListView lv_local_songs_list;
    private ArrayList<Mp3Info> mp3Infos;
    private LocalSongsListAdapter localSongsListAdapter;
    private ImageView iv_backtrack,iv_album,iv_previous,iv_play,iv_next;
    private TextView tv_song_name1,tv_song_artist1;
    private RelativeLayout rl_music_play_control;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_local_songs);
        lv_local_songs_list = (ListView)findViewById(R.id.lv_local_songs_list);
        iv_backtrack = (ImageView)findViewById(R.id.iv_backtrack);
        iv_album = (ImageView) findViewById(R.id.iv_album);
        iv_play = (ImageView)findViewById(R.id.iv_play);
        iv_next = (ImageView)findViewById(R.id.iv_next);
        iv_previous = (ImageView)findViewById(R.id.iv_previous);
        tv_song_name1 = (TextView)findViewById(R.id.tv_song_name1);
        tv_song_artist1 = (TextView)findViewById(R.id.tv_song_artist1);
        rl_music_play_control = (RelativeLayout)findViewById(R.id.rl_music_play_control);
        rl_music_play_control.setOnClickListener(this);
        iv_play.setOnClickListener(this);
        iv_backtrack.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        iv_previous.setOnClickListener(this);
        lv_local_songs_list.setOnItemClickListener(this);
        initDate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //绑定播放服务
        bindMusicPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindMusicPlayService();
    }

    /**
     * 初始化本地音乐列表
     */
    private void initDate() {
        mp3Infos = MediaUtils.getMp3Infos(this);
        System.out.println(mp3Infos.size());
        localSongsListAdapter = new LocalSongsListAdapter(this,mp3Infos);
        localSongsListAdapter.notifyDataSetChanged();
        lv_local_songs_list.setAdapter(localSongsListAdapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_backtrack:
                finish();
                break;
            case R.id.iv_play:
                if(this.musicPlayService.isPlaying()){
                    this.musicPlayService.pause();
                    iv_play.setImageResource(R.drawable.play);
                }else{
                    if(this.musicPlayService.isPause()){
                        this.musicPlayService.start();
                        iv_play.setImageResource(R.drawable.pause);
                    }else {
                        this.musicPlayService.play(0);
                        iv_play.setImageResource(R.drawable.pause);
                    }
                }

                break;
            case R.id.iv_next:
                this.musicPlayService.next();
                iv_play.setImageResource(R.drawable.pause);
                break;
            case R.id.iv_previous:
                this.musicPlayService.previous();
                iv_play.setImageResource(R.drawable.pause);
                break;
            case R.id.rl_music_play_control:
                Intent intent = new Intent(this,PlayUIActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                break;
        }
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        musicPlayService.play(position);
        iv_play.setImageResource(R.drawable.pause);
    }
    @Override
    public void publish(int progress) {

    }
    @Override
    public void change(int position) {
        //切换状态播放位置
        changeUIStatus(position);
    }
    //回调播放状态下的UI设置
    public void changeUIStatus(int position){
        if(position>=0 && position<mp3Infos.size()){
            Mp3Info mp3Info = mp3Infos.get(position);
            tv_song_name1.setText(mp3Info.getTitle());
            tv_song_artist1.setText(mp3Info.getArtist());

            if(musicPlayService.isPlaying()){
                iv_play.setImageResource(R.drawable.pause);
            }else {
                iv_play.setImageResource(R.drawable.play);
            }
            //Bitmap albumBitmap =  MediaUtils.getArtwork(this, mp3Info.getTitle(),mp3Info.getId(), mp3Info.getAlbumId(),true);
            Bitmap albumBitmap =  MediaUtils.getArtwork(this,mp3Info.getId(), mp3Info.getAlbumId(),true,false);
            if(albumBitmap==null){
                iv_album.setImageResource(R.drawable.music_play);
            }else{
                iv_album.setImageBitmap(albumBitmap);
            }
        }
    }
}

