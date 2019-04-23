package com.alizawren.comiccatalog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class NavActivity extends AppCompatActivity {

    static final String TAG = "NavActivity";

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_navigation);

        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());

        loadFragment(new ExploreFragment());

        // ------------------- Action bar ----------------------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onBackPressed() {
        // Signs User out of the app
        FirebaseAuth.getInstance().signOut();
        super.onBackPressed();
    }

    // ------------------------- Fragment methods -----------------------------

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_frame, fragment)
                    .commit();

            return true;
        }

        return false;
    }

    // ------------------------ Navigation methods ----------------------------

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_explore:
                    fragment = new ExploreFragment();
                    break;
                case R.id.navigation_library:
                    fragment = new LibraryFragment();
                    break;
                case R.id.navigation_settings:
                    fragment = new SettingsFragment();
                    break;
            }
            return loadFragment(fragment);
        }
    };
}
