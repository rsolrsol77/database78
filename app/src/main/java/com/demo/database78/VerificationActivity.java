package com.demo.database78;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends AppCompatActivity {

    private TextInputEditText etVerificationCode;
    private Button btnVerify, btnResendCode;
    private TextView tvTimer;
    private FirebaseAuth mAuth;
    private CountDownTimer countDownTimer;
    private final long RESEND_DELAY = 50000; // 50 ثانية

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);


        // Set up toolbar with back (logout) button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // or set a title if desired
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        }



        etVerificationCode = findViewById(R.id.etVerificationCode);
        btnVerify = findViewById(R.id.btnVerify);
        btnResendCode = findViewById(R.id.btnResendCode);
        tvTimer = findViewById(R.id.tvTimer);
        mAuth = FirebaseAuth.getInstance();

        setupResendButton();
        startResendTimer();

        btnVerify.setOnClickListener(v -> {
            String code = etVerificationCode.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                etVerificationCode.setError("أدخل الرمز المرسل");
            } else {
                checkEmailVerification();
            }
        });

        // تحقق مباشرة عند فتح الشاشة لأول مرة
        checkEmailVerification();
    }




    @Override
    public void onBackPressed() {
        // بدلاً من العودة إلى MainActivity غير المُفعّل
        super.onBackPressed();
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Logout and return to LoginActivity
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onResume() {
        super.onResume();
        // تحقق عند عودة التطبيق إلى الواجهة
        checkEmailVerification();
    }

    private void setupResendButton() {
        btnResendCode.setOnClickListener(v -> {
            btnResendCode.setEnabled(false);
            resendVerificationCode();
            startResendTimer();
        });
    }

    private void resendVerificationCode() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "تم إعادة إرسال رمز التحقق إلى بريدك الإلكتروني", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMessage = "فشل الإرسال: " + task.getException().getMessage();
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                            btnResendCode.setEnabled(true);
                        }
                    });
        } else {
            Toast.makeText(this, "لا يوجد مستخدم مسجل!", Toast.LENGTH_SHORT).show();
            btnResendCode.setEnabled(true);
        }
    }

    private void startResendTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(RESEND_DELAY, 1000) {
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("إعادة الإرسال متاحة بعد: " + millisUntilFinished / 1000 + " ثانية");
            }

            public void onFinish() {
                tvTimer.setText("");
                btnResendCode.setEnabled(true);
            }
        }.start();
    }


    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "لا يوجد مستخدم مسجل!", Toast.LENGTH_SHORT).show();
            return;
        }
        // أعد تحميل حالة المستخدم من الخادم
        user.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (user.isEmailVerified()) {
                    // تم التحقق، انتقل للـ MainActivity
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "بريديك لم يتم التحقق منه بعد.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this,
                        "فشل في التحقق: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
