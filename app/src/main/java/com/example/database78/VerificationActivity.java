package com.example.database78;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    // تجاوز إدخال الكود إذا تم التحقق
                    Toast.makeText(this, "تم التحقق بنجاح!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
