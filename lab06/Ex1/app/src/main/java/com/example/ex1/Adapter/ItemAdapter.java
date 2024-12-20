package com.example.ex1.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ex1.Model.Item;
import com.example.ex1.R;
import com.example.ex1.UpdatePage;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {
    Context context;
    List<Item> itemList;
    Activity activity;


    public ItemAdapter(Context context, List<Item> itemList, Activity activity) {
        this.context = context;
        this.itemList = itemList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.main_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.txtName.setText(item.getName());
        holder.txtMail.setText(item.getMail());
        holder.txtNumber.setText(item.getNumber());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private Item checkStudentById(String id){
        for (Item s: itemList) {
            if (s.getId() == Integer.parseInt(id)){
                return s;
            }
        }
        return null;
    }

    private void deleteStudent(String id){
        OkHttpClient client = new OkHttpClient();
        String createStudentURL = "http://10.0.2.2/api/delete-student.php";
        RequestBody formBody = new FormBody.Builder()
                .add("id", id)
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
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            itemList.remove(checkStudentById(id));
                            Toast.makeText(context, "Delete Successfully", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        }
                    });
                } catch (JSONException e) {
                    Log.d("onResponse", e.getMessage());
                }
            }
        });
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        GestureDetector gestureDetector;
        TextView txtName, txtMail, txtNumber;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtMail = itemView.findViewById(R.id.txtMail);
            txtNumber = itemView.findViewById(R.id.txtNumber);

            gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    // Khi double click, mở màn hình UpdatePage để chỉnh sửa sinh viên
                    Item student = itemList.get(getAdapterPosition());
                    int id = student.getId();
                    String name = student.getName();
                    String email = student.getMail();
                    String phone = student.getNumber();

                    Intent intent = new Intent(context, UpdatePage.class);
                    intent.putExtra("edit_id", id);
                    intent.putExtra("edit_name", name);
                    intent.putExtra("edit_mail", email);
                    intent.putExtra("edit_phone", phone);
                    activity.startActivityForResult(intent, 2);
                    return true;
                }
            });

            // Xử lý sự kiện touch để lắng nghe sự kiện double click
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    Item student = itemList.get(getAdapterPosition());
                    popupMenu.getMenu().add(student.getName()).setEnabled(false);
                    popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(item -> {
                        int item_id = item.getItemId();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        if (item_id == R.id.menu_delete) {
                            String studentName = student.getName();
                            int id = itemList.get(getAdapterPosition()).getId();

                            builder.setTitle("Delete " + studentName);
                            builder.setMessage("Do you want to delete this student ?");
                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                deleteStudent(String.valueOf(id));
                                itemList.remove(getAdapterPosition());
                                notifyItemRemoved(getAdapterPosition());
                            });

                            builder.setNegativeButton("No", (dialog, which) -> {
                                Toast.makeText(context, "No change", Toast.LENGTH_SHORT).show();
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return true;
                        } else if (item_id == R.id.menu_edit) {
                            int id = student.getId();
                            String name = student.getName();
                            String email = student.getMail();
                            String phone = student.getNumber();

                            Intent intent = new Intent(context, UpdatePage.class);
                            intent.putExtra("edit_id", id);
                            intent.putExtra("edit_name", name);
                            intent.putExtra("edit_mail", email);
                            intent.putExtra("edit_phone", phone);
                            activity.startActivityForResult(intent, 2);

                            return true;
                        }
                        return false;
                    });
                    popupMenu.show();
                    return true;
                }
            });

        }
    }
}
