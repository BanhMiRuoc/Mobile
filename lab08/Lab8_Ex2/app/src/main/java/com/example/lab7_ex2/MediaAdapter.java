package com.example.lab7_ex2;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private final List<MediaItem> mediaItems;
    private final List<MediaItem> selectedItems = new ArrayList<>();
    private final OnItemSelectedListener listener;

    public MediaAdapter(List<MediaItem> mediaItems, OnItemSelectedListener listener) {
        this.mediaItems = mediaItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = mediaItems.get(position);
        holder.tvFileName.setText(item.getName());
        holder.checkBoxSelect.setChecked(selectedItems.contains(item));

        // Kiểm tra nếu là hình ảnh (MEDIA_TYPE_IMAGE)
        if (item.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            // Chuyển ID hình ảnh thành URI
            String imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + item.getId();

            // Dùng Glide để tải hình ảnh vào ImageView
            Glide.with(holder.itemView.getContext())
                    .load(imageUri) // Đường dẫn đến hình ảnh
                    .centerCrop() // Cắt ảnh để vừa khung hình
                    .into(holder.imageThumbnail);
        } else {
            // Nếu là video, bạn có thể thay hình ảnh mặc định hoặc xử lý khác
            holder.imageThumbnail.setImageResource(R.drawable.ic_launcher_foreground); // Ví dụ hình ảnh video mặc định
        }

        holder.checkBoxSelect.setOnClickListener(v -> toggleSelection(item));
    }


    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    private void toggleSelection(MediaItem item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
        notifyItemChanged(mediaItems.indexOf(item));
        listener.onItemSelected(!selectedItems.isEmpty());
    }

    public List<MediaItem> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public interface OnItemSelectedListener {
        void onItemSelected(boolean isSelected);
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageThumbnail;
        TextView tvFileName;
        CheckBox checkBoxSelect;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            checkBoxSelect = itemView.findViewById(R.id.checkBoxSelect);
        }
    }
}

