
package com.example.database78;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

//loog in

    FirebaseAuth mAuth;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    private TextView toolbarTitle;
    private SearchView searchView;
    private CombinedFragment combinedFragment;
    private MenuItem searchMenuItem; // عنصر القائمة الخاص بالبحث

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();  // استعادة اللغة من SharedPreferences
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //loog in


        mAuth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);







        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }


        // إعداد زر القائمة الجانبية
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        drawerToggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.white));



        // **تحديث واجهة المستخدم داخل القائمة الجانبية**
        View headerView = navigationView.getHeaderView(0);
        ImageView userImage = headerView.findViewById(R.id.user_image);
        TextView userEmail = headerView.findViewById(R.id.user_email);
        TextView userName = headerView.findViewById(R.id.user_name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail.setText(user.getEmail());

            // التحقق من وجود اسم المستخدم
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                userName.setText(user.getDisplayName());
            } else {
                // استخدام البريد الإلكتروني كاسم مؤقت
                userName.setText(user.getEmail());
            }

            // معالجة صورة المستخدم
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(userImage);
            } else {
                Glide.with(this)
                        .load(R.drawable.profile2)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(userImage);
            }

            // استعادة البيانات من Firebase إذا كان هناك اتصال
            if (isNetworkConnected()) {
                DatabaseHelper.syncFromFirebase(this);
                DatabaseHelper.syncPendingOperations(MainActivity.this);
            }
        }




        // استماع للأزرار في القائمة الجانبية
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_total_account_updates) {
                startActivity(new Intent(MainActivity.this, TotalAccountUpdatesActivity.class));
            } else if (id == R.id.action_filter_total_account_updates) {
                startActivity(new Intent(MainActivity.this, FilterTotalAccountUpdatesActivity.class));
            } else if (id == R.id.btnRevenueAnalysis) {
                startActivity(new Intent(MainActivity.this, RevenueAnalysisActivity.class));
            } else if (id == R.id.btnTop) {
                startActivity(new Intent(MainActivity.this, TopSellingProductsActivity.class));
            }else if (id == R.id.nav_change_language) {
                showLanguageDialog();
            } else if (id == R.id.btnLogOut) {
                checkPendingOperationsBeforeLogout(); // التعديل هنا
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });



        loadFragment(new MainFragment());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment;

            if (item.getItemId() == R.id.navigation_main) {
                fragment = new MainFragment();
                toolbarTitle.setText(R.string.home);
                hideSearchView(); // إخفاء SearchView عند الانتقال إلى MainFragment
            } else if (item.getItemId() == R.id.navigation_combined) {
                fragment = new CombinedFragment();
                toolbarTitle.setText(R.string.display_data);
                showSearchView(); // إظهار SearchView عند الانتقال إلى CombinedFragment
            } else if (item.getItemId() == R.id.navigation_sales) {
                fragment = new Sales_mainFragment();
                toolbarTitle.setText(R.string.sales_list);
                hideSearchView(); // إخفاء SearchView عند الانتقال إلى Sales_mainFragment
            } else if (item.getItemId() == R.id.navigation_updates) {
                fragment = new UpdatesFragment();
                toolbarTitle.setText(R.string.update_log);
                hideSearchView(); // إخفاء SearchView عند الانتقال إلى UpdatesFragment
            } else {
                fragment = new Sales_mainFragment();
            }

            loadFragment(fragment);
            invalidateOptionsMenu(); // إعادة إنشاء القائمة لتحديث حالة MenuItem
            return true;
        });
    }




    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }




    //loog in

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // لم يُسجِّل دخول بعد
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else if (!user.isEmailVerified()) {
            // سجّل دخول مؤقت لكنه غير موثَّق
            startActivity(new Intent(this, VerificationActivity.class));
            finish();
        }
        // وإلا: مواصلة عمل MainActivity
    }




    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        if (fragment instanceof CombinedFragment) {
            combinedFragment = (CombinedFragment) fragment;
        } else {
            combinedFragment = null; // إزالة المرجع عند الانتقال إلى جزء آخر
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_button, menu);

        // الحصول على عنصر القائمة الخاص بالبحث
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("ابحث هنا...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (combinedFragment != null) {
                    combinedFragment.filterData(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (combinedFragment != null) {
                    combinedFragment.filterData(newText);
                }
                return false;
            }
        });

        // إخفاء زر البحث افتراضيًا
        if (combinedFragment == null) {
            hideSearchView();
        } else {
            showSearchView();
        }

        return true;
    }

    private void showSearchView() {
        if (searchMenuItem != null) {
            searchMenuItem.setVisible(true); // إظهار زر البحث
            searchView.setIconified(false); // توسيع SearchView تلقائيًا
            searchView.requestFocus(); // إعطاء التركيز لـ SearchView
        }
    }

    private void hideSearchView() {
        if (searchMenuItem != null) {
            searchMenuItem.setVisible(false); // إخفاء زر البحث
            searchView.setQuery("", false); // مسح النص الموجود في SearchView
            searchView.setIconified(true); // طي SearchView
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_total_account_updates) {
            Intent intent = new Intent(MainActivity.this, TotalAccountUpdatesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_filter_total_account_updates) {
            Intent intent = new Intent(MainActivity.this, FilterTotalAccountUpdatesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.btnRevenueAnalysis) {
            Intent intent = new Intent(MainActivity.this, RevenueAnalysisActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.btnTop) {
            Intent intent = new Intent(MainActivity.this, TopSellingProductsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }







    // حفظ اللغة في SharedPreferences
    private void saveLocale(String langCode) {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        prefs.edit().putString("My_Lang", langCode).apply();
    }

    // تحميل اللغة من SharedPreferences
    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String lang = prefs.getString("My_Lang", "ar"); // العربية افتراضياً
        setAppLocale(lang);
    }

    // تعديل إعدادات التطبيق على مستوى الموارد
    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources res = getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }



    private void showLanguageDialog() {
        String[] names = getResources().getStringArray(R.array.language_names);
        String[] codes = getResources().getStringArray(R.array.language_codes);

        // إيجاد اللغة المختارة حالياً
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String currentLang = prefs.getString("My_Lang", "ar");
        int checkedItem = Arrays.asList(codes).indexOf(currentLang);

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_language)
                .setSingleChoiceItems(names, checkedItem, (dialog, which) -> {
                    // عند اختيار اللغة
                    setAppLocale(codes[which]);
                    saveLocale(codes[which]);
                    dialog.dismiss();
                    recreate(); // إعادة إنشاء الـ Activity لتطبيق اللغة الجديدة
                })
                .show();
    }






//دوال خاصة بزر تسجيل الخروج من هنا
    private void checkPendingOperationsBeforeLogout() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        boolean hasPending = dbHelper.hasPendingOperations();
        dbHelper.close();

        if (hasPending) {
            showPendingOperationsAlert();
        } else {
            showNormalLogoutConfirmation();
        }
    }

    private void showPendingOperationsAlert() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(R.string.pending_operations_warning)
                .setPositiveButton(R.string.logout_anyway, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showNormalLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_logout)
                .setMessage(R.string.logout_confirmation_message)
                .setPositiveButton(R.string.logout, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performLogout() {
        mAuth.signOut();
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                .addOnCompleteListener(task -> {
                    getApplicationContext().deleteDatabase("SliteDb.db");
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                });
    }


//الى هنا









}
