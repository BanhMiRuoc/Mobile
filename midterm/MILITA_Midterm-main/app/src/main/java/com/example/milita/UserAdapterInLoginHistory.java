package com.example.milita;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserAdapterInLoginHistory extends RecyclerView.Adapter<UserAdapterInLoginHistory.UserAdapterInLoginHistoryViewHolder> {
    private Context context;
    private List<LoginHistory> loginList;

    public UserAdapterInLoginHistory(Context context, List<LoginHistory> loginList) {
        this.context = context;
        this.loginList = loginList != null ? loginList : new ArrayList<>();

    }

    @NonNull
    @Override
    public UserAdapterInLoginHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_in_login_history, parent, false);
        return new UserAdapterInLoginHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapterInLoginHistoryViewHolder holder, int position) {
        LoginHistory loginHistory = loginList.get(position);
        holder.tvAccount.setText(loginHistory.getAccount());
        holder.tvName.setText(loginHistory.getName());
        holder.tvTime.setText(loginHistory.getTime());

        if (loginHistory.getProfileImage() != null) {
            holder.imgAvt.setImageBitmap(loginHistory.getProfileImage());
        } else {
            holder.imgAvt.setImageResource(R.drawable.avatar_error);
        }

    }

    @Override
    public int getItemCount() {
        return loginList != null ? loginList.size() : 0;
    }


    public static class UserAdapterInLoginHistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvt;
        TextView tvAccount, tvName, tvTime;

        public UserAdapterInLoginHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvt = itemView.findViewById(R.id.imgAvt);
            tvAccount = itemView.findViewById(R.id.tvAccount);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
