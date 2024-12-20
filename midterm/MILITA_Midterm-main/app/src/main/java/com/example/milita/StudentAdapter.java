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

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private Context context;
    private List<Student> studentList;
    private String roleCurrentUser;
    private String currentUserEmail;
    private List<Boolean> selectedItems = new ArrayList<>();

    public StudentAdapter(Context context, List<Student> studentList, String roleCurrentUser, String currentUserEmail) {
        this.context = context;
        this.roleCurrentUser = roleCurrentUser;
        this.currentUserEmail = currentUserEmail;
        this.studentList = studentList != null ? studentList : new ArrayList<>();

        // Khởi tạo danh sách `selectedItems` với giá trị `false` cho mỗi phần tử trong `studentList`
        for (int i = 0; i < this.studentList.size(); i++) {
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
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.tvUserName.setText(student.getName());
        holder.tvId.setText(student.getId());
        holder.tvClass.setText(student.getStudentClass());

        if (student.getProfileImage() != null) {
            holder.imgAvt.setImageBitmap(student.getProfileImage());
        } else {
            holder.imgAvt.setImageResource(R.drawable.avatar_error); // Đặt hình ảnh mặc định nếu không có ảnh
        }

        // Tránh gán lại `OnCheckedChangeListener` trong khi cập nhật checkbox
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedItems.get(position));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectedItems.set(position, isChecked);
        });

        holder.btnSeeMore.setOnClickListener(v -> {
            Intent intent = new Intent(context, StudentProfileActivity.class);
            intent.putExtra("id", student.getId());
            intent.putExtra("role", roleCurrentUser);
            intent.putExtra("currentUserEmail", currentUserEmail);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }

    // Hàm cập nhật tất cả checkbox khi "Select All" được chọn
    public void selectAllItems(boolean isChecked) {
        for (int i = 0; i < selectedItems.size(); i++) {
            selectedItems.set(i, isChecked);
        }
        notifyDataSetChanged();
    }

    public void removeSelectedItems(FirebaseFirestore db) {
        List<Integer> itemsToRemove = new ArrayList<>();
        List<String> idsToRemove = new ArrayList<>();

        // Vòng lặp đầu tiên: Thu thập danh sách các chỉ số và ID của các mục cần xóa
        for (int i = selectedItems.size() - 1; i >= 0; i--) {
            if (selectedItems.get(i)) {
                itemsToRemove.add(i);
                idsToRemove.add(studentList.get(i).getId());
            }
        }

        // Vòng lặp thứ hai: Xóa các mục khỏi Firestore trước
        for (String id : idsToRemove) {
            db.collection("students")
                    .whereEqualTo("id", id)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String docId = task.getResult().getDocuments().get(0).getId();

                            db.collection("students").document(docId)
                                    .delete()
                                    .addOnCompleteListener(deleteTask -> {
                                        if (!deleteTask.isSuccessful()) {
                                            Toast.makeText(context, "Lỗi khi xóa người dùng từ Firestore", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(context, "Không tìm thấy người dùng với id: " + id, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Vòng lặp thứ ba: Xóa cục bộ trong `studentList` và `selectedItems`
        for (int index : itemsToRemove) {
            studentList.remove(index);
            selectedItems.remove(index);
            notifyItemRemoved(index);
        }

        // Cập nhật toàn bộ RecyclerView sau khi xóa
        notifyDataSetChanged();
    }

    public void removeAllItems() {
        studentList.clear();
        selectedItems.clear();
        notifyDataSetChanged();
    }

    // Cập nhật lại `selectedItems` nếu `studentList` thay đổi
    private void updateSelectedItems() {
        // Điều chỉnh lại kích thước của `selectedItems` nếu cần
        if (selectedItems.size() < studentList.size()) {
            while (selectedItems.size() < studentList.size()) {
                selectedItems.add(false); // Thêm phần tử `false` vào để khớp với kích thước của studentList
            }
        } else if (selectedItems.size() > studentList.size()) {
            while (selectedItems.size() > studentList.size()) {
                selectedItems.remove(selectedItems.size() - 1); // Xóa phần tử dư thừa
            }
        }
    }


    public void updateList(List<Student> filteredList) {
        this.studentList = filteredList;
        notifyDataSetChanged();
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

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvt;
        TextView tvUserName, tvId, tvClass, btnSeeMore;
        CheckBox checkBox;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvt = itemView.findViewById(R.id.imgAvt);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvId = itemView.findViewById(R.id.tvId);
            tvClass = itemView.findViewById(R.id.tvClass);
            btnSeeMore = itemView.findViewById(R.id.btnSeeMore);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
