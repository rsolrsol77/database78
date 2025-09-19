package com.demo.database78;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UpdatesFragment extends Fragment {

    DatabaseHelper dbHelper;
    ListView updatesListView;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_updates, container, false);

        dbHelper = new DatabaseHelper(getActivity());
        updatesListView = view.findViewById(R.id.updates_list_view);

        List<UpdateItem> updates = getAllUpdates();
        UpdateAdapter adapter = new UpdateAdapter(getActivity(), updates);
        updatesListView.setAdapter(adapter);

        return view;
    }

    public List<UpdateItem> getAllUpdates() {
        List<UpdateItem> updates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT message, timestamp FROM updates ORDER BY timestamp DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String message = cursor.getString(0);
                String timestamp = formatTimestamp(cursor.getString(1));
                updates.add(new UpdateItem(message, timestamp));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return updates;
    }

    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "Unknown Date";
        }

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dbFormat.setTimeZone(TimeZone.getTimeZone("UTC"));



        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault());
        displayFormat.setTimeZone(TimeZone.getDefault());


        try {
            Date date = dbFormat.parse(timestamp);
            return date != null ? displayFormat.format(date) : timestamp;
        } catch (ParseException e) {
            e.printStackTrace();
            return timestamp;
        }
    }

    // فئة الـ Adapter المخصصة مع ViewHolder
    private static class UpdateAdapter extends ArrayAdapter<UpdateItem> {

        public UpdateAdapter(Context context, List<UpdateItem> updates) {
            super(context, 0, updates);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            UpdateItem update = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_item_update, parent, false);
                holder = new ViewHolder();
                holder.tvMessage = convertView.findViewById(R.id.tv_message);
                holder.tvTimestamp = convertView.findViewById(R.id.tv_timestamp);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvMessage.setText(update.getMessage());
            holder.tvTimestamp.setText(update.getTimestamp());

            return convertView;
        }

        private static class ViewHolder {
            TextView tvMessage;
            TextView tvTimestamp;
        }
    }
}