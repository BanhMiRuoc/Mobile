package com.example.ex5;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private int totalUsers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserAdapter(userList);
        recyclerView.setAdapter(userAdapter);

        TextView totalUsersText = findViewById(R.id.totalUsers);
        Button addUsersButton = findViewById(R.id.addUsersButton);
        Button removeUsersButton = findViewById(R.id.removeUsersButton);

        addUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUsers();
                totalUsersText.setText("Total users: " + userList.size());
            }
        });

        removeUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeUsers();
                totalUsersText.setText("Total users: " + userList.size());
            }
        });
    }

    private void addUsers() {
        for (int i = 0; i < 5; i++) {
            totalUsers++;
            User user = new User("User " + totalUsers, "user" + totalUsers + "@domain.com");
            userList.add(user);
        }
        userAdapter.notifyDataSetChanged();
    }

    private void removeUsers() {
        if (userList.size() < 5) {
            userList.clear();
            Toast.makeText(this, "List of users is empty", Toast.LENGTH_SHORT).show();
        } else {
            for (int i = 0; i < 5; i++) {
                userList.remove(userList.size() - 1);
            }
        }
        userAdapter.notifyDataSetChanged();
    }
}
