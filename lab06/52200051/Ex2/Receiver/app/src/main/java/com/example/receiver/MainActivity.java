package com.example.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class MainActivity extends AppCompatActivity {
    private TextView tvMessage;
    private static final String CHANNEL_ID = "MyChannel";

    // Tạo một instance của BroadcastReceiver
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Nhận thông điệp từ Intent
            if ("com.example.sender.ACTION_SEND".equals(intent.getAction())){
                String message = intent.getStringExtra("message");
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                showNotification(context, message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMessage = findViewById(R.id.tvMessage);
        IntentFilter intentFilter = new IntentFilter("com.example.sender.ACTION_SEND");
        registerReceiver(myBroadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED);

        createNotificationChannel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // UnRegister the receiver when ever you pause the activity to avoid leak of receiver.
        unregisterReceiver(myBroadcastReceiver);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Message Channel";
            String description = "Channel for receiving messages";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Đăng ký kênh với NotificationManager
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Context context, String message) {
        // Intent để mở MainActivity khi người dùng bấm vào thông báo
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Xây dựng Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // Icon của thông báo
                .setContentTitle("New Message")  // Tiêu đề thông báo
                .setContentText(message)  // Nội dung thông báo
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // Độ ưu tiên của thông báo
                .setContentIntent(pendingIntent)  // Intent khi người dùng bấm vào thông báo
                .setAutoCancel(true);  // Tự động đóng thông báo khi bấm vào

        // Hiển thị thông báo
        tvMessage.setText("Chay duoc tb ne");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

}
