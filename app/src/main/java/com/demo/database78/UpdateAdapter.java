package com.demo.database78;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class UpdateAdapter extends ArrayAdapter<UpdateItem> {

    public UpdateAdapter(Context context, List<UpdateItem> updates) {
        super(context, 0, updates);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UpdateItem update = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_update, parent, false);
        }

        TextView tvMessage = convertView.findViewById(R.id.tv_message);
        TextView tvTimestamp = convertView.findViewById(R.id.tv_timestamp);

        tvMessage.setText(update.getMessage());
        tvTimestamp.setText(update.getTimestamp());

        return convertView;
    }
}
