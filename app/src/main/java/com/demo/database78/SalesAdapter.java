package com.demo.database78;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
        TextView tvOrderInfo = convertView.findViewById(R.id.order_info);



        // تعيين القيم إلى حقول الـ TextView
        tvId.setText("" + sale.get("id"));
        tvBuyerName.setText("" + sale.get("buyer_name"));
        tvOrderInfo.setText("" + sale.get("order_info"));



        ArrayList<HashMap<String, String>> items = (ArrayList<HashMap<String, String>>) sale.get("items");





        // إضافة النقر على زر التعديل
        TextView tvEdit = convertView.findViewById(R.id.tv_edit_sale);
        tvEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, SalesEditActivity.class);
            intent.putExtra("id", sale.get("id").toString());
            intent.putExtra("buyer_name", sale.get("buyer_name").toString());
            intent.putExtra("phone", sale.get("phone").toString());
            intent.putExtra("address", sale.get("address").toString());
            intent.putExtra("date", sale.get("date").toString());
            intent.putExtra("order_info", sale.get("order_info").toString());
            intent.putExtra("order_status", sale.get("order_status").toString());
            intent.putExtra("total_amount", sale.get("total_amount").toString());
            intent.putExtra("currency", sale.get("currency").toString());
            intent.putExtra("items", items);
            context.startActivity(intent);
        });








        // داخل دالة getView()
        TextView tvShowDetails = convertView.findViewById(R.id.tv_show_details);
        tvShowDetails.setOnClickListener(v -> {
            showDetailsPopup(sale, context);
        });




        return convertView;
    }




    // دالة جديدة لعرض التفاصيل
    private void showDetailsPopup(HashMap<String, Object> sale, Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_sales_details, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // ربط جميع العناصر حسب التنسيق الجديد
        TextView tvId = popupView.findViewById(R.id.Id);
        TextView tvBuyer = popupView.findViewById(R.id.buyer_name);
        TextView tvPhone = popupView.findViewById(R.id.Phone);
        TextView tvAddress = popupView.findViewById(R.id.Address);
        TextView tvDate = popupView.findViewById(R.id.Date);
        TextView tvOrderInfo = popupView.findViewById(R.id.order_info);
        TextView tvOrderStatus = popupView.findViewById(R.id.order_status);
        TextView tvTotalAmount = popupView.findViewById(R.id.TotalAmount);
        TextView tvCurrency = popupView.findViewById(R.id.Currency);
        LinearLayout itemsContainer = popupView.findViewById(R.id.items_container);

        // تعيين كافة القيم من الـ HashMap
        tvId.setText(safeGetString(sale, "id"));
        tvBuyer.setText(safeGetString(sale, "buyer_name"));
        tvPhone.setText(safeGetString(sale, "phone"));
        tvAddress.setText(safeGetString(sale, "address"));
        tvDate.setText(safeGetString(sale, "date"));
        tvOrderInfo.setText(safeGetString(sale, "order_info"));
        tvOrderStatus.setText(safeGetString(sale, "order_status"));
        tvTotalAmount.setText(safeGetString(sale, "total_amount"));
        tvCurrency.setText(safeGetString(sale, "currency"));

        // إضافة العناصر الديناميكية
        itemsContainer.removeAllViews();
        ArrayList<HashMap<String, String>> items = (ArrayList<HashMap<String, String>>) sale.get("items");
        if (items != null) {
            for (HashMap<String, String> item : items) {
                View itemView = inflater.inflate(R.layout.sub_record_item3, itemsContainer, false);
                TextView material = itemView.findViewById(R.id.tv_material);
                TextView quantity = itemView.findViewById(R.id.tv_quantity);


                material.setText(safeGetString(item, "material"));
                quantity.setText(item.get("quantity") + " " + item.get("unit"));

                itemsContainer.addView(itemView);
            }
        }

        // إغلاق النافذة
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnClose = popupView.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> popupWindow.dismiss());

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    // دالة مساعدة لتجنب NullPointerException
    private String safeGetString(HashMap<String, ?> map, String key) {
        Object value = map.get(key);
        return (value != null) ? value.toString() : "N/A";
    }




}
