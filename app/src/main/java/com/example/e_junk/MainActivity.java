package com.example.e_junk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth ejunkAuth;
    FirebaseUser ejunkUser;
    ProgressDialog progressDialog;

    ViewPager viewPager;
    TabLayout tabLayout;

    AccountFragment accountFragment;
    ScheduleFragment scheduleFragment;
    LocationFragment locationFragment;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Dashboard");

        ejunkAuth = FirebaseAuth.getInstance();
        ejunkUser = ejunkAuth.getCurrentUser();
        progressDialog = new ProgressDialog(this);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabLayout);

        accountFragment = new AccountFragment();
        scheduleFragment = new ScheduleFragment();
        locationFragment = new LocationFragment();

        tabLayout.setupWithViewPager(viewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(accountFragment, "Account");
        viewPagerAdapter.addFragment(scheduleFragment, "Schedule");
        viewPagerAdapter.addFragment(locationFragment, "Location");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_username);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_schedule);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_location);

        //BadgeDrawable badgeDrawable = tabLayout.getTabAt(0).getOrCreateBadge();
        //badgeDrawable.setVisible(true);
        //badgeDrawable.setNumber(3);

    }//end of inner block

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals("Logout")){
            progressDialog.setMessage("Logging out please wait");
            progressDialog.setTitle("Logout");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            performLogout();
        }if(item.getTitle().equals("Refresh")){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        }if(item.getTitle().equals("Edit Profile")){
            Intent intent = new Intent(MainActivity.this, EditProfile.class);
            startActivity(intent);
        }if(item.getTitle().equals("Settings")){
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        ejunkAuth.signOut();
        Toast.makeText(MainActivity.this, "Logout Successfully", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
        Intent intent = new Intent(MainActivity.this, LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}// end of outer block