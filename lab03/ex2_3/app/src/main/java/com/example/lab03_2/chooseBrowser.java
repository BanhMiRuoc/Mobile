package com.example.lab03_2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class chooseBrowser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getStringExtra("URL");
        if (url != null) {
            openUrlWithBrowserChooser(url);
        }
        finish();
    }

    private void openUrlWithBrowserChooser(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

        Intent chooser = Intent.createChooser(intent, "Open with Browser");

        Intent webViewIntent = new Intent(chooseBrowser.this, WebViewActivity.class);
        webViewIntent.putExtra("URL", url);

        Intent[] intentArray = {webViewIntent};
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        startActivity(chooser);

    }
}
