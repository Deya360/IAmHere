package com.sse.iamhere;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.florent37.viewtooltip.ViewTooltip;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.sse.iamhere.Server.AuthRequestBuilder;
import com.sse.iamhere.Server.Body.CredentialData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.InternetUtil;
import com.sse.iamhere.Utils.PreferencesUtil;
import com.sse.iamhere.Utils.TextFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;
import static com.sse.iamhere.Utils.Constants.TOKEN_REFRESH;

public class AccountActivity extends AppCompatActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    ImageView editApplyIv;
    TextInputEditText nameEt;
    TextInputEditText emailEt;
    TextInputLayout emailLy;
    ShimmerFrameLayout placeholderLy;
    LinearLayout detailsLy;

    private Constants.Role role;
    private CredentialData loadedCredentialData;
    private boolean isDataRefreshing = false;
    private boolean editMode = false;
    private boolean wasInEditMode = false;
    private boolean changesMade = false;
    private boolean isHelpTooltipShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Theme must be set before setContentView
        role = PreferencesUtil.getRole(this, Constants.Role.NONE);
        setTheme(role.getTheme());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_account);

        if (savedInstanceState!=null) {
            changesMade = savedInstanceState.getBoolean("changesMade");
            wasInEditMode = savedInstanceState.getBoolean("wasInEditMode");
            if (wasInEditMode) {
                loadedCredentialData = savedInstanceState.getParcelable("loadedCredentialData");
            }
        }

        overridePendingTransition(R.anim.push_up_in, R.anim.none);
        setupUI();

        populateLocalData();
        if (!wasInEditMode) {
            loadData();
        } else {
            wasInEditMode = false;
            toggleEditMode();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupUI() {
        setTitle(R.string.activity_account_events_title);

        Toolbar toolbar = findViewById(R.id.account_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        // Setup of swipe to refresh layout
        mSwipeRefreshLayout = findViewById(R.id.account_swipe_refreshLy);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isDataRefreshing) {
                isDataRefreshing = true;
                populateLocalData();
                loadData();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


        // Setup views
        editApplyIv = findViewById(R.id.account_edit_applyIv);
        nameEt = findViewById(R.id.account_nameEt);
        emailEt = findViewById(R.id.account_emailEt);
        placeholderLy = findViewById(R.id.account_placeholderLy);
        detailsLy = findViewById(R.id.account_detailsLy);

        ImageView logoutIv = findViewById(R.id.account_logoutIv);
        logoutIv.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
            AsyncTask.execute(() -> {
                new InternetUtil(new InternetUtil.InternetResponse() {
                    @Override
                    public void isConnected() {
                        new AuthRequestBuilder(AccountActivity.this)
                            .attachToken(TOKEN_REFRESH)
                            .setCallback(new RequestsCallback() {
                                @Override
                                public void onLogoutSuccess() {
                                    try {
                                        PreferencesUtil.setToken(AccountActivity.this, null, role);
                                        finishActivity(true);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        onFailure(Constants.RQM_EC.TOKEN_STORE_FAIL);
                                    }
                                }

                                @Override
                                public void onFailure(int errorCode) {
                                    if (errorCode== Constants.RQM_EC.TOKEN_STORE_FAIL) {
                                        showInfoSnackbar(getString(R.string.msg_unknown_error), Snackbar.LENGTH_LONG);

                                    } else {
                                        showInfoSnackbar(getString(R.string.msg_server_error), Snackbar.LENGTH_LONG);
                                    }
                                }
                            })
                            .logout();
                    }

                    @Override
                    public void notConnected() {
                        showInfoSnackbar(getString(R.string.splash_connectionTv_label), Snackbar.LENGTH_LONG);
                    }
                }).hasInternetConnection(AccountActivity.this);
            });
            }
        });

        editApplyIv.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!editMode) {
                    toggleEditMode();
                } else {
                    saveData();
                }
            }
        });

        nameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateAvatar(nameEt.getText().toString().trim());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        emailLy = findViewById(R.id.account_emailLy);
        emailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String email = emailEt.getText().toString().trim();
                if (email.length() == 0) {
                    emailLy.setError(null);

                } else {
                    if (isEmailValid(email)) {
                        emailLy.setError(null);
                    } else {
                        emailLy.setError(getString(R.string.account_bad_email));
                    }
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        emailEt.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                if(event.getRawX() >= (emailEt.getRight() - emailEt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (!isHelpTooltipShown) {
                        showHelpTooltip();
                    }
                    return true;
                }
            }
            return false;
        });
    }

    private void updateAvatar(String name) {
        TextView avatarTv = findViewById(R.id.account_avatarTv);
        ImageView avatarIv = findViewById(R.id.account_avatarIv);

        if (!TextUtils.isEmpty(name)) {
            avatarIv.setVisibility(View.GONE);
            avatarTv.setText(String.valueOf(name.charAt(0)));

        } else {
            avatarIv.setVisibility(View.VISIBLE);
            avatarTv.setText("");
        }
    }

    private void showHelpTooltip() {
        isHelpTooltipShown = true;
        ViewTooltip.on(emailLy)
            .autoHide(true , 3500).clickToHide(true).align(ViewTooltip.ALIGN.START)
                .position(ViewTooltip.Position.BOTTOM).text(getString(R.string.account_email_help))
                .textColor(Color.WHITE).color(Color.DKGRAY).corner(50).padding(2,2,2,2)
                .animation(new ViewTooltip.FadeTooltipAnimation(300))
                .onHide(view -> isHelpTooltipShown = false).show();
    }

    private void loadData() {
        showShimmer();
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onGetCredentialsSuccess(CredentialData credentialData) {
                    loadedCredentialData = credentialData;
                    String name = credentialData.getName();
                    nameEt.setText(name);
                    updateAvatar(name);
                    emailEt.setText(credentialData.getEmail());

                    editApplyIv.setEnabled(true);
                    isDataRefreshing = false;
                    mSwipeRefreshLayout.setRefreshing(false);

                    hideShimmer();
                }

                @Override
                public void onFailure(int errorCode) {
                    editApplyIv.setEnabled(false);
                    placeholderLy.stopShimmer();

                    if (errorCode== Constants.RQM_EC.NO_INTERNET_CONNECTION) {
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
    }
    private void populateLocalData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser!=null) {
            TextView phoneTv = findViewById(R.id.account_phoneTv);
            phoneTv.setText(TextFormatter.formatPhone(currentUser.getPhoneNumber()));

            TextView joinedTv = findViewById(R.id.account_joinedTv);

            FirebaseUserMetadata md = currentUser.getMetadata();
            if (md!=null) {
                Calendar joinedCal = Calendar.getInstance();
                joinedCal.setTimeInMillis(md.getCreationTimestamp());

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                joinedTv.setText(String.format("%s %s", getString(R.string.account_joinedTv_label), sdf.format(joinedCal.getTime())));
                joinedTv.setVisibility(View.VISIBLE);
            }

        } else {
            //Todo: implement properly: add verify phone dialog
            showInfoSnackbar("Debug: Couldn't get firebase user", Snackbar.LENGTH_LONG);
        }

        TextView roleTv = findViewById(R.id.account_roleTv);
        roleTv.setText(String.format("%s %s", getString(R.string.home_role_change_toast), getString(role.toStringRes())));
    }

    private void saveData() {
        //validate entered data, save only if email is valid
        String email = emailEt.getText().toString().trim();
        if (emailLy.getError() == null && (email.isEmpty() || isEmailValid(email))) {
            //build credential data object
            boolean updated = false;
            CredentialData updatedCredentialData = new CredentialData();
            String name = nameEt.getText().toString().trim();
            if (!loadedCredentialData.getName().equals(name)) {
                updatedCredentialData.setName(name);
                updated = true;
            }

            if (!loadedCredentialData.getEmail().equals(email)) {
                updatedCredentialData.setEmail(email);
                updated = true;
            }

            if (updated) {
                editApplyIv.setEnabled(false);

                ProgressBar loadingPb = findViewById(R.id.account_loadingPb);
                loadingPb.setVisibility(View.VISIBLE);

                RequestBuilder rb = new RequestBuilder()
                    .setCallback(new RequestsCallback() {
                    @Override
                    public void onSetCredentialsSuccess(String string) {
                        toggleEditMode();
                        editApplyIv.setEnabled(true);
                        changesMade = true;

                        loadingPb.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        Drawable progressDrawable = loadingPb.getProgressDrawable().mutate();
                        progressDrawable.setColorFilter(Color.parseColor("#C62828"), android.graphics.PorterDuff.Mode.SRC_IN);
                        loadingPb.setProgressDrawable(progressDrawable);

                        if (errorCode== Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), Snackbar.LENGTH_LONG);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), Snackbar.LENGTH_LONG);
                        }
                    }
                });
                rb.checkInternet(this).attachToken(this, TOKEN_ACCESS);
                switch (role) {
                    case ATTENDEE: rb.callRequest(()->rb.attendeeSetCredentials(updatedCredentialData)); break;
                    case HOST: rb.callRequest(()->rb.hostSetCredentials(updatedCredentialData)); break;
                    case MANAGER: break;//FIX_FOR_MANAGER
                }

            } else {
                toggleEditMode();
            }
        } else {
            Toast.makeText(this, getString(R.string.account_bad_email), Toast.LENGTH_LONG).show();

        }
    }
    public boolean isEmailValid(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void toggleEditMode() {
        editMode=!editMode;

        int drawableRes;
        if (editMode) {
            drawableRes = R.drawable.ic_check_black_24dp;

        } else {
            drawableRes = R.drawable.ic_edit_black_24dp;
        }

        nameEt.setEnabled(editMode);
        emailEt.setEnabled(editMode);
        editApplyIv.setImageResource(drawableRes);

    }

    private void showShimmer() {
        placeholderLy.startShimmer();
        placeholderLy.setVisibility(View.VISIBLE);
        detailsLy.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        placeholderLy.setVisibility(View.GONE);
        detailsLy.setVisibility(View.VISIBLE);
    }

    private void showInfoSnackbar(String msg, int duration) {
        if (getWindow().getDecorView().isShown()) {
            if (!TextUtils.isEmpty(msg)) {
                Snackbar.make(findViewById(R.id.home_mainLy), msg, duration).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (editMode) {
            toggleEditMode();
            editApplyIv.setEnabled(false);
            populateLocalData();
            loadData();
            return;
        }

        finishActivity(false);
    }

    private void finishActivity(boolean isLogout) {
        Intent returnIntent = new Intent();
        if (isLogout) {
            returnIntent.putExtra("isLogout", true);
        }
        returnIntent.putExtra("updateNeeded", changesMade);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("wasInEditMode", editMode);
        if (editMode) {
            outState.putParcelable("loadedCredentialData", loadedCredentialData);
        }
        outState.putBoolean("changesMade", changesMade);

    }
}

