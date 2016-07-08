package com.music.rptang.tingtingmusic;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.music.rptang.tingtingmusic.utils.ImageUtils;
import com.music.rptang.tingtingmusic.utils.MediaUtils;
import com.music.rptang.tingtingmusic.vo.Mp3Info;

import java.util.ArrayList;

public class PlayUIActivity extends BaseActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener{

    private ImageView iv_pull_down,iv_play_ui_play,iv_play_ui_next,iv_play_ui_previous,iv_play_ui_play_mode,iv_ablum2,imageView1_ablum_reflection;
    private TextView tv_play_ui_song,tv_play_ui_artist,tv_play_ui_end_time,tv_play_ui_play_time;
    private ArrayList<Mp3Info> mp3Infos;
    private SeekBar sb_play_ui_seekbar;
    private static final int UPDATE_TIME = 0x1;
    private static MyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_play_ui);
        iv_pull_down = (ImageView)findViewById(R.id.iv_pull_down);
        iv_ablum2 = (ImageView)findViewById(R.id.iv_ablum2);
        imageView1_ablum_reflection=(ImageView)findViewById(R.id.imageView1_ablum_reflection);
        tv_play_ui_song = (TextView)findViewById(R.id.tv_play_ui_song);
        tv_play_ui_artist = (TextView)findViewById(R.id.tv_play_ui_artist);
        tv_play_ui_end_time = (TextView)findViewById(R.id.tv_play_ui_end_time);
        tv_play_ui_play_time = (TextView)findViewById(R.id.tv_play_ui_play_time);
        iv_play_ui_play = (ImageView)findViewById(R.id.iv_play_ui_play);
        iv_play_ui_next = (ImageView)findViewById(R.id.iv_play_ui_next);
        iv_play_ui_previous = (ImageView)findViewById(R.id.iv_play_ui_previous);
        iv_play_ui_play_mode = (ImageView)findViewById(R.id.iv_play_ui_play_mode);
        sb_play_ui_seekbar = (SeekBar)findViewById(R.id.sb_play_ui_seekbar);
        iv_pull_down.setOnClickListener(this);
        iv_play_ui_play.setOnClickListener(this);
        iv_play_ui_next.setOnClickListener(this);
        iv_play_ui_previous.setOnClickListener(this);
        iv_play_ui_play_mode.setOnClickListener(this);
        sb_play_ui_seekbar.setOnSeekBarChangeListener(this);
        mp3Infos = MediaUtils.getMp3Infos(this);
        myHandler = new MyHandler(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindMusicPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindMusicPlayService();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            musicPlayService.pause();//暂停
            musicPlayService.seekTo(progress);//拖动
            musicPlayService.start();//播放
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 进度条控件已经内部处理过了，开始时间的改变是在子线程中改变主线程的UI，这当然是不可以的
     * 怎么办呢，用你最熟悉的Handler处理吧
     */
    static class MyHandler extends android.os.Handler{
        //内部类去要想使用外部类的权限，就得把外部类拿进来
        private PlayUIActivity playUIActivity;
        public MyHandler(PlayUIActivity playUIActivity){
            this.playUIActivity = playUIActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(playUIActivity!=null){
                switch (msg.what){
                    case UPDATE_TIME:
                        playUIActivity.tv_play_ui_play_time.setText(MediaUtils.formatTime(msg.arg1));
                        break;
                }
            }
        }
    }

    //这里是子线程，不断的发送msg给主线程，通知其更改UI
    @Override
    public void publish(int progress) {
        Message msg = myHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        myHandler.sendMessage(msg);
        sb_play_ui_seekbar.setProgress(progress);
    }

    @Override
    public void change(int position) {
        Mp3Info mp3Info = mp3Infos.get(position);
        tv_play_ui_song.setText(mp3Info.getTitle());
        tv_play_ui_artist.setText(mp3Info.getArtist());
        tv_play_ui_end_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        iv_play_ui_play.setImageResource(R.drawable.pause);
        //获取专辑封面图片
        Bitmap albumBitmap = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
        //改变播放界面专辑封面图片
        iv_ablum2.setImageBitmap(albumBitmap);
        sb_play_ui_seekbar.setProgress(0);
        sb_play_ui_seekbar.setMax((int)mp3Info.getDuration());
        if(musicPlayService.isPlaying()){
            iv_play_ui_play.setImageResource(R.drawable.pause);
        }else {
            iv_play_ui_play.setImageResource(R.drawable.play);
        }
        if(albumBitmap !=null) {
            imageView1_ablum_reflection.setImageBitmap(ImageUtils.createReflectionBitmapForSingle(albumBitmap));//显示倒影
        }



        switch (musicPlayService.getPlay_mode()){
            case MusicPlayService.ORDER_PLAY:
                iv_play_ui_play_mode.setImageResource(R.drawable.list_cycle);
                //iv_play_ui_play_mode.setTag(MusicPlayService.ORDER_PLAY);
                break;
            case MusicPlayService.RANDOM_PLAY:
                iv_play_ui_play_mode.setImageResource(R.drawable.random);
                //iv_play_ui_play_mode.setTag(MusicPlayService.RANDOM_PLAY);
                break;
            case MusicPlayService.SINGLE_PLAY:
                iv_play_ui_play_mode.setImageResource(R.drawable.single_cycle);
                //iv_play_ui_play_mode.setTag(MusicPlayService.SINGLE_PLAY);
                break;
            default:
                break;
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_pull_down:
                finish();
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                break;
            case R.id.iv_play_ui_play:
                if(musicPlayService.isPlaying()){
                    musicPlayService.pause();
                    iv_play_ui_play.setImageResource(R.drawable.play);
                }else{
                    if(musicPlayService.isPause()){
                        musicPlayService.start();
                        iv_play_ui_play.setImageResource(R.drawable.pause);
                    }else{
                        musicPlayService.play(0);
                    }
                }
                break;
            case R.id.iv_play_ui_previous:
                musicPlayService.previous();
                break;
            case R.id.iv_play_ui_next:
                musicPlayService.next();
                break;
            case R.id.iv_play_ui_play_mode:
                switch (musicPlayService.getPlay_mode()){
                    case MusicPlayService.ORDER_PLAY:
                        iv_play_ui_play_mode.setImageResource(R.drawable.random);
                        musicPlayService.setPlay_mode(MusicPlayService.RANDOM_PLAY);
                        Toast.makeText(getApplicationContext(), "随机播放", Toast.LENGTH_SHORT).show();
                        break;
                    case MusicPlayService.RANDOM_PLAY:
                        iv_play_ui_play_mode.setImageResource(R.drawable.single_cycle);
                        musicPlayService.setPlay_mode(MusicPlayService.SINGLE_PLAY);
                        Toast.makeText(getApplicationContext(),"单曲循环",Toast.LENGTH_SHORT).show();
                        break;
                    case MusicPlayService.SINGLE_PLAY:
                        iv_play_ui_play_mode.setImageResource(R.drawable.list_cycle);
                        musicPlayService.setPlay_mode(MusicPlayService.ORDER_PLAY);
                        Toast.makeText(getApplicationContext(),"顺序播放",Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            default:
                break;

        }
    }
}
