package com.example.lab05_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<Item> {
    private Context context;
    private List<Item> itemList;

    public CustomAdapter(Context context, List<Item> items) {
        super(context, 0, items);
        this.context = context;
        this.itemList = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        }

        Item currentItem = getItem(position);

        TextView text = convertView.findViewById(R.id.text);
        ImageView icon = convertView.findViewById(R.id.icon);
        CheckBox checkBox = convertView.findViewById(R.id.checkbox);

        text.setText(currentItem.getName());
        icon.setImageResource(currentItem.getIcon());
        checkBox.setChecked(currentItem.isChecked());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentItem.setChecked(isChecked);
        });

        return convertView;
    }
}
