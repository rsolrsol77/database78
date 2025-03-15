package com.example.database78;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class SalesAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<HashMap<String, Object>> salesList;

    public SalesAdapter(Context context, ArrayList<HashMap<String, Object>> salesList) {
        this.context = context;
        this.salesList = salesList;
    }

    @Override
    public int getCount() {
        return salesList.size();
    }

    @Override
    public Object getItem(int position) {
        return salesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.sales_list_item, parent, false);
        }

        HashMap<String, Object> sale = salesList.get(position);

        TextView tvId = convertView.findViewById(R.id.Id);
        TextView tvBuyerName = convertView.findViewById(R.id.buyer_name);
        TextView tvPhone = convertView.findViewById(R.id.Phone);
        TextView tvAddress = convertView.findViewById(R.id.Address);
        TextView tvDate = convertView.findViewById(R.id.Date);
        TextView tvTotalAmount = convertView.findViewById(R.id.TotalAmount);
        TextView tvCurrency = convertView.findViewById(R.id.Currency);
        TextView tvOrderInfo = convertView.findViewById(R.id.order_info);
        TextView tvOrderStatus = convertView.findViewById(R.id.order_status);
        LinearLayout itemsContainer = convertView.findViewById(R.id.items_container);

        // تعيين القيم إلى حقول الـ TextView
        tvId.setText("" + sale.get("id"));
        tvBuyerName.setText("" + sale.get("buyer_name"));
        tvPhone.setText("" + sale.get("phone"));
        tvAddress.setText("" + sale.get("address"));
        tvDate.setText("" + sale.get("date"));
        tvTotalAmount.setText("" + sale.get("total_amount"));
        tvCurrency.setText("" + sale.get("currency"));
        tvOrderInfo.setText("" + sale.get("order_info"));
        tvOrderStatus.setText("" + sale.get("order_status"));

        // عرض المواد
        itemsContainer.removeAllViews();
        ArrayList<HashMap<String, String>> items = (ArrayList<HashMap<String, String>>) sale.get("items");
        for (HashMap<String, String> item : items) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.sub_record_item3, itemsContainer, false);

            TextView tvItemId = itemView.findViewById(R.id.tv_item_id); // عرض id للعنصر
            TextView tvMaterial = itemView.findViewById(R.id.tv_material);
            TextView tvQuantity = itemView.findViewById(R.id.tv_quantity);


            tvItemId.setText("" + item.get("id"));
            tvMaterial.setText("" + item.get("material"));
            tvQuantity.setText("" + item.get("quantity") + " " + item.get("unit"));

            itemsContainer.addView(itemView);
        }

        return convertView;
    }
}
