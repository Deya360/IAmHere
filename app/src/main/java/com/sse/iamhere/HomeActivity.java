package com.sse.iamhere;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sse.iamhere.Dialogs.InviteCodesDialog;
import com.sse.iamhere.Dialogs.ReLogDialog;
import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.RequestCallback;
import com.sse.iamhere.Server.RequestManager;
import com.sse.iamhere.Server.TokenProvider;
import com.sse.iamhere.Subclasses.DropdownOnClickListener;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;

import java.util.ArrayList;

import static com.sse.iamhere.Utils.Constants.AUTHENTICATION_RQ;
import static com.sse.iamhere.Utils.Constants.DEBUG_MODE;
import static com.sse.iamhere.Utils.Constants.INVITE_CODES_TEMP;
import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;
import static com.sse.iamhere.Utils.PreferencesUtil.getStringArrayPrefByName;
import static com.sse.iamhere.Utils.PreferencesUtil.setStringArrayByName;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private NavigationView sideNav;
    private BottomNavigationView bottomNav;

    private Constants.Role role;
    private boolean isRoleDropdownExpanded;
    private int currentBottomNavItemId;
    private SparseArray<Fragment.SavedState> savedStateSA = new SparseArray<>();

    private boolean changingRole = false;
    private int menuItemId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Theme must be set before setContentView
        role = PreferencesUtil.getRole(this, Constants.Role.NONE);
        setTheme(role.getTheme());

        setContentView(R.layout.activity_home);
        setTitle(getString(R.string.activity_home_title));

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        sideNav = findViewById(R.id.home_sideNav);
        setupSideNavContent(savedInstanceState);
        setupNavAvatar();

        setupBottomNav();

        setupFragments(savedInstanceState);

        if (savedInstanceState!=null) {
            // check if role change was intiated before screen rotation
            changingRole = savedInstanceState.getBoolean("changingRole");
            menuItemId = savedInstanceState.getInt("menuItemId");

            if (changingRole) {
                sideNav.getMenu().findItem(menuItemId).getActionView().setVisibility(View.VISIBLE);
            }
        }


//        startBackgroundServices(1000*10);

    }

    private void startBackgroundServices(int delayInMilli) {
        new Handler().postDelayed(this::startBackgroundAccountCheck, delayInMilli);
    }
    private void startBackgroundAccountCheck() {
        //Check if account is still valid
        if ((role=PreferencesUtil.getRole(HomeActivity.this, Constants.Role.NONE))!= Constants.Role.NONE) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser!=null) {
                AsyncTask.execute(() -> {
                    new RequestManager(this)
                        .check(currentUser.getUid())
                        .setCallback(new RequestCallback() {
                            @Override
                            public void onCheckSuccess(CheckData checkResult) {
                                startBackgroundTokenValidityCheck(checkResult.getRegisteredStatusByRole(role));
                            }

                            @Override
                            public void onCheckFailure(int errorCode) {
                                //TODO: implement
                            }
                        });
                });
            } else {
                //TODO: implement
            }
        } else {
            //Todo: implement
        }

    }
    private void startBackgroundTokenValidityCheck(boolean isRegistered) {
        //Check if token is still valid
        Constants.Role role;
        if ((role=PreferencesUtil.getRole(HomeActivity.this, Constants.Role.NONE))!= Constants.Role.NONE) {
            TokenProvider.getUsableAccessToken(HomeActivity.this, TOKEN_ACCESS, role,
                new TokenProvider.TokenProviderCallback() {
                    @Override
                    public void onSuccess(int token_type, String token) {
                        FragmentManager fm = getSupportFragmentManager();
                        ReLogDialog reLogDialog = new ReLogDialog(isRegistered);
                        reLogDialog.show(fm, getString(R.string.fragment_relog_dialog_tag));
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        //Todo: implement
                    }
                });

        } else {
            //Todo: implement
        }
    }


    private void setupSideNavContent(Bundle savedInstanceState) {
        sideNav.setItemIconTintList(null);

        View navHeaderView = sideNav.getHeaderView(0);
        TextView textrole = navHeaderView.findViewById(R.id.nav_header_nameTv);
        textrole.setText(getString(role.toStringRes()));
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
            if (!menuItem.isChecked()) {
                View actionView;
                switch (menuItem.getItemId()) {
                    case R.id.nav_role_attendee:
                        if (!changingRole) {
                            actionView = menuItem.getActionView();
                            actionView.setVisibility(View.VISIBLE);
                            menuItemId = menuItem.getItemId();
                            changeRole(Constants.Role.ATTENDEE, actionView);
                        }
                        return false;

                    case R.id.nav_role_host:
                        if (!changingRole) {
                            actionView = menuItem.getActionView();
                            actionView.setVisibility(View.VISIBLE);
                            menuItemId = menuItem.getItemId();
                            changeRole(Constants.Role.HOST, actionView);
                        }
                        return false;

                    case R.id.nav_role_manager:
                        if(!changingRole) {
                            actionView = menuItem.getActionView();
                            actionView.setVisibility(View.VISIBLE);
                            menuItemId = menuItem.getItemId();
                            changeRole(Constants.Role.MANAGER, actionView);
                        }
                        return false;

                    case R.id.nav_settings:
                        //TODO: Implement
                        return true;
                    case R.id.nav_invite_codes:
                        //TODO: Implement
                        showInviteCodeDialog();
                        return true;
                }
            }
            return false;
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
                        getString(R.string.home_role_change_toast) + " " + getString(role.toStringRes()),
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

    private void setupNavAvatar() {

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





    // workflow, first we check if we have a stored token for the new role, if yes, we switch interface over,
    // if not, second, we check if user has account of new role, if yes, we redirect to login, else, to register.
    private void changeRole(Constants.Role newRole, View actionView) {
        changingRole = true;
        if (PreferencesUtil.isTokenAvailableForRole(this, newRole) || DEBUG_MODE) {
            preformRoleSwitch(newRole);

        } else {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser!=null) {
                AsyncTask.execute(() -> {
                    new RequestManager(this)
                            .check(currentUser.getUid())
                            .setCallback(new RequestCallback() {
                                @Override
                                public void onCheckSuccess(CheckData checkResult) {
                                    startAuthenticationActivityDialog(
                                        checkResult.getRegisteredStatusByRole(newRole), newRole, currentUser);
                                }

                                @Override
                                public void onCheckFailure(int errorCode) {
                                    changingRole = false;
                                    actionView.setVisibility(View.GONE);
                                    //TODO: implement
                                }
                            });
                });
            } else {
                changingRole = false;
                actionView.setVisibility(View.GONE);
                //TODO: implement. close drawer then show snackbar
            }
        }
    }

    private void preformRoleSwitch(Constants.Role newRole) {
        PreferencesUtil.setRole(HomeActivity.this, newRole);
        Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("changeRole", true);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void startAuthenticationActivityDialog(boolean isRegistered, Constants.Role newRole, FirebaseUser currentUser) {
        Intent intent;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isRegistered", isRegistered);
        bundle.putString("phone", currentUser.getPhoneNumber());
        bundle.putString("phoneFormatted", currentUser.getPhoneNumber()); //TODO: format properly
        bundle.putBoolean("showAsDialog", true);
        bundle.putInt("forceRole", newRole.toIdx());
        bundle.putInt("activityOrientation",this.getResources().getConfiguration().orientation);
        bundle.putInt("returnRequestCode", AUTHENTICATION_RQ);


        String description = "";
        if (isRegistered) {
            description = getString(R.string.auth_subtitleTv_remote_login_label_prefix) + " " +
                    getString(newRole.toStringRes()) + " " + getString(R.string.auth_subtitleTv_remote_login_label_suffix);

        } else {
            switch (newRole) {
                case HOST:
                case MANAGER:
                    description = getString(R.string.auth_subtitleTv_remote_registration_label_prefix) + " " +
                            getString(newRole.toStringRes()) + " "
                            + getString(R.string.auth_subtitleTv_remote_registration_label_suffix);
                    break;

                case ATTENDEE:
                    description = getString(R.string.auth_subtitleTv_remote_registration_label_prefix) +
                            getString(R.string.auth_subtitleTv_remote_registration_label_prefix_attendee_extra) + " " +
                            getString(newRole.toStringRes()) + " "
                            + getString(R.string.auth_subtitleTv_remote_registration_label_suffix);
                    break;
            }
        }


        bundle.putString("customDescription", description);

        intent = new Intent(HomeActivity.this, AuthenticationActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, AUTHENTICATION_RQ);
    }

    private void showInviteCodeDialog() {
        //TODO invite codes should be loaded from server instead of local shared prefs
        InviteCodesDialog inviteCodesDialog = new InviteCodesDialog(role, new InviteCodesDialog.InviteCodesDialogListener() {
            @Override
            public void onPositiveButton(ArrayList<String> individuals) {
                setStringArrayByName(HomeActivity.this, INVITE_CODES_TEMP, individuals);
            }

            @Override
            public void onDismiss() {

            }
        });

        ArrayList<String> arr = getStringArrayPrefByName(this, INVITE_CODES_TEMP, "");
        inviteCodesDialog.setInviteCodes(arr);

        FragmentManager fm = getSupportFragmentManager();
        inviteCodesDialog.show(fm, getString(R.string.fragment_invite_code_dialog_tag));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // receives result of launched authentication activity, if result is ok, change role, else cancel
        if (requestCode==AUTHENTICATION_RQ) {
            if (resultCode==RESULT_OK) {
                if (data!=null) {
                    int newRole = data.getIntExtra("role",-1);
                    if (newRole!=-1) {
                        preformRoleSwitch(Constants.Role.values()[newRole]);
                        return;
                    }
                }
            }

            changingRole = false;
            if (menuItemId!=-1) {
                sideNav.getMenu().findItem(menuItemId).getActionView().setVisibility(View.GONE);
            }
        }
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

        outState.putBoolean("changingRole", changingRole);
        outState.putInt("menuItemId", menuItemId);
    }
}
