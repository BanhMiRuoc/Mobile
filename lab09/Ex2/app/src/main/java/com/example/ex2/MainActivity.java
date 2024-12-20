package com.example.ex2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ex2.Model.AudioFile;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<AudioFile> data;
    ArrayAdapter<AudioFile> adapter;
    ListView listView;
    TextView txtSBLeft;
    TextView txtSBRight;
    TextView txtSBTitle;
    ProgressBar seekBar;
    Button btnBack;
    Button btnNext;
    Button btnPause;

    Boolean isServiceBound = false;
    MediaPlayerService.ServiceController serviceController;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            isServiceBound = true;
            serviceController = (MediaPlayerService.ServiceController) service;
            List<AudioFile> playlist = serviceController.getPlayList();
            if (playlist.size() > 0){
                data.clear();
                data.addAll(playlist);
                adapter.notifyDataSetChanged();
                AudioFile audioFile = serviceController.getCurrentTrack();
                txtSBTitle.setText(audioFile.getFileName());
            }else{
                loadAudioFiles();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };

    private void loadAudioFiles(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){

            }else{
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            }
        }else{
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DURATION
            };

            Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    null
            );
            while (cursor.moveToNext()){
                @SuppressLint("Range") AudioFile audioFile = new AudioFile(
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                );
                data.add(audioFile);
            }
            serviceController.setPlayList(data);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        txtSBRight = findViewById(R.id.txtSBRight);
        txtSBLeft = findViewById(R.id.txtSBLeft);
        txtSBTitle = findViewById(R.id.txtSBTitle);
        seekBar = findViewById(R.id.seekBar);
        btnPause = findViewById(R.id.btnPause);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        data = new ArrayList<>();

        adapter = new ArrayAdapter<AudioFile>(
            this,
            R.layout.list_item,
            R.id.txtItemName,
            data
        );
        listView.setAdapter(adapter);

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnPause.getText().toString().equals("Pause")){
                    btnPause.setText("Play");
                    serviceController.pause();
                }else{
                    btnPause.setText("Pause");
                    serviceController.play();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceController.next();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceController.back();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                txtSBTitle.setText(data.get(i).getFileName());
                txtSBLeft.setText("0.0");
                txtSBRight.setText(data.get(i).getDurationString());

                serviceController.play(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);

        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(serviceConnection);
    }
}