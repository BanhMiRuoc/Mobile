package com.example.ex2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
public class EventAdapter extends ArrayAdapter<Event> {
    private Context context;
    private List<Event> eventList;

    public EventAdapter(Context context, List<Event> eventList) {
        super(context, R.layout.item, eventList);
        this.context = context;
        this.eventList = eventList;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item, parent, false);
        }

        TextView txtEvent = rowView.findViewById(R.id.txtEvent);
        TextView txtPlace = rowView.findViewById(R.id.txtPlace);
        TextView txtDateTime = rowView.findViewById(R.id.txtDateTime);
        Switch swCheck = rowView.findViewById(R.id.swCheck);

        Event event = eventList.get(position);
        txtEvent.setText(event.getName());
        txtPlace.setText(event.getPlace());
        txtDateTime.setText(event.getDate() + " " + event.getTime());

        swCheck.setOnCheckedChangeListener(null); // Xóa listener cũ
        swCheck.setChecked(event.isEnabled()); // Cập nhật trạng thái Switch từ event
        swCheck.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            Event current = eventList.get(position);
            current.setEnabled(isChecked);
        });

        return rowView;
    }



}
