package com.demo.database78;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;

public class RecordsFragment extends Fragment {

    private ListView lst1;
    private ArrayList<Student> students;
    private DatabaseHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);

        lst1 = view.findViewById(R.id.lst1);
        dbHelper = new DatabaseHelper(getActivity());

        loadRecords();
        return view;
    }

    @SuppressLint("Range")
    private void loadRecords() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM records", null);
        students = new ArrayList<>();
        final ArrayList<ArrayList<SubRecord>> allSubRecords = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                Student stu = new Student();
                stu.id = c.getString(c.getColumnIndex("id"));
                stu.name = c.getString(c.getColumnIndex("name"));
                stu.course = c.getString(c.getColumnIndex("course"));
                stu.fee = c.getString(c.getColumnIndex("fee"));

                Cursor subCursor = db.rawQuery("SELECT * FROM sub_records WHERE parent_id = ?", new String[]{stu.id});
                ArrayList<SubRecord> subRecords = new ArrayList<>();

                if (subCursor.moveToFirst()) {
                    do {
                        SubRecord subRecord = new SubRecord();
                        subRecord.id = subCursor.getInt(subCursor.getColumnIndex("id"));
                        subRecord.material = subCursor.getString(subCursor.getColumnIndex("material"));
                        subRecord.quantity = subCursor.getInt(subCursor.getColumnIndex("quantity"));
                        subRecord.unit = subCursor.getString(subCursor.getColumnIndex("unit"));
                        subRecords.add(subRecord);
                    } while (subCursor.moveToNext());
                }
                subCursor.close();

                students.add(stu);
                allSubRecords.add(subRecords);
            } while (c.moveToNext());
        }
        c.close();

        StudentAdapter studentAdapter = new StudentAdapter(getActivity(), students, allSubRecords);
        lst1.setAdapter(studentAdapter);

        lst1.setOnItemClickListener((parent, view, position, id) -> {
            Student stu = students.get(position);
            ArrayList<SubRecord> subRecords = allSubRecords.get(position);

            Intent i = new Intent(getActivity(), edit.class);
            i.putExtra("id", stu.id);
            i.putExtra("name", stu.name);
            i.putExtra("course", stu.course);
            i.putExtra("fee", stu.fee);
            i.putExtra("subRecords", subRecords);
            startActivity(i);
        });
    }
}
