package com.sse.iamhere;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;
import com.sse.iamhere.Views.DropdownOnClickListener;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private BottomNavigationView bottomNav;

    private Constants.Role role;
    private boolean isRoleDropdownExpanded;
    private int currentBottomNavItemId;
    private SparseArray<Fragment.SavedState> savedStateSA = new SparseArray<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Theme must be set before setContentView
        role = PreferencesUtil.getRole(this, Constants.Role.NONE);
        setTheme(role.getTheme());

        setContentView(R.layout.activity_home);
        setTitle(getString(R.string.activity_home_title));

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        NavigationView sideNav = findViewById(R.id.home_sideNav);
        setupNavContent(sideNav, savedInstanceState);

        setupNavAvatar();

        setupFragments(savedInstanceState);

        setupBottomNav();

    }


    private void setupNavContent(NavigationView sideNav, Bundle savedInstanceState) {
        sideNav.setItemIconTintList(null);

        View navHeaderView = sideNav.getHeaderView(0);
        ImageView dropdownIv = navHeaderView.findViewById(R.id.nav_header_dropdownIv);
        DropdownOnClickListener dropdownOnClickListener =
            new DropdownOnClickListener() {
                Menu menuNav = sideNav.getMenu();
                @Override
                public void onClick(View v, boolean animate) {
                    isRoleDropdownExpanded = !isRoleDropdownExpanded;

                    if (animate) {
                        //update to reflect new ui
                        if (isRoleDropdownExpanded) {
                            sideNav.setCheckedItem(role.getSideNavMenuItem());
                            dropdownIv.animate().rotation(dropdownIv.getRotation()-180f).setDuration(300).start();

                        } else {
                            dropdownIv.animate().rotation(dropdownIv.getRotation()+180f).setDuration(300).start();
                        }

                        // delay is needed as set group visible will cause a redraw, thus cancelling click animation and rotation animation,
                        // 350 ms is not a lot to feel any lag
                        new Handler().postDelayed(() -> {
                            menuNav.setGroupVisible(R.id.nav_group_accounts, isRoleDropdownExpanded);
                        }, 350);

                    } else {
                        if (isRoleDropdownExpanded) sideNav.setCheckedItem(role.getSideNavMenuItem());

                        menuNav.setGroupVisible(R.id.nav_group_accounts, isRoleDropdownExpanded);
                    }
                }
            };
        dropdownIv.setOnClickListener(dropdownOnClickListener);

        sideNav.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.nav_role_attendee:
                    changeRole(Constants.Role.ATTENDEE);
                    return true;
                case R.id.nav_role_host:
                    changeRole(Constants.Role.HOST);
                    return true;
                case R.id.nav_role_manager:
                    changeRole(Constants.Role.MANAGER);
                    return true;
                case R.id.nav_settings:
                    //TODO: Implement
                    return true;
                case R.id.nav_invite_codes:
                    //TODO: Implement
                    return true;
                default: return false;
            }
        });


        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.home_nav_drawer_open, R.string.home_nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // here we check that if the activity was started with the "changeRole" extra (role was changed) and so we
        // open the drawer and expand role dropdown on activity start
        if (getIntent().getExtras()!=null && savedInstanceState==null) {
            if (getIntent().getExtras().getBoolean("changeRole", false)) {
                drawer.openDrawer(GravityCompat.START);
                dropdownOnClickListener.onClick(dropdownIv,false);

                Toast.makeText(this,
                        getString(R.string.activity_home_role_change_toast) + " " + getString(role.toStringRes()),
                        Toast.LENGTH_LONG).show();
            }
        }

        // here we check if the screen was rotated while the drawer was open, if so we open the drawer and expand role dropdown
        if (savedInstanceState!=null) {
            if (savedInstanceState.getBoolean("isRoleDropdownExpanded",false)) {
                drawer.isDrawerOpen(GravityCompat.START);
                dropdownOnClickListener.onClick(dropdownIv,false);
            }
        }
    }
    private void changeRole(Constants.Role newRole) {
        PreferencesUtil.setRole(HomeActivity.this, newRole);
        Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("changeRole", true);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void setupNavAvatar() {

    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.home_bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_host_events:
                    swapFragments(item.getItemId(), getResources().getString(R.string.fragment_events_tag));
                    return true;
                case R.id.nav_host_groups:
                    swapFragments(item.getItemId(), getResources().getString(R.string.fragment_groups_tag));
                    return true;
            }
            return false;
        });
    }

    private void setupFragments(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedStateSA = savedInstanceState.getSparseParcelableArray("savedStateSA");
            currentBottomNavItemId = savedInstanceState.getInt("currentBottomNavItemId");

        } else {
            switch (role) {
                case ATTENDEE:

                    break;
                case HOST:
                    swapFragments(R.id.nav_host_events, getResources().getString(R.string.fragment_events_tag));
                    break;
                case MANAGER:

                    break;
            }
        }
    }


    private void swapFragments(int actionId, String tag) {
        if (getSupportFragmentManager().findFragmentByTag(tag) == null) {
            saveFragmentState(actionId);
            createFragment(actionId, tag);
        }
    }

    private void createFragment(int actionId, String tag) {
        Fragment fragment;
        switch (actionId) {
            case R.id.nav_host_events: fragment = new EventsFrag(); break;
            case R.id.nav_host_groups: fragment = new GroupsFrag(); break;
            default: throw new RuntimeException("HomeActivity:createFragment No fragment found for actionId: " + actionId);
        }

        fragment.setInitialSavedState(savedStateSA.get(actionId));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.home_mainLy, fragment, tag)
                .commit();
    }

    private void saveFragmentState(int actionId) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.home_mainLy);
        if (currentFragment != null) {
            savedStateSA.put(currentBottomNavItemId,
                    getSupportFragmentManager().saveFragmentInstanceState(currentFragment)
            );
        }
        currentBottomNavItemId = actionId;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

//        } else if (role == Constants.Role.ATTENDEE && bottomNav.getSelectedItemId() != R.id.nav_host_events) {
//            bottomNav.setSelectedItemId(R.id.nav_host_events);

        } else if (role == Constants.Role.HOST && bottomNav.getSelectedItemId() != R.id.nav_host_events) {
            bottomNav.setSelectedItemId(R.id.nav_host_events);

//        } else if (role == Constants.Role.MANAGER && bottomNav.getSelectedItemId() != R.id.nav_host_events) {
//            bottomNav.setSelectedItemId(R.id.nav_host_events);

        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            outState.putBoolean("isRoleDropdownExpanded", isRoleDropdownExpanded);
        }

        outState.putSparseParcelableArray("savedStateSA", savedStateSA);
        outState.putInt("currentBottomNavItemId", currentBottomNavItemId);

    }
}
