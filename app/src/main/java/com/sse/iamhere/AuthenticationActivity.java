package com.sse.iamhere;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sse.iamhere.Server.RequestCallback;
import com.sse.iamhere.Server.RequestManager;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;
import com.sse.iamhere.Views.OnSingleClickListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static com.sse.iamhere.Utils.Constants.AUTHENTICATION_RELOG_RQ;
import static com.sse.iamhere.Utils.Constants.AUTHENTICATION_RQ;
import static com.sse.iamhere.Utils.Constants.DEBUG_MODE;
import static com.sse.iamhere.Utils.Constants.RQM_EC.LOGIN_BAD_PHONE;
import static com.sse.iamhere.Utils.Constants.RQM_EC.LOGIN_BAD_ROLE;
import static com.sse.iamhere.Utils.Constants.RQM_EC.LOGIN_UNKNOWN;
import static com.sse.iamhere.Utils.Constants.RQM_EC.LOGIN_USER_NOT_FOUND;
import static com.sse.iamhere.Utils.Constants.RQM_EC.REGISTRATION_UNKNOWN;
import static com.sse.iamhere.Utils.Constants.RQM_EC.REGISTRATION_USER_EXISTS;
import static com.sse.iamhere.Utils.Constants.RQM_EC.TOKEN_STORE_FAIL;
import static com.sse.iamhere.Utils.Constants.TOKEN_NONE;

public class AuthenticationActivity extends AppCompatActivity {
    private String phone;
    private String phoneFormatted;
    private boolean isRegistered = true;

    private int forceRole = -1;
    private String customDescription = null;
    private boolean showAsDialog = false;
    private boolean disallowCancel = false;
    private int returnRequestCode = -1;
    private int activityOrientation = ORIENTATION_PORTRAIT;

    private boolean isProcessing = false;

    private ActionProcessButton continueBtn;
    private TextInputEditText passwordEt;
    private Spinner roleSp;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState!=null) {
            phone = savedInstanceState.getString("phone");
            phoneFormatted = savedInstanceState.getString("phoneFormatted");
            isRegistered = savedInstanceState.getBoolean("isRegistered");

            showAsDialog = savedInstanceState.getBoolean("showAsDialog");
            forceRole = savedInstanceState.getInt("forceRole");
            customDescription = savedInstanceState.getString("customDescription");
            returnRequestCode = savedInstanceState.getInt("returnRequestCode");
            activityOrientation = savedInstanceState.getInt("activityOrientation");
            disallowCancel = savedInstanceState.getBoolean("disallowCancel");

        } else {
            Bundle extras = getIntent().getExtras();
            if (extras==null) {
                Log.e("AuthenticationActivity", "getIntent().getExtras() is null onCreate");
                return;
            }

            phone = extras.getString("phone");
            isRegistered = extras.getBoolean("isRegistered");
            phoneFormatted = extras.getString("phoneFormatted");

            showAsDialog = extras.getBoolean("showAsDialog", showAsDialog);
            forceRole = extras.getInt("forceRole", forceRole);
            customDescription = extras.getString("customDescription", customDescription);
            returnRequestCode = extras.getInt("returnRequestCode", returnRequestCode);
            activityOrientation = extras.getInt("activityOrientation", activityOrientation);
        }

        if (showAsDialog) {
            setTheme(R.style.ActivityDialog);
        }

        if (activityOrientation==ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        } else if (activityOrientation==ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        } else throw new RuntimeException("Authentication Activity: supplied bad orientation");

        if (disallowCancel) {
            this.setFinishOnTouchOutside(false);
        }

        setContentView(R.layout.activity_authentication);


        TextView phoneNumberTv = findViewById(R.id.auth_phone_numberTv);
        phoneNumberTv.setText(phoneFormatted);

        TextView titleTv = findViewById(R.id.auth_titleTv);
        TextView subtitleTv = findViewById(R.id.auth_subtitleTv);

        continueBtn = findViewById(R.id.auth_continueBtn);
        TextView instructionsTv = findViewById(R.id.auth_instructionsTv);

        TextInputLayout passwordLy = findViewById(R.id.auth_passwordLy);
        passwordEt = findViewById(R.id.auth_passwordEt);
        passwordEt.requestFocus();

        roleSp = findViewById(R.id.auth_roleSp);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new ArrayList<String>() {{
                    add(getString(R.string.auth_roleSp_label));
                    add(getString(R.string.onboard_attendee_label));
                    add(getString(R.string.onboard_host_label));
                    add(getString(R.string.onboard_manager_label));
                }});
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSp.setAdapter(dataAdapter);
        roleSp.setSelection(0);

        if (!isRegistered) {
            titleTv.setText(getString(R.string.auth_titleTv_registration_label));
            if (customDescription!=null) {
                subtitleTv.setText(customDescription);
            } else {
                subtitleTv.setText(getString(R.string.auth_subtitleTv_registration_label));
            }
            continueBtn.setText(getString(R.string.auth_continueBtn_registration_label_normal));
            continueBtn.setLoadingText(getString(R.string.auth_continueBtn_registration_label_progress));
            continueBtn.setCompleteText(getString(R.string.auth_continueBtn_registration_label_complete));

            passwordEt.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    if (TextUtils.isEmpty(text(passwordEt.getText()))) {
                        passwordLy.setError(getString(R.string.auth_validation_empty));

                    } else if (!isValidPassword(text(passwordEt.getText()))) {
                        passwordLy.setError(getString(R.string.auth_validation_invalid));
                        instructionsTv.setVisibility(View.VISIBLE);

                    } else {
                        passwordLy.setError(null);
                        instructionsTv.setVisibility(View.GONE);
                    }
                } else {
                    instructionsTv.setVisibility(View.VISIBLE);
                }
            });

            TextInputLayout passwordAgainLy = findViewById(R.id.auth_password_againLy);
            passwordAgainLy.setVisibility(View.VISIBLE);

            TextInputEditText passwordAgainEt = findViewById(R.id.auth_password_againEt);
            passwordAgainEt.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    if (TextUtils.isEmpty(text(passwordAgainEt.getText()))) {
                        passwordAgainLy.setError(getString(R.string.auth_validation_empty));

                    } else if (!text(passwordEt.getText()).equals(text(passwordAgainEt.getText()))) {
                        passwordAgainLy.setError(getString(R.string.auth_validation_mismatch));

                    } else {
                        passwordAgainLy.setError(null);
                        instructionsTv.setVisibility(View.GONE);
                    }
                }
            });

            ImageView roleHelpIv = findViewById(R.id.auth_role_helpIv);
            roleHelpIv.setVisibility(View.VISIBLE);
            roleHelpIv.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    startOnboardActivity();
                }
            });

            continueBtn.setOnClickListener(v -> {
                if (isProcessing) return;

                passwordEt.clearFocus();
                passwordAgainEt.clearFocus();
                if (passwordLy.getError()==null && passwordAgainLy.getError()==null
                        && getSelectedRole()!= Constants.Role.NONE
                        && text(passwordEt.getText()).equals(text(passwordAgainEt.getText()))) {
                    continueBtn.setProgress(5);
                    isProcessing = true;
                    onRegister();

                } else if (passwordEt.getText()!=null && passwordLy.getError()==null && passwordAgainLy.getError()==null
                        && getSelectedRole()==Constants.Role.NONE) {
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.auth_bad_role), Snackbar.LENGTH_SHORT).show();

                } else if (!text(passwordEt.getText()).equals(text(passwordAgainEt.getText()))) {
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.auth_validation_mismatch), Snackbar.LENGTH_SHORT).show();

                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.auth_bad_input), Snackbar.LENGTH_SHORT).show();
                }
            });

        } else { // Log in
            titleTv.setText(getString(R.string.auth_titleTv_login_label));
            if (customDescription!=null) {
                subtitleTv.setText(customDescription);
            } else {
                subtitleTv.setText(getString(R.string.auth_subtitleTv_login_label));
            }
            continueBtn.setText(getString(R.string.auth_continueBtn_login_label));
            continueBtn.setLoadingText(getString(R.string.auth_continueBtn_login_label_progress));
            continueBtn.setCompleteText(getString(R.string.auth_continueBtn_login_label_complete));
            instructionsTv.setVisibility(View.GONE);

            passwordEt.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    if (TextUtils.isEmpty(text(passwordEt.getText()))) {
                        passwordLy.setError(getString(R.string.auth_validation_empty));

                    } else {
                        passwordLy.setError(null);
                    }
                }
            });

            continueBtn.setOnClickListener(v -> {
                if (isProcessing) return;

                passwordEt.clearFocus();
                if (passwordLy.getError()==null && getSelectedRole()!= Constants.Role.NONE) {
                    continueBtn.setProgress(5);
                    isProcessing = true;
                    onLogin();

                } else if (passwordLy.getError()==null && getSelectedRole()==Constants.Role.NONE) {
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.auth_bad_role), Snackbar.LENGTH_SHORT).show();

                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.auth_bad_input), Snackbar.LENGTH_SHORT).show();
                }
            });
        }

        if (forceRole!=-1) {
            roleSp.setSelection(forceRole);
            roleSp.setEnabled(false);
        }
    }

    private void onRegister () {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null || DEBUG_MODE) {
            String uuid;
            if (!DEBUG_MODE) {
                uuid = user.getUid();

            } else {
                uuid = "Deya";
            }

            AsyncTask.execute(() -> {
                new RequestManager(this).attachToken(TOKEN_NONE)
                    .register(uuid, text(passwordEt.getText()), getSelectedRole())
                    .setCallback(new RequestCallback() {
                        @Override
                        public void onRegisterSuccess() {
                            continueBtn.setProgress(100);
                            isProcessing=false;

                            PreferencesUtil.setRole(AuthenticationActivity.this, getSelectedRole());

                            finishActivity(true);
                        }

                        @Override
                        public void onRegisterFailure(int errorCode) {
                            continueBtn.setProgress(0);
                            continueBtn.setText(getString(R.string.auth_titleTv_registration_label));
                            isProcessing=false;

                            String msg;
                            switch (errorCode) {
                                case REGISTRATION_USER_EXISTS:
                                    msg = getString(R.string.auth_registration_user_exists);
                                    break;

                                case REGISTRATION_UNKNOWN:
                                    msg = getString(R.string.auth_registration_unknown);
                                    break;

                                case TOKEN_STORE_FAIL:
                                    msg = "Internal Error: Failed to store Auth data";
                                    break;

                                default:
                                case LOGIN_BAD_ROLE:
                                case LOGIN_BAD_PHONE:
                                    msg = "An internal error occurred: " + errorCode;
                            }

                            Snackbar.make(findViewById(android.R.id.content),
                                    msg, Snackbar.LENGTH_INDEFINITE).show();
                        }
                    });
            });
        } else {
            isProcessing=false;
            //todo: implement properly
        }
    }
    private void onLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null || DEBUG_MODE) {
            String uuid;
            if (!DEBUG_MODE) {
                uuid = user.getUid();
            } else {
                uuid = "Deya";
            }
            AsyncTask.execute(() -> {
                new RequestManager(this).attachToken(TOKEN_NONE)
                    .login(uuid, text(passwordEt.getText()), getSelectedRole())
                    .setCallback(new RequestCallback() {
                        @Override
                        public void onLoginSuccess() {
                            continueBtn.setProgress(100);
                            isProcessing=false; //release lock (although not needed as activity will finish)

                            // Save role to prefs
                            PreferencesUtil.setRole(AuthenticationActivity.this, getSelectedRole());

                            finishActivity(true);
                        }

                        @Override
                        public void onLoginFailure(int errorCode) {
                            // reset button ui
                            continueBtn.setProgress(0);
                            continueBtn.setText(getString(R.string.auth_titleTv_login_label));

                            isProcessing=false; //release lock (although not needed as activity will finish)

                            String msg;
                            switch (errorCode) {
                                case LOGIN_USER_NOT_FOUND:
                                    msg = getString(R.string.auth_login_wrong_password);
                                    break;

                                case LOGIN_UNKNOWN:
                                    msg = getString(R.string.auth_login_unknown);
                                    break;

                                case TOKEN_STORE_FAIL:
                                    msg = "Internal Error: Failed to store Auth data";
                                    break;

                                default:
                                case LOGIN_BAD_ROLE:
                                case LOGIN_BAD_PHONE:
                                    msg = "An internal error occurred: " + errorCode;
                            }

                            Snackbar.make(findViewById(android.R.id.content),
                                    msg, Snackbar.LENGTH_INDEFINITE).show();
                        }
                    });
            });
        } else {
            isProcessing=false;
            //TODO: implement
        }
    }

    private String text(Editable editable) {
        return (editable==null)? "" : editable.toString();
    }

    private Constants.Role getSelectedRole() {
        return Constants.Role.values()[roleSp.getSelectedItemPosition()];
    }

    public void onRoleSetupDismiss(Constants.Role selectedRole) {
        roleSp.setSelection(selectedRole.toIdx());
    }

    private void startOnboardActivity() {
        Intent intent = new Intent(AuthenticationActivity.this, OnboardActivity.class);
        intent.putExtra("activityOrientation", activityOrientation);
        startActivity(intent);
    }

    private void finishActivity(boolean goodOutcome) {
        if (getCallingActivity()!=null) {
            if (returnRequestCode==AUTHENTICATION_RQ) {
                Intent returnIntent = new Intent();
                setResult((goodOutcome)? Activity.RESULT_OK : Activity.RESULT_CANCELED, returnIntent);
                if (goodOutcome) {
                    returnIntent.putExtra("role", forceRole);
                }
                finish();

            } else if (returnRequestCode==AUTHENTICATION_RELOG_RQ) {
                Intent returnIntent = new Intent();
                setResult((goodOutcome)? Activity.RESULT_OK : Activity.RESULT_CANCELED, returnIntent);
                finish();
            }

        } else {
            startHomeActivity();
        }
    }

    private void startHomeActivity() {
        Intent intent = new Intent(AuthenticationActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean isValidPassword(final String password) {
        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getCallingActivity()!=null && !disallowCancel) {
            finishActivity(false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("phone", phone);
        outState.putString("phoneFormatted", phoneFormatted);
        outState.putBoolean("isRegistered", isRegistered);

        outState.putBoolean("showAsDialog", showAsDialog);
        outState.putInt("forceRole", forceRole);
        outState.putString("customDescription", customDescription);
        outState.putInt("returnRequestCode", returnRequestCode);
        outState.putInt("activityOrientation", activityOrientation);
        outState.putBoolean("disallowCancel", disallowCancel);
    }
}
