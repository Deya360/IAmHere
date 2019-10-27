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
import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.RequestCallback;
import com.sse.iamhere.Server.RequestManager;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.InternetUtil;
import com.sse.iamhere.Utils.PreferencesUtil;

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
        InternetUtil internetUtil = new InternetUtil(connected -> {
            if (connected || DEBUG_MODE) {
                finishSplashActivity();

            } else {
                ViewCompat.animate(findViewById(R.id.splash_connectionTv))
                    .translationY(50).alpha(1)
                    .setDuration(ANIM_ITEM_DURATION)
                    .setInterpolator(new DecelerateInterpolator()).start();

                Button retryBtn = findViewById(R.id.splash_retryBtn);
                retryBtn.setOnClickListener(v -> {
                        Toast.makeText(SplashActivity.this, "Checking...", Toast.LENGTH_LONG).show();
                        retryBtn.setEnabled(false);

                        new InternetUtil(connected1 -> {
                            retryBtn.setEnabled(true);
                            if (connected1) {
                                Toast.makeText(SplashActivity.this, "Connected!", Toast.LENGTH_LONG).show();
                                finishSplashActivity();

                            } else {
                                Toast.makeText(SplashActivity.this, "No Connection", Toast.LENGTH_LONG).show();
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
                try {
                    if (PreferencesUtil.getTokenData(this)!=null) {
                        startHomeActivity();
                    } else {
                        startAuthenticationActivity();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
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
                new RequestManager(this).check(uuid).attachToken(Constants.TOKEN_NONE)
                    .setCallback(new RequestCallback() {
                        @Override
                        public void onCheckSuccess(CheckData checkResult) {
                            startAuthenticationActivity(checkResult.isRegistered());
                        }

                        @Override
                        public void onCheckFailure(int errorCode) {
                            Toast.makeText(SplashActivity.this, "Internal Server Error :(", Toast.LENGTH_LONG).show();
                            //todo: implement properly
                        }
                    });
            });
        } else {
            Toast.makeText(SplashActivity.this, "Couldn't connect to server", Toast.LENGTH_LONG).show();
            //todo: implement properly
        }

    }
    private void startAuthenticationActivity(boolean isRegistered) {
        Intent intent;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isRegistered", isRegistered);

        if (DEBUG_MODE) {
            bundle.putString("phone", DEBUG_PHONE);
            bundle.putString("phoneFormatted", DEBUG_PHONE);

        } else {
            bundle.putString("phone", firebaseAuth.getCurrentUser().getPhoneNumber());
            bundle.putString("phoneFormatted", firebaseAuth.getCurrentUser().getPhoneNumber()); //TODO: format properly
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
