package com.example.beskar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.beskar.service.BeskarVpnService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BMainActivity";

    public static final String LAUNCH_ACTION = "com.example.Beskar.MainActivity.LAUNCH_ACTION";
    public static final int LAUNCH_ACTION_NONE = 0;
    public static final int LAUNCH_ACTION_ACTIVATE = 1;
    public static final int LAUNCH_ACTION_DEACTIVATE = 2;
    public static final int LAUNCH_ACTION_SERVICE_DONE = 3;

    public static final String LAUNCH_FRAGMENT = "com.example.Beskar.MainActivity.LAUNCH_FRAGMENT";
    public static final int FRAGMENT_NONE = -1;
    public static final int FRAGMENT_SETTINGS = 2;

    private static MainActivity instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        mTitle.setText(toolbar.getTitle());
        mTitle.setTextColor(Color.WHITE);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Initialize nav view and controller
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Update based on intent
        updateOnNewIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        updateOnNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateMainButton();
    }

    public static MainActivity getInstance() {
        return instance;
    }

    private void updateMainButton() {
        boolean isMainSwitchOn = BeskarVpnService.isActivated();
        SwitchMaterial mainSwitch = findViewById(R.id.activity_fragment_home_main_switch);
        TextView mainSwitchText = findViewById(R.id.activity_fragment_home_main_switch_text);

        // Protect from null pointer exceptions if not on HomeFragment
        if (mainSwitch != null) mainSwitch.setChecked(isMainSwitchOn);
        if (mainSwitchText != null) mainSwitchText.setText(isMainSwitchOn ? "DEACTIVATE" : "ACTIVATE");
    }

    private void updateOnNewIntent(Intent intent) {
        // Update lock screen actions
        if (!Beskar.getPrefs().contains("beskar_pin")) {
            startActivity(new Intent(MainActivity.this, LockActivity.class)
                    .putExtra(LockActivity.LOCK_SCREEN_ACTION, LockActivity.LOCK_SCREEN_ACTION_SET_UP));
            finish();
        }

        // Update launch actions
        int launchAction = intent.getIntExtra(LAUNCH_ACTION, LAUNCH_ACTION_NONE);
        Log.d(TAG, "Updating activity with launch action: " + launchAction);
        if (launchAction == LAUNCH_ACTION_ACTIVATE) {
            this.activateService();
            // Restart time marker from manual activation
            Beskar.getPrefs().edit().putLong("beskar_start_time_marker",
                    System.currentTimeMillis()).apply();
        } else if (launchAction == LAUNCH_ACTION_DEACTIVATE) {
            Beskar.deactivateService(getApplicationContext());
        } else if (launchAction == LAUNCH_ACTION_SERVICE_DONE) {
            Toast.makeText(getApplicationContext(), "Service " + (BeskarVpnService.isActivated()
                    ? "activated" :
                    "deactivated"), Toast.LENGTH_SHORT).show();
        }

        // Update fragments
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        int fragment = intent.getIntExtra(LAUNCH_FRAGMENT, FRAGMENT_NONE);
        Log.d(TAG, "Updating activity with fragment: " + fragment);
        if (fragment == FRAGMENT_SETTINGS) {
            navController.navigate(R.id.navigation_settings);
        }
    }

    public void onActivityResult(int request, int result, Intent data) {
        if (result == Activity.RESULT_OK) {
            Beskar.activateService(Beskar.getInstance());
            Beskar.updateShortcut(getApplicationContext());
        }
        super.onActivityResult(request, result, data);
    }

    public void activateService() {
        Intent intent = VpnService.prepare(Beskar.getInstance());
        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, Activity.RESULT_OK, null);
        }
    }
}