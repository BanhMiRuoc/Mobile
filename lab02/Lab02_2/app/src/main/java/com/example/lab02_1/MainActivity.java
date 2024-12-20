package com.example.lab02_1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView tvFollowers;
    private Button btnFollow;
    private boolean isFollowing = false;
    private int followersCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        tvFollowers = findViewById(R.id.tv_followers);
        TextView tvFollowing = findViewById(R.id.tv_following);
        btnFollow = findViewById(R.id.btn_follow);

        // Set random followers and following count
        Random random = new Random();
        followersCount = random.nextInt(9901) + 100; // Random number between 100 and 10000
        int followingCount = random.nextInt(9901) + 100; // Random number between 100 and 10000

        tvFollowers.setText(String.valueOf(followersCount));
        tvFollowing.setText(String.valueOf(followingCount));

        // Set up button click listener
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollowing) {
                    // Unfollow action
                    followersCount--;
                    btnFollow.setText("FOLLOW");
                    isFollowing = false;
                } else {
                    // Follow action
                    followersCount++;
                    btnFollow.setText("UNFOLLOW");
                    isFollowing = true;
                }
                // Update followers count
                tvFollowers.setText(String.valueOf(followersCount));
            }
        });
    }
}
