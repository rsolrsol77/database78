
package com.example.database78;

import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        toolbarTitle.setText("الرئيسية");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }


        // إعداد زر القائمة الجانبية
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();



        // **تحديث واجهة المستخدم داخل القائمة الجانبية**
        View headerView = navigationView.getHeaderView(0);
        ImageView userImage = headerView.findViewById(R.id.user_image);
        TextView userEmail = headerView.findViewById(R.id.user_email);
        TextView userName = headerView.findViewById(R.id.user_name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail.setText(user.getEmail()); // عرض البريد الإلكتروني
            userName.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(userImage); // تحميل الصورة باستخدام Glide
            }


            // استعادة البيانات من Firebase إذا كان هناك اتصال
            if (isNetworkConnected()) {
                DatabaseHelper.syncFromFirebase(this);

                DatabaseHelper.syncPendingOperations(MainActivity.this); // إضافة هذا السطر بعد syncFromFirebase

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
            } else if (id == R.id.btnLogOut) {
                mAuth.signOut();
                GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                        .addOnCompleteListener(task -> {
                            // حذف قاعدة البيانات المحلية
                            getApplicationContext().deleteDatabase("SliteDb.db");
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        });
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
                toolbarTitle.setText("الرئيسية");
                hideSearchView(); // إخفاء SearchView عند الانتقال إلى MainFragment
            } else if (item.getItemId() == R.id.navigation_combined) {
                fragment = new CombinedFragment();
                toolbarTitle.setText("عرض البيانات");
                showSearchView(); // إظهار SearchView عند الانتقال إلى CombinedFragment
            } else if (item.getItemId() == R.id.navigation_sales) {
                fragment = new Sales_mainFragment();
                toolbarTitle.setText("قائمة المبيعات");
                hideSearchView(); // إخفاء SearchView عند الانتقال إلى Sales_mainFragment
            } else if (item.getItemId() == R.id.navigation_updates) {
                fragment = new UpdatesFragment();
                toolbarTitle.setText("سجل التحديثات");
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

    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
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
}
