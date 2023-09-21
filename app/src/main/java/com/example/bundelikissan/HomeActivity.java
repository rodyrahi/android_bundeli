package com.kamingo.bundelikissan;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import com.example.bundelikissan.ui.gallery.GalleryFragment;
import com.example.bundelikissan.ui.home.HomeFragment;
import com.example.bundelikissan.ui.logout.LogoutFragment;
import com.example.bundelikissan.ui.slideshow.SlideshowFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.kamingo.bundelikissan.R;
import com.kamingo.bundelikissan.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity implements HomeFragment.BottomNavViewCallback {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    public BottomNavigationView bottomNavigationView;
    private static final int PERMISSION_REQUEST_CODE = 123; // Unique code for permission request
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHome.toolbar);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
        ).setOpenableLayout(binding.drawerLayout) // Use the existing binding reference
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController); // Use the existing binding reference

        bottomNavigationView = binding.bottomNavigation; // Assignment moved here

        NavigationUI.setupWithNavController(bottomNavigationView, navController);
//        checkAndRequestMicrophonePermission();
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.navigation_notifications:
                        selectedFragment = new GalleryFragment();
                        break;
                    case R.id.navigation_dashboard:
                        selectedFragment = new SlideshowFragment();
                        break;
                    case R.id.navigation_logout:
                        // Handle logout or any other action
                        selectedFragment = new LogoutFragment();

                        break;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.nav_host_fragment_content_home, selectedFragment)
                            .commit();
                    return true; // Return true to indicate the item click is handled
                }

                return false; // Return false if no action is performed
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Implement interface methods
    @Override
    public void showBottomNavigationView() {
        bottomNavigationView.setVisibility(View.GONE);
    }

    @Override
    public void hideBottomNavigationView() {
        bottomNavigationView.setVisibility(View.GONE);
    }



//    private void checkAndRequestMicrophonePermission() {
//        // Check if the permission is already granted
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                // Permission is not granted, request it
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
//            }
//        }
//    }

    // Handle permission request result

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, you can perform microphone-related actions here if needed
//            } else {
//                // Permission denied, handle accordingly (e.g., show a message)
//            }
//        }
//    }
}
