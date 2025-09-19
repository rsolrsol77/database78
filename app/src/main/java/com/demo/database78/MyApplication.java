// MyApplication.java
package com.demo.database78;

import android.app.Application;
import com.google.android.gms.ads.MobileAds;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // تهيئة مكتبة Google Mobile Ads SDK
        MobileAds.initialize(this, initializationStatus -> {
            // يمكنك هنا فحص initializationStatus إن أردت
        });
    }
}
