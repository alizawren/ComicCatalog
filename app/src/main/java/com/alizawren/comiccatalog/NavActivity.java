package com.alizawren.comiccatalog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
        // Create a confirmation dialog of Signing Out
        new AlertDialog.Builder(this)
                .setTitle("Confirm Sign Out")
                .setMessage("Are you sure you want to Sign Out?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Return a Sign Out result
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("signout", "Signed User Out");
                            setResult(RESULT_OK, resultIntent);

                            // Signs User out of the app
                            FirebaseAuth.getInstance().signOut();
                            finish();
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }})
                .setNegativeButton("No", null)
                .show();
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
