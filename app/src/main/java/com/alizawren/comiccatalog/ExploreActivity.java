package com.alizawren.comiccatalog;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.MenuItem;

public class ExploreActivity extends AppCompatActivity {

    static final String TAG = "ExploreActivity";

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_explore);

        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());

        // ------------------- Action bar ----------------------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    // ------------------------ Navigation methods ----------------------------

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // DEFAULT STUFF
            switch (item.getItemId()) {
                case R.id.navigation_explore:
                    //mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_library:
                    startActivity(MainActivity.class);
                    return true;
                case R.id.navigation_settings:
                    //mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    private void startActivity(Class activityClass) {
        final Intent intent = new Intent(this, activityClass);
        this.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

}
