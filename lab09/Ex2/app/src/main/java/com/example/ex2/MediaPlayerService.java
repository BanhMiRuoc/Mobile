package com.example.ex2;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.ex2.Model.AudioFile;

import java.util.ArrayList;
import java.util.List;

public class MediaPlayerService extends Service {
    MediaPlayer mediaPlayer;
    List<AudioFile> playlist;
    int currentTrackIndex = 0;
    ServiceController binder = new ServiceController();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        playlist = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                binder.next();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class ServiceController extends Binder{
        public void play(int index){
            currentTrackIndex = index;
            AudioFile audioFile = playlist.get(currentTrackIndex);
            try {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(audioFile.getFilePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void play(){
            mediaPlayer.start();
        }

        public void pause(){
            mediaPlayer.pause();
        }

        public void next(){
            currentTrackIndex++;
            if (currentTrackIndex >= playlist.size()){
                currentTrackIndex = 0;
            }
            play(currentTrackIndex);
        }

        public void back(){
            currentTrackIndex--;
            if (currentTrackIndex < 0){
                currentTrackIndex = playlist.size() - 1;
            }
            play(currentTrackIndex);
        }

        public void setPlayList(List<AudioFile> data){
            MediaPlayerService.this.playlist = data;
        }

        public List<AudioFile> getPlayList(){
            return playlist;
        }

        public AudioFile getCurrentTrack(){
            return playlist.get(currentTrackIndex);
        }
    }
}
