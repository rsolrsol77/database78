package com.demo.database78;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class StudentAdapter extends ArrayAdapter<Student> {
    private final Context context;
    private final ArrayList<Student> students;
    private final ArrayList<ArrayList<SubRecord>> allSubRecords;

    public StudentAdapter(Context context, ArrayList<Student> students, ArrayList<ArrayList<SubRecord>> allSubRecords) {
        super(context, R.layout.record_item, students);
        this.context = context;
        this.students = students;
        this.allSubRecords = allSubRecords;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.record_item, parent, false);
        }

        // الحصول على البيانات الخاصة بالسجل الرئيسي
        Student student = students.get(position);
        ArrayList<SubRecord> subRecords = allSubRecords.get(position);

        // ربط الحقول في تصميم العنصر ببيانات السجل
        TextView tvId = convertView.findViewById(R.id.tv_id);
        TextView tvName = convertView.findViewById(R.id.tv_name);
        TextView tvFee = convertView.findViewById(R.id.tv_fee);


        // تعيين البيانات الخاصة بالسجل الرئيسي
        tvId.setText("" + student.id);
        tvName.setText("" + student.name);
        tvFee.setText("" + student.fee);




        // إضافة النقر على زر التعديل
        TextView tvEdit = convertView.findViewById(R.id.tv_edit_record);
        tvEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, edit.class);
            intent.putExtra("id", student.id);
            intent.putExtra("name", student.name);
            intent.putExtra("course", student.course);
            intent.putExtra("fee", student.fee);
            intent.putExtra("subRecords", subRecords);
            context.startActivity(intent);
        });






        // داخل دالة getView()
        TextView tvShowDetails = convertView.findViewById(R.id.tv_show_details);
        tvShowDetails.setOnClickListener(v -> {
            showDetailsPopup(student, subRecords, context);
        });




        return convertView;
    }



    // دالة جديدة لعرض التفاصيل
    private void showDetailsPopup(Student student, ArrayList<SubRecord> subRecords, Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_warehouse_details, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // ربط العناصر الجديدة
        TextView tvId = popupView.findViewById(R.id.tv_id);
        TextView tvName = popupView.findViewById(R.id.tv_name);
        TextView tvCourse = popupView.findViewById(R.id.tv_course);
        TextView tvFee = popupView.findViewById(R.id.tv_fee);
        LinearLayout subContainer = popupView.findViewById(R.id.sub_record_container);

        tvId.setText(String.valueOf(student.id));
        tvName.setText(student.name);
        tvCourse.setText(student.course);
        tvFee.setText(String.valueOf(student.fee));

        // إضافة العناصر الفرعية
        subContainer.removeAllViews();
        for (SubRecord sub : subRecords) {
            View subItemView = inflater.inflate(R.layout.sub_record_item3, subContainer, false);
            TextView material = subItemView.findViewById(R.id.tv_material);
            TextView quantity = subItemView.findViewById(R.id.tv_quantity);

            material.setText(sub.material);
            quantity.setText(sub.quantity + " " + sub.unit);
            subContainer.addView(subItemView);
        }

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnClose = popupView.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> popupWindow.dismiss());

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }




}
