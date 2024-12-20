package com.example.milita;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {
    private Context context;
    private Activity activity;
    private List<Certificate> certificateList;
    private String roleCurrentUser;
    private String studentId, currentUserEmail;
    private List<Boolean> selectedItems = new ArrayList<>();

    public CertificateAdapter(Activity activity, Context context, List<Certificate> certificateList, String roleCurrentUser, String studentId, String currentUserEmail) {
        this.activity = activity;
        this.context = context;
        this.roleCurrentUser = roleCurrentUser;
        this.currentUserEmail = currentUserEmail;
        this.studentId = studentId;
        this.certificateList = certificateList != null ? certificateList : new ArrayList<>();

        // Khởi tạo danh sách `selectedItems` với giá trị `false` cho mỗi phần tử trong `certificateList`
        for (int i = 0; i < this.certificateList.size(); i++) {
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
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.certificate_item, parent, false);
        return new CertificateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        Certificate certificate = certificateList.get(position);
        holder.tvUserName.setText(certificate.getName());
        holder.tvSession.setText(certificate.getSession());
        holder.tvDateCreated.setText(certificate.getDateCreated());

        if (certificate.getProfileImage() != null) {
            holder.imgAvt.setImageBitmap(certificate.getProfileImage());
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
            Intent intent = new Intent(context, CertificateInfomationActivity.class);
            intent.putExtra("id", certificate.getId());
            intent.putExtra("role", roleCurrentUser);
            intent.putExtra("studentId", studentId);
            intent.putExtra("currentUserEmail", currentUserEmail);
            activity.startActivityForResult(intent, 20);
        });
    }

    @Override
    public int getItemCount() {
        return certificateList != null ? certificateList.size() : 0;
    }

    // Hàm cập nhật tất cả checkbox khi "Select All" được chọn
    public void selectAllItems(boolean isChecked) {
        for (int i = 0; i < selectedItems.size(); i++) {
            selectedItems.set(i, isChecked);
        }
        notifyDataSetChanged();
    }

    public void removeSelectedItems(FirebaseFirestore db, String studentId) {
        List<Integer> itemsToRemove = new ArrayList<>();
        List<String> idsToRemove = new ArrayList<>();

        // Vòng for đầu tiên: Thu thập các chỉ số và cerId của các mục cần xóa
        for (int i = selectedItems.size() - 1; i >= 0; i--) {
            if (selectedItems.get(i)) {
                itemsToRemove.add(i);
                idsToRemove.add(certificateList.get(i).getId());  // Lấy cerId của chứng chỉ muốn xóa
            }
        }

        // Vòng for thứ hai: Xóa các mục khỏi Firestore
        for (String cerId : idsToRemove) {
            // Truy vấn tài liệu sinh viên theo studentId
            db.collection("students")
                    .whereEqualTo("id", studentId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Lấy tài liệu sinh viên đầu tiên tìm được
                            String studentDocId = task.getResult().getDocuments().get(0).getId();

                            // Truy vấn subcollection "certificates" của sinh viên để xóa tài liệu với cerId
                            db.collection("students")
                                    .document(studentDocId)
                                    .collection("certificates")
                                    .document(cerId)
                                    .delete()
                                    .addOnCompleteListener(deleteTask -> {
                                        if (!deleteTask.isSuccessful()) {
                                            Toast.makeText(context, "Lỗi khi xóa chứng chỉ với cerId: " + cerId, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(context, "Không tìm thấy sinh viên với studentId: " + studentId, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Vòng for thứ ba: Xóa các mục khỏi certificateList và selectedItems cục bộ
        for (int index : itemsToRemove) {
            certificateList.remove(index);
            selectedItems.remove(index);
            notifyItemRemoved(index);
        }

        // Cập nhật RecyclerView sau khi xóa
        notifyDataSetChanged();
    }


    public void removeAllItems() {
        certificateList.clear();
        selectedItems.clear();
        notifyDataSetChanged();
    }

    // Cập nhật lại `selectedItems` nếu `certificateList` thay đổi
    private void updateSelectedItems() {
        if (selectedItems.size() < certificateList.size()) {
            while (selectedItems.size() < certificateList.size()) {
                selectedItems.add(false);
            }
        } else if (selectedItems.size() > certificateList.size()) {
            while (selectedItems.size() > certificateList.size()) {
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

    public static class CertificateViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvt;
        TextView tvUserName, tvSession, tvDateCreated;
        Button btnSeeMore;
        CheckBox checkBox;

        public CertificateViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvt = itemView.findViewById(R.id.imgAvt);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvSession = itemView.findViewById(R.id.tvSession);
            tvDateCreated = itemView.findViewById(R.id.tvDateCreated);
            btnSeeMore = itemView.findViewById(R.id.btnSeeMore);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
