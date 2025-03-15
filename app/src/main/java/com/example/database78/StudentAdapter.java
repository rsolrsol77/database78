package com.example.database78;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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
        TextView tvCourse = convertView.findViewById(R.id.tv_course);
        TextView tvFee = convertView.findViewById(R.id.tv_fee);
        LinearLayout subRecordContainer = convertView.findViewById(R.id.sub_record_container);

        // تعيين البيانات الخاصة بالسجل الرئيسي
        tvId.setText("" + student.id);
        tvName.setText("" + student.name);
        tvCourse.setText("" + student.course);
        tvFee.setText("" + student.fee);

        // إعداد وتصميم السجلات الفرعية
        subRecordContainer.removeAllViews(); // تنظيف الحاوية قبل الإضافة
        LayoutInflater inflater = LayoutInflater.from(context);
        for (SubRecord subRecord : subRecords) {
            View subView = inflater.inflate(R.layout.sub_record_item2, subRecordContainer, false);

            TextView tvSubId = subView.findViewById(R.id.tv_sub_id);
            TextView tvMaterial = subView.findViewById(R.id.tv_material);
            TextView tvQuantity = subView.findViewById(R.id.tv_quantity);

            tvSubId.setText(String.valueOf(subRecord.id));
            tvMaterial.setText(subRecord.material);
            tvQuantity.setText(subRecord.quantity + " " + subRecord.unit);

            subRecordContainer.addView(subView);
        }

        return convertView;
    }
}
