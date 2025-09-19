package com.demo.database78;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDarkn));
        }

        // إنشاء Handler باستخدام الـ Main Looper لضمان تنفيذ التأخير بشكل دقيق
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);

            // تأثير انتقالي سلس
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            finish();
        }, 100); // تعيين التأخير إلى 2000 مللي ثانية = 2 ثوانٍ
    }
}
