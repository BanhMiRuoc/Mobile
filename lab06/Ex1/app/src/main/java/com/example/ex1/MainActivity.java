package com.example.ex1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ex1.Adapter.ItemAdapter;
import com.example.ex1.Model.Item;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView btnAdd;
    List<Item> itemList = new ArrayList<>();
    ItemAdapter itemAdapter;
    private NetworkChangeReceiver networkChangeReceiver;
    Thread uiThread;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.rcv);
        btnAdd = findViewById(R.id.imageView);

        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);

        uiThread = Thread.currentThread();
        handler = new Handler();
        if (!isNetworkConnected()) {
            showNoConnectionDialog();
        } else {
            getStudent();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
            }
        }).start();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddPage.class);
                startActivityForResult(intent, 1);
            }
        });
        itemAdapter = new ItemAdapter(MainActivity.this, itemList, MainActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(itemAdapter);
       //System.out.println(itemList);

    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void showNoConnectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK){
            String name = data.getStringExtra("name");
            String mail = data.getStringExtra("mail");
            String phone = data.getStringExtra("phone");
            addStudent(name, mail, phone);
        }
        else if(requestCode == 2 && resultCode == RESULT_OK){
            String id = data.getStringExtra("idU");
            String name = data.getStringExtra("nameU");
            String mail = data.getStringExtra("mailU");
            String phone = data.getStringExtra("phoneU");
            updateStudent(id, name, mail, phone);
        }
    }

    private void updateStudent(String id, String name, String email, String phone){
        OkHttpClient client = new OkHttpClient();
        String createStudentURL = "http://10.0.2.2/api/update-student.php";
        RequestBody formBody = new FormBody.Builder()
                .add("id", id)
                .add("name", name)
                .add("email", email)
                .add("phone", phone)
                .build();
        Request request = new Request.Builder()
                .url(createStudentURL)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("onFailure", e.getMessage());
            }
            @Override
            public void onResponse(Call call, final Response response)
                    throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (Item s: itemList) {
                                if (s.getId() == Integer.parseInt(id)){
                                    s.setName(name);
                                    s.setMail(email);
                                    s.setNumber(phone);
                                }
                            }
                            Toast.makeText(MainActivity.this, "Update Successfully", Toast.LENGTH_SHORT).show();
                            itemAdapter.notifyDataSetChanged();

                        }
                    });
                } catch (JSONException e) {
                    Log.d("onResponse", e.getMessage());
                }
            }
        });
    }

    private void addStudent(String name, String email, String phone){
        OkHttpClient client = new OkHttpClient();
        String createStudentURL = "http://10.0.2.2/api/add-student.php";
        RequestBody formBody = new FormBody.Builder()
                .add("name", name)
                .add("email", email)
                .add("phone", phone)
                .build();
        Request request = new Request.Builder()
                .url(createStudentURL)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("onFailure", e.getMessage());
            }
            @Override
            public void onResponse(Call call, final Response response)
                    throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            itemList.add(new Item(name, email, phone));
                            Toast.makeText(MainActivity.this, "Add Successfully", Toast.LENGTH_SHORT).show();
                            itemAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (JSONException e) {
                    Log.d("onResponse", e.getMessage());
                }
            }
        });
    }

    public List<Item> getStudent(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://10.0.2.2/api/get-students.php").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("onFailure", e.getMessage());
            }
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray data = json.getJSONArray("data");
                                itemList.clear();
                                for (int i = 0; i < data.length(); i++) {
                                    int id = data.getJSONObject(i).getInt("id");
                                    String name = data.getJSONObject(i).getString("name");
                                    String email = data.getJSONObject(i).getString("email");
                                    String phone = data.getJSONObject(i).getString("phone");

                                    Item item = new Item(id, name, email, phone);
                                    itemList.add(item);
                                }
                                itemAdapter.notifyDataSetChanged();
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    Log.d("onResponse", e.getMessage());
                }
            }
        });
        return itemList;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }
}