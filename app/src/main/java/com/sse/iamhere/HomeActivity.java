package com.sse.iamhere;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sse.iamhere.Dialogs.InviteCodesDialog;
import com.sse.iamhere.Dialogs.ReLogDialog;
import com.sse.iamhere.Server.AuthRequestBuilder;
import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.Body.CredentialData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Server.TokenProvider;
import com.sse.iamhere.Subclasses.DropdownOnClickListener;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Subclasses.OnSingleClickNavListener;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.InternetUtil;
import com.sse.iamhere.Utils.PreferencesUtil;
import com.sse.iamhere.Utils.TextFormatter;

import static com.sse.iamhere.Utils.Constants.ACCOUNT_RQ;
import static com.sse.iamhere.Utils.Constants.AUTHENTICATION_RELOG_RQ;
import static com.sse.iamhere.Utils.Constants.AUTHENTICATION_RQ;
import static com.sse.iamhere.Utils.Constants.DEBUG_MODE;
import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;
import static com.sse.iamhere.Utils.Constants.TOKEN_REFRESH;

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


        /*
        * Setup the side navigation pane (as in, setup the role dropdown menu, menu items. etc)
        * */
        sideNav = findViewById(R.id.home_sideNav);
        setupSideNavContent(savedInstanceState);

        /*
        * Setup user avatar (in the side navigation pane) based on user name
        * */
        setupNavAvatar();

        /*
        * Setup the bottom navigation pane based on the current role
        * */
        setupBottomNav();

        /*
        * Based on both the selected role (in the side nav panel) and the current tab (in the bottom nav),
        * show the correct fragment in R.layout.content_home
        * */
        setupFragments(savedInstanceState);


        /*
        * Restore variable states after screen rotation
        * */
        if (savedInstanceState!=null) {
            // check if role change was intiated before screen rotation
            changingRole = savedInstanceState.getBoolean("changingRole");
            menuItemId = savedInstanceState.getInt("menuItemId");

            if (changingRole) {
                sideNav.getMenu().findItem(menuItemId).getActionView().setVisibility(View.VISIBLE);
            }
        }

        startBackgroundServices(1000*10);
    }

    private void startBackgroundServices(int delayInMilli) {
        new Handler().postDelayed(this::startBackgroundInternetCheck, delayInMilli);
    }
    private void startBackgroundInternetCheck() {
        //Check if internet connection is available
        new InternetUtil(new InternetUtil.InternetResponse() {
            @Override
            public void isConnected() {
                startBackgroundAccountCheck();
            }

            @Override
            public void notConnected() {
                showInfoSnackbar("Debug: No Internet Connection", Snackbar.LENGTH_LONG);
            }
        }).hasInternetConnection(this);
    }
    private void startBackgroundAccountCheck() {
        //Check if account is still valid
        if ((role=PreferencesUtil.getRole(HomeActivity.this, Constants.Role.NONE))!= Constants.Role.NONE) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser!=null) {
                AsyncTask.execute(() -> {
                    new AuthRequestBuilder(this)
                        .setCallback(new RequestsCallback() {
                            @Override
                            public void onCheckSuccess(CheckData checkResult) {
                                super.onCheckSuccess(checkResult);
                                startBackgroundSessionValidityCheck(checkResult.getRegisteredStatusByRole(role));
                            }

                            @Override
                            public void onFailure(int errorCode) {
                                //Todo: implement properly: add relog dialog (role not constrained)
                                super.onFailure(errorCode);
                                showInfoSnackbar("Debug: Couldn't get user account for role", Snackbar.LENGTH_LONG);
                            }
                        })
                        .check(currentUser.getUid());
                });
            } else {
                //Todo: implement properly: add verify phone dialog
                showInfoSnackbar("Debug: Couldn't get firebase user", Snackbar.LENGTH_LONG);
            }
        } else {
            //Todo: implement properly: add relog dialog (role not constrained)
            showInfoSnackbar("Debug: Couldn't get role", Snackbar.LENGTH_LONG);
        }
    }
    private void startBackgroundSessionValidityCheck(boolean isRegistered) {
        //Check if refresh token is still valid
        TokenProvider.getUsableToken(HomeActivity.this, TOKEN_REFRESH,
            PreferencesUtil.getRole(HomeActivity.this, Constants.Role.NONE),
            new TokenProvider.TokenProviderCallback() {
                @Override
                public void onSuccess(int token_type, String token) {
                    //don't need to do anything all is good,
                    // perhaps in future we add a timer to repeat this chain of checks again every n seconds
                }

                @Override
                public void onFailure(int errorCode) {
                    if (errorCode== Constants.RQM_EC.REFRESH_REFRESH_EXPIRED) {
                        if (getWindow().getDecorView().isShown()) {
                            FragmentManager fm = getSupportFragmentManager();
                            ReLogDialog reLogDialog = new ReLogDialog(isRegistered);
                            reLogDialog.show(fm, getString(R.string.fragment_relog_dialog_tag));
                        }
                    }
                }
            });
    }

    private void startBackgroundInviteCheck() {

    }


    private void setupSideNavContent(Bundle savedInstanceState) {
        sideNav.setItemIconTintList(null);

        View navHeaderView = sideNav.getHeaderView(0);
        TextView headerRoleTv = navHeaderView.findViewById(R.id.nav_header_roleTv);
        headerRoleTv.setText(getString(role.toStringRes()));

        RelativeLayout headerAvatarLy = navHeaderView.findViewById(R.id.nav_header_avatarLy);
        headerAvatarLy.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                startAccountActivity();
            }
        });

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

        sideNav.setNavigationItemSelectedListener(new OnSingleClickNavListener() {
            @Override
            public boolean onSingleNavigationItemSelected(@NonNull MenuItem menuItem) {
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
                            if (!changingRole) {
                                actionView = menuItem.getActionView();
                                actionView.setVisibility(View.VISIBLE);
                                menuItemId = menuItem.getItemId();
                                changeRole(Constants.Role.MANAGER, actionView);
                            }
                            return false;

                        case R.id.nav_all_events:
                            startAllEventsActivity();
                            return true;

                        case R.id.nav_invite_codes:
                            showInviteCodeDialog();
                            return true;

                        case R.id.nav_account:
                            startAccountActivity();
                            return true;

                        case R.id.nav_settings:
                            //TODO: Implement
                            return true;
                    }
                }
                return false;
            }
        });

        //Customize side nav menu items based on role
        MenuItem inviteCodesMIt = sideNav.getMenu().findItem(R.id.nav_invite_codes);
        inviteCodesMIt.setVisible(role!=Constants.Role.MANAGER);

        MenuItem allEventsMIt = sideNav.getMenu().findItem(R.id.nav_all_events);
        allEventsMIt.setVisible(role==Constants.Role.HOST);

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
                case R.id.nav_host_parties:
                    swapFragments(item.getItemId(), getResources().getString(R.string.fragment_parties_tag));
                    return true;
            }
            return false;
        });
    }

    private void setupNavAvatar() {
        AsyncTask.execute(() -> {
            RequestBuilder rb = new RequestBuilder()
                .setCallback(new RequestsCallback() {
                    @Override
                    public void onGetCredentialsSuccess(CredentialData credentialData) {
                        super.onGetCredentialsSuccess(credentialData);
                        String name = credentialData.getName();

                        View navHeaderView = sideNav.getHeaderView(0);
                        TextView headerNameTv = navHeaderView.findViewById(R.id.nav_header_nameTv);
                        TextView headerAvatarTv = navHeaderView.findViewById(R.id.nav_header_avatarTv);
                        ImageView headerAvatarIv = navHeaderView.findViewById(R.id.nav_header_avatarIv);

                        if (!TextUtils.isEmpty(name)) {
                            headerNameTv.setVisibility(View.VISIBLE);
                            headerNameTv.setText(name);
                            headerAvatarTv.setText(String.valueOf(name.charAt(0)));
                            headerAvatarIv.setVisibility(View.GONE);

                        } else {
                            headerNameTv.setVisibility(View.GONE);
                            headerNameTv.setText("");
                            headerAvatarTv.setText("");
                            headerAvatarIv.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        super.onFailure(errorCode);
                        if (errorCode == Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), Snackbar.LENGTH_LONG);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), Snackbar.LENGTH_LONG);
                        }
                    }
                });
            rb.checkInternet(this).attachToken(this, TOKEN_ACCESS);
            switch (role) {
                case ATTENDEE: rb.callRequest(rb::attendeeGetCredentials); break;
                case HOST: rb.callRequest(rb::hostGetCredentials); break;
                case MANAGER: break;//FIX_FOR_MANAGER
            }
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
            case R.id.nav_host_parties: fragment = new PartiesFrag(); break;
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
                new InternetUtil(new InternetUtil.InternetResponse() {
                    @Override
                    public void isConnected() {
                        AsyncTask.execute(() -> {
                            new AuthRequestBuilder(HomeActivity.this)
                                .setCallback(new RequestsCallback() {
                                    @Override
                                    public void onCheckSuccess(CheckData checkResult) {
                                        super.onCheckSuccess(checkResult);
                                        startActivityForResult(getAuthActivityIntent(currentUser,
                                                checkResult.getRegisteredStatusByRole(newRole),
                                                newRole, false, AUTHENTICATION_RQ), AUTHENTICATION_RQ);
                                    }

                                    @Override
                                    public void onFailure(int errorCode) {
                                        super.onFailure(errorCode);
                                        changeRoleFailure(actionView, getString(R.string.msg_server_error));
                                    }
                                })
                                .check(currentUser.getUid());
                        });
                    }

                    @Override
                    public void notConnected() {
                        changeRoleFailure(actionView, getString(R.string.splash_connectionTv_label));
                    }
                }).hasInternetConnection(this);

            } else {
                changeRoleFailure(actionView, getString(R.string.msg_unknown_error));
            }
        }
    }
    private void changeRoleFailure(View actionView, String errorMsg) {
        changingRole = false;
        actionView.setVisibility(View.GONE);
        drawer.closeDrawer(GravityCompat.START);
        showInfoSnackbar(errorMsg, Snackbar.LENGTH_LONG);
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

    private Intent getAuthActivityIntent(FirebaseUser currentUser, boolean isRegistered, Constants.Role role,
                                         boolean disallowCancel, int requestCode) {
        Intent intent;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isRegistered", isRegistered);
        bundle.putString("phone", currentUser.getPhoneNumber());
        bundle.putString("phoneFormatted", TextFormatter.formatPhone(currentUser.getPhoneNumber()));
        bundle.putBoolean("showAsDialog", true);

        if (role!=null && role!= Constants.Role.NONE) {
            bundle.putInt("forceRole", role.toIdx());
        }
        bundle.putInt("activityOrientation",this.getResources().getConfiguration().orientation);
        bundle.putBoolean("disallowCancel", disallowCancel);
        bundle.putInt("returnRequestCode", requestCode);


        String description = "";
        if (isRegistered) {
            description = getString(R.string.auth_subtitleTv_remote_login_label_prefix) + " " +
                    getString(role.toStringRes()) + " " + getString(R.string.auth_subtitleTv_remote_login_label_suffix);

        } else {
            switch (role) {
                case HOST:
                case MANAGER:
                    description = getString(R.string.auth_subtitleTv_remote_registration_label_prefix) + " " +
                            getString(role.toStringRes()) + " "
                            + getString(R.string.auth_subtitleTv_remote_registration_label_suffix);
                    break;

                case ATTENDEE:
                    description = getString(R.string.auth_subtitleTv_remote_registration_label_prefix) +
                            getString(R.string.auth_subtitleTv_remote_registration_label_prefix_attendee_extra) + " " +
                            getString(role.toStringRes()) + " "
                            + getString(R.string.auth_subtitleTv_remote_registration_label_suffix);
                    break;
            }
        }


        bundle.putString("customDescription", description);
        intent = new Intent(HomeActivity.this, AuthenticationActivity.class);
        intent.putExtras(bundle);
        return intent;
    }

    private void startAllEventsActivity() {
        Intent intent = new Intent(HomeActivity.this, AllEventsActivity.class);
        startActivity(intent);
    }

    private void startAccountActivity() {
        Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
        startActivityForResult(intent, Constants.ACCOUNT_RQ);
    }

    private void showInviteCodeDialog() {
        InviteCodesDialog inviteCodesDialog = new InviteCodesDialog();
        inviteCodesDialog.show(getSupportFragmentManager(), getString(R.string.fragment_invite_code_dialog_tag));
    }

    private void showInfoSnackbar(String msg, int duration) {
        if (getWindow().getDecorView().isShown()) {
            if (!TextUtils.isEmpty(msg)) {
                Snackbar.make(findViewById(R.id.home_mainLy), msg, duration).show();
            }
        }
    }


    /* This method is a callback of the ReLogDialog fragment */
    public void onReLogDialogAuthenticate(boolean isRegistered) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser!=null) {
            startActivityForResult(getAuthActivityIntent(currentUser,
                    isRegistered, role, true, AUTHENTICATION_RELOG_RQ), AUTHENTICATION_RELOG_RQ);

        } else {
            Toast.makeText(HomeActivity.this, getString(R.string.msg_unknown_error), Toast.LENGTH_LONG).show();
        }
    }

    /* This method is a callback of the InviteCodeDialog fragment */
    public void onInviteCodeDialogDismissed(boolean updated) {
        if (updated) {
            AsyncTask.execute(this::startBackgroundInviteCheck);
        }
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

            if (menuItemId!=-1) {
                changeRoleFailure(sideNav.getMenu().findItem(menuItemId).getActionView(), getString(R.string.msg_authentication_aborted));
            }
        }

        if (requestCode==AUTHENTICATION_RELOG_RQ) {
            if (resultCode==RESULT_OK) {
                preformRoleSwitch(PreferencesUtil.getRole(this, Constants.Role.NONE));

            } else { //RESULT_CANCELED
                finish();
            }
        }

        // receives result from account saying whether account details need to be updated
        if (requestCode==ACCOUNT_RQ) {
            if (resultCode==RESULT_OK) {
                if (data!=null) {
                    boolean isLogout = data.getBooleanExtra("isLogout",false);
                    if (isLogout) {
                        restart();


                    } else {
                        boolean updateNeeded = data.getBooleanExtra("updateNeeded",false);
                        if (updateNeeded) {
                            setupNavAvatar();
                        }
                    }
                }
            }
        }
    }

    private void restart() {
        Intent intent = new Intent(HomeActivity.this, SplashActivity.class);
        startActivity(intent);
        finish();
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
