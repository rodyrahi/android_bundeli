package com.kamingo.bundelikissan;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;

import com.example.bundelikissan.ui.gallery.GalleryFragment;
import com.example.bundelikissan.ui.home.HomeFragment;
import com.example.bundelikissan.ui.slideshow.SlideshowFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

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
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideBottomNavigationView() {
        bottomNavigationView.setVisibility(View.GONE);
    }



}
