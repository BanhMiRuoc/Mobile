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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;
    private List<Boolean> selectedItems = new ArrayList<>();

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList != null ? userList : new ArrayList<>();

        // Khởi tạo danh sách `selectedItems` với giá trị `false` cho mỗi phần tử trong `userList`
        for (int i = 0; i < this.userList.size(); i++) {
            selectedItems.add(false);
        }

        // Quan sát thay đổi trong danh sách để cập nhật `selectedItems` cho phù hợp
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateSelectedItems();
            }
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUserName.setText(user.getName());
        holder.tvRole.setText(user.getRole());
        holder.tvStatus.setText(user.getStatus());

        if (user.getProfileImage() != null) {
            holder.imgAvt.setImageBitmap(user.getProfileImage());
        } else {
            holder.imgAvt.setImageResource(R.drawable.avatar_error); // Đặt hình ảnh mặc định nếu không có ảnh
        }

        // Tránh gán lại `OnCheckedChangeListener` trong khi cập nhật checkbox
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedItems.get(position));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectedItems.set(position, isChecked);
        });

        holder.tvSeeMore.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", user.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    // Hàm cập nhật tất cả checkbox khi "Select All" được chọn
    public void selectAllItems(boolean isChecked) {
        for (int i = 0; i < selectedItems.size(); i++) {
            selectedItems.set(i, isChecked);
        }
        notifyDataSetChanged();
    }

    public void removeSelectedItems(FirebaseFirestore db) {
        // Lặp ngược từ cuối lên đầu để tránh thay đổi danh sách trong quá trình xóa
        for (int i = selectedItems.size() - 1; i >= 0; i--) {
            if (selectedItems.get(i)) {
                final int index = i;  // Biến final cho chỉ số i
                String userId = userList.get(index).getUserId();

                // Xóa người dùng khỏi Firestore trong collection "users"
                db.collection("users").document(userId)
                        .delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Sau khi xóa trong "users", tiếp tục xóa tài khoản trong "accounts"
                                db.collection("accounts").document(userId)
                                        .delete()
                                        .addOnCompleteListener(accountTask -> {
                                            if (accountTask.isSuccessful()) {
                                                // Nếu xóa thành công trong cả hai collection
                                                userList.remove(index);
                                                selectedItems.remove(index);
                                                notifyItemRemoved(index);
                                            } else {
                                                // Nếu có lỗi khi xóa tài khoản trong "accounts"
                                                Toast.makeText(context, "Lỗi khi xóa tài khoản người dùng trong Firestore", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Nếu có lỗi khi xóa người dùng trong "users"
                                Toast.makeText(context, "Lỗi khi xóa người dùng từ Firestore", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
        notifyDataSetChanged(); // Cập nhật adapter sau khi xóa
    }


    // Cập nhật lại `selectedItems` nếu `userList` thay đổi
    private void updateSelectedItems() {
        if (selectedItems.size() < userList.size()) {
            while (selectedItems.size() < userList.size()) {
                selectedItems.add(false);
            }
        } else if (selectedItems.size() > userList.size()) {
            while (selectedItems.size() > userList.size()) {
                selectedItems.remove(selectedItems.size() - 1);
            }
        }
    }

    public int getSelectedItemsCount() {
        int count = 0;
        for (boolean isSelected : selectedItems) {
            if (isSelected) {
                count++;
            }
        }
        return count;
    }


    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvt;
        TextView tvUserName, tvRole, tvStatus, tvSeeMore;
        CheckBox checkBox;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvt = itemView.findViewById(R.id.imgAvt);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSeeMore = itemView.findViewById(R.id.tvSeeMore);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
