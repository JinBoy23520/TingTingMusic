package com.music.rptang.tingtingmusic.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.music.rptang.tingtingmusic.MainActivity;
import com.music.rptang.tingtingmusic.R;
import com.music.rptang.tingtingmusic.vo.Mp3Info;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MediaUtils {

    //获取专辑封面的Uri
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

    /**
     * 获取默认专辑图片
     */
    public static Bitmap getDefaultArtwork(Context context,boolean small){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        if(small){
            return BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.music_play),null,opts);
        }
        return BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.music_play),null,opts);
    }

    /**
     * 从文件当中获取专辑封面位图
     */
    private static Bitmap getArtworkFromFile(Context context,long songid,long albumid){
        Bitmap bm = null;
        if(albumid<0 && songid<0){
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if(albumid<0){
                Uri uri = Uri.parse("content://media/external/audio/media"
                        +songid+"albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri,"r");
                if(pfd!=null){
                    fd = pfd.getFileDescriptor();
                }
            }else{
                Uri uri = ContentUris.withAppendedId(albumArtUri,albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if(pfd!=null){
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize=1;
            //只进行大小判断
            options.inJustDecodeBounds = true;
            //调用此方法得到options得到图片大小
            BitmapFactory.decodeFileDescriptor(fd,null,options);
            //我们的目标是在800pixel的画面上显示
            //所以需要调用computeSampleSize得到图片缩放的比例
            options.inSampleSize = 100;
            //我们得到了缩放的比例，现在开始正式读入Bitmap数据
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd,null,options);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return bm;
    }

    /**
     * 获取专辑封面位图对象
     */
    public static Bitmap getArtwork(Context context,long song_id,long album_id,boolean allowdefault,boolean small){
        if(album_id<0){
            if(song_id<0){
                Bitmap bm = getArtworkFromFile(context,song_id,-1);
                if(bm!=null){
                    return bm;
                }
            }
            if(allowdefault){
                return getDefaultArtwork(context,small);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUri,album_id);
        if(uri !=null){
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                //先指定原始大小
                options.inSampleSize = 1;
                //只进行大小判断
                options.inJustDecodeBounds = true;
                //调用此方法得到options得到图片的大小
                BitmapFactory.decodeStream(in,null,options);
                //我们的目标是在你N pixel的画面上显示。所以需要调用computeSampleSize得到图片缩放的比例
                //这里的target为800是根据默认专辑图片代傲决定的，800只是测试数字但是试验后发现完美的结合
                if(small){
                    options.inSampleSize = computeSampleSize(options,40);
                }else {
                    options.inSampleSize = computeSampleSize(options,600);
                }
                //我们得到了缩放比例，现在开始正式读入Bitmap数据
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in,null,options);
            }catch (FileNotFoundException e){
                Bitmap bm = getArtworkFromFile(context,song_id,album_id);
                if(bm!=null){
                    if(bm.getConfig()==null){
                        bm = bm.copy(Bitmap.Config.RGB_565,false);
                        if(bm == null && allowdefault){
                            return getDefaultArtwork(context,small);
                        }
                    }else if(allowdefault){
                        bm = getDefaultArtwork(context,small);
                    }
                    return bm;
                }
            }finally {
                try {
                    if(in != null){
                        in.close();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 对图片进行合适的缩放
     */
    public static int computeSampleSize(BitmapFactory.Options options,int target){
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW,candidateH);
        if(candidate == 0){
            return 1;
        }
        if (candidate>1){
            if((w>target)&&(w/candidate)<target){
                candidate -= -1;
            }
        }
        if(candidate>1){
            if((h>target)&&(h/candidate)<target){
                candidate -= -1;
            }
        }
        return candidate;
    }

    /**
     * 用于从数据库中查询歌曲的信息，保存在List当中
     */
    public static ArrayList<Mp3Info> getMp3Infos(Context context){
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                //最小音乐长度
                MediaStore.Audio.Media.DURATION + ">=180000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        ArrayList<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
        for(int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            Mp3Info mp3Info = new Mp3Info();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));//id
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));//歌名
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));//专辑
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));//专辑id
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));//时长
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));//大小
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//路径
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐

            if(isMusic!=0){
                mp3Info.setId(id);
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setAlbum(album);
                mp3Info.setAlbumId(albumId);
                mp3Info.setDuration(duration);
                mp3Info.setSize(size);
                mp3Info.setUrl(url);
                mp3Infos.add(mp3Info);
            }
        }

        cursor.close();
        return mp3Infos;
    }

    /**
     * 格式化时间，将毫秒转换为分：秒格式
     */
    public static String formatTime(long time){
        String min = time / (1000 * 60)+"";
        String sec = time % (1000 * 60)+"";
        if(min.length()<2){
            min = "0"+time / (1000 * 60)+"";
        }else{
            min = time / (1000 * 60)+"";
        }
        if(sec.length()==4){
            sec = "0"+(time % (1000 * 60))+"";
        }else if(sec.length()==3){
            sec = "00"+(time % (1000 * 60))+"";
        }else if(sec.length()==2){
            sec = "000"+(time % (1000 * 60))+"";
        }else if(sec.length()==1){
            sec = "0000"+(time % (1000 * 60))+"";
        }

        return min + ":" +sec.trim().substring(0,2);
    }

//    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
//    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
//
//    public static Bitmap getArtwork(Context context, String title, long song_id, long album_id,
//                                    boolean allowdefault) {
//        if (album_id < 0) {
//            if (song_id >= 0) {
//                Bitmap bm = getArtworkFromFile(context, song_id, -1);
//                if (bm != null) {
//                    return bm;
//                }
//            }
//            if (allowdefault) {
//                return getDefaultArtwork(context);
//            }
//            return null;
//        }
//        ContentResolver res = context.getContentResolver();
//        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
//        if (uri != null) {
//            InputStream in = null;
//            try {
//                in = res.openInputStream(uri);
//                Bitmap bmp = BitmapFactory.decodeStream(in, null, sBitmapOptions);
//                if (bmp == null) {
//                    bmp = getDefaultArtwork(context);
//                }
//                return bmp;
//            } catch (FileNotFoundException ex) {
//                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
//                if (bm != null) {
//                    if (bm.getConfig() == null) {
//                        bm = bm.copy(Bitmap.Config.RGB_565, false);
//                        if (bm == null && allowdefault) {
//                            return getDefaultArtwork(context);
//                        }
//                    }
//                } else if (allowdefault) {
//                    bm = getDefaultArtwork(context);
//                }
//                return bm;
//            } finally {
//                try {
//                    if (in != null) {
//                        in.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return null;
//    }
//
//    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
//        Bitmap bm = null;
//        if (albumid < 0 && songid < 0) {
//            throw new IllegalArgumentException("Must specify an album or a song id");
//        }
//        try {
//            if (albumid < 0) {
//                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
//                ParcelFileDescriptor pfd = context.getContentResolver()
//                        .openFileDescriptor(uri, "r");
//                if (pfd != null) {
//                    FileDescriptor fd = pfd.getFileDescriptor();
//                    bm = BitmapFactory.decodeFileDescriptor(fd);
//                }
//            } else {
//                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
//                ParcelFileDescriptor pfd = context.getContentResolver()
//                        .openFileDescriptor(uri, "r");
//                if (pfd != null) {
//                    FileDescriptor fd = pfd.getFileDescriptor();
//                    bm = BitmapFactory.decodeFileDescriptor(fd);
//                }
//            }
//        } catch (FileNotFoundException ex) {
//
//        }
//        return bm;
//    }
//
//    private static Bitmap getDefaultArtwork(Context context) {
//        BitmapFactory.Options opts = new BitmapFactory.Options();
//        opts.inPreferredConfig = Bitmap.Config.RGB_565;
//        return BitmapFactory.decodeStream(
//                context.getResources().openRawResource(R.drawable.music_play), null,
//                opts);
//    }

}
