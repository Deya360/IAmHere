package com.sse.iamhere;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sse.iamhere.Server.AuthRequestBuilder;
import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.InternetUtil;
import com.sse.iamhere.Utils.LocaleUtil;
import com.sse.iamhere.Utils.PreferencesUtil;
import com.sse.iamhere.Utils.TextFormatter;

import static com.sse.iamhere.Utils.Constants.DEBUG_MODE;
import static com.sse.iamhere.Utils.Constants.IS_FIRST_TIME;
import static com.sse.iamhere.Utils.ServerConstants.DEBUG_PHONE;

public class SplashActivity extends AppCompatActivity {
    private static final int AUTO_CONTINUE_TIME_DELAY = 1100;
    private static final int STARTUP_DELAY = 300;
    private static final int ITEM_DELAY = 450;
    private static int ANIM_ITEM_DURATION = 450;

    private FirebaseAuth firebaseAuth;
    private boolean isPhoneVerified;
    private boolean autoContinue = true;
    private boolean finished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleUtil.setConfigLang(this);
        setContentView(R.layout.activity_splash);

        initAuthListener();
    }

    private void initAuthListener() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(firebaseAuth -> {
            isPhoneVerified = firebaseAuth.getCurrentUser()!=null;

            if (!isPhoneVerified) {
                if (!PreferencesUtil.getPrefByName(getApplicationContext(), IS_FIRST_TIME, false)) {
                    PreferencesUtil.setPrefByName(getApplicationContext(), IS_FIRST_TIME, true);
                    autoContinue = false;
                }
            }
            initUi();
        });
    }

    private void initUi () {
        Button continueBtn = findViewById(R.id.splash_continueBtn);
        if (autoContinue) {
            continueBtn.setVisibility(View.GONE);

        } else {
            continueBtn.setVisibility(View.VISIBLE);
            continueBtn.setOnClickListener(v -> checkConnection());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            animate();

            if (autoContinue) {
                new Handler().postDelayed(this::checkConnection, AUTO_CONTINUE_TIME_DELAY);
            }

            super.onWindowFocusChanged(true);
        }
    }

    private void animate() {
        ViewCompat.animate(findViewById(R.id.splash_logoIv))
                .translationY(-250)
                .setStartDelay(STARTUP_DELAY)
                .setDuration(ANIM_ITEM_DURATION)
                .setInterpolator(new DecelerateInterpolator(1.2f)).start();

        ViewCompat.animate(findViewById(R.id.splash_logo_textIv))
                .translationY(50).alpha(1)
                .setStartDelay(STARTUP_DELAY)
                .setDuration(ANIM_ITEM_DURATION)
                .setInterpolator(new DecelerateInterpolator()).start();

        Button continueBtn = findViewById(R.id.splash_continueBtn);
        ViewCompat.animate(continueBtn)
                .scaleY(1).scaleX(1)
                .setStartDelay(STARTUP_DELAY)
                .setDuration(ANIM_ITEM_DURATION)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    private void checkConnection() {
        InternetUtil internetUtil = new InternetUtil(new InternetUtil.InternetResponse() {
            @Override
            public void isConnected() {
                finishSplashActivity();
            }

            @Override
            public void notConnected() {
                if (DEBUG_MODE) {
                    finishSplashActivity();
                    return;
                }

                ViewCompat.animate(findViewById(R.id.splash_connectionTv))
                        .translationY(50).alpha(1)
                        .setDuration(ANIM_ITEM_DURATION)
                        .setInterpolator(new DecelerateInterpolator()).start();

                Button retryBtn = findViewById(R.id.splash_retryBtn);
                retryBtn.setOnClickListener(v -> {
                            Toast.makeText(SplashActivity.this, getString(R.string.splash_connection_checking), Toast.LENGTH_LONG).show();
                            retryBtn.setEnabled(false);

                            new InternetUtil(new InternetUtil.InternetResponse() {
                                @Override
                                public void isConnected() {
                                    retryBtn.setEnabled(true);
                                    Toast.makeText(SplashActivity.this, getString(R.string.splash_connection_success), Toast.LENGTH_LONG).show();
                                    finishSplashActivity();
                                }

                                @Override
                                public void notConnected() {
                                    retryBtn.setEnabled(true);
                                    Toast.makeText(SplashActivity.this, getString(R.string.splash_connection_fail), Toast.LENGTH_LONG).show();
                                }
                            }).hasInternetConnection(SplashActivity.this);
                        }
                );

                ViewCompat.animate(retryBtn)
                        .scaleY(1).scaleX(1)
                        .setDuration(ANIM_ITEM_DURATION)
                        .setInterpolator(new DecelerateInterpolator()).start();
            }
        });
        internetUtil.hasInternetConnection(SplashActivity.this);
    }

    private void finishSplashActivity() {
        if (!finished) {
            finished=true;

            if (isPhoneVerified) {
                if (PreferencesUtil.isTokenAvailableForCurrentRole(this)) {
                    startHomeActivity();

                } else {
                    startAuthenticationActivity();
                }

            } else {
                if (DEBUG_MODE) {
                    PreferencesUtil.setRole(this, Constants.Role.HOST);
                    startHomeActivity();
                    return;
                }
                startVerificationActivity();
            }
        }
    }

    private void startVerificationActivity() {
        Intent intent = new Intent(SplashActivity.this, VerificationActivity.class);
        startActivity(intent);
        finish();
    }

    private void startAuthenticationActivity() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null || Constants.DEBUG_MODE) {
            String uuid;
            if (DEBUG_MODE) {
                uuid = "Deya";
            } else {
                uuid = currentUser.getUid();
            }

            AsyncTask.execute(() -> {
                new AuthRequestBuilder(this)
                    .setCallback(new RequestsCallback() {
                        @Override
                        public void onCheckSuccess(CheckData checkResult) {
                            super.onCheckSuccess(checkResult);
                            startAuthenticationActivity(checkResult.isRegistered());
                        }

                        @Override
                        public void onFailure(int errorCode) {
                            super.onFailure(errorCode);
                            Toast.makeText(SplashActivity.this, getString(R.string.msg_server_error), Toast.LENGTH_LONG).show();
                        }
                    })
                    .check(uuid).attachToken(Constants.TOKEN_NONE);
            });
        } else {
            Toast.makeText(SplashActivity.this, getString(R.string.msg_server_error), Toast.LENGTH_LONG).show();
        }
    }
    private void startAuthenticationActivity(boolean isRegistered) {
        Intent intent;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isRegistered", isRegistered);

        if (DEBUG_MODE) {
            bundle.putString("phone", DEBUG_PHONE);
            bundle.putString("phoneFormatted", TextFormatter.formatPhone(DEBUG_PHONE));

        } else {
            bundle.putString("phone", firebaseAuth.getCurrentUser().getPhoneNumber());
            bundle.putString("phoneFormatted", TextFormatter.formatPhone(firebaseAuth.getCurrentUser().getPhoneNumber()));
        }

        intent = new Intent(SplashActivity.this, AuthenticationActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void startHomeActivity() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
