package com.demo.database78;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class TotalAccountAdapter extends ArrayAdapter<TotalAccountItem> {

    public TotalAccountAdapter(Context context, List<TotalAccountItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        TotalAccountItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_total_account, parent, false);
            holder = new ViewHolder();
            holder.tvAmount = convertView.findViewById(R.id.tv_amount);
            holder.tvCurrency = convertView.findViewById(R.id.tv_currency);
            holder.tvTimestamp = convertView.findViewById(R.id.tv_timestamp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvAmount.setText(item.getTotalAmount());
        holder.tvCurrency.setText(item.getCurrency());
        holder.tvTimestamp.setText(item.getTimestamp());

        return convertView;
    }

    private static class ViewHolder {
        TextView tvAmount;
        TextView tvCurrency;
        TextView tvTimestamp;
    }
}