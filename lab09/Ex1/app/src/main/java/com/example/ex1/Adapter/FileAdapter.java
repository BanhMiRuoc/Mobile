package com.example.ex1.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ex1.Model.File;
import com.example.ex1.R;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {
    Context context;
    List<File> fileList;

    public FileAdapter(Context context, List<File> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.main_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        File file = fileList.get(position);

        holder.txtName.setText(file.getName());
        holder.txtCapacity.setText(file.getCapacity() + "MB");
        holder.imgView.setImageResource(file.getImageResourceId());

        int status = file.getStatus();
        if(status == 1){
            holder.txtStatus.setVisibility(View.INVISIBLE);
            holder.progressBar.setVisibility(View.VISIBLE);

            holder.progressBar.setProgress(file.getProgress());
        } else if (status == 2) {
            holder.txtStatus.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.INVISIBLE);

            holder.txtStatus.setText("Fail");
            holder.txtStatus.setTextColor(Color.YELLOW);
        }else {
            holder.txtStatus.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.INVISIBLE);

            holder.txtStatus.setText("Completed");
            holder.txtStatus.setTextColor(Color.GREEN);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imgView;
        TextView txtName, txtCapacity, txtStatus;
        ProgressBar progressBar;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgView = itemView.findViewById(R.id.imgViewItem);
            txtName = itemView.findViewById(R.id.txtName);
            txtCapacity = itemView.findViewById(R.id.txtCapacity);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
