package com.sse.iamhere;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import static com.sse.iamhere.Utils.Constants.DEBUG_MODE;
import static com.sse.iamhere.Utils.Constants.RQM_EC.LOGIN_BAD_PHONE;
import static com.sse.iamhere.Utils.Constants.RQM_EC.LOGIN_BAD_ROLE;
import static com.sse.iamhere.Utils.Constants.RQM_EC.LOGIN_UNKNOWN;
import static com.sse.iamhere.Utils.Constants.RQM_EC.LOGIN_USER_NOT_FOUND;
import static com.sse.iamhere.Utils.Constants.RQM_EC.REGISTRATION_UNKNOWN;
import static com.sse.iamhere.Utils.Constants.RQM_EC.REGISTRATION_USER_EXISTS;
import static com.sse.iamhere.Utils.Constants.RQM_EC.TOKEN_STORE_FAIL;
import static com.sse.iamhere.Utils.Constants.TOKEN_NONE;
import static com.sse.iamhere.Utils.ServerConstants.DEBUG_PHONE;

public class AuthenticationActivity extends AppCompatActivity {
    private String phone;
    private String phoneFormatted;
    private boolean isRegistered = true;

    private boolean isProcessing = false;

    private ActionProcessButton continueBtn;
    private TextInputEditText passwordEt;
    private Spinner roleSp;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        if (savedInstanceState!=null) {
            phone = savedInstanceState.getString("phone");
            phoneFormatted = savedInstanceState.getString("phoneFormatted");
            isRegistered = savedInstanceState.getBoolean("isRegistered");

        } else {
            Bundle extras = getIntent().getExtras();
            if (extras==null) {
                Log.e("AuthenticationActivity", "getIntent().getExtras() is null onCreate");
                return;
            }

            phone = extras.getString("phone");
            isRegistered = extras.getBoolean("isRegistered");
            phoneFormatted = extras.getString("phoneFormatted");
        }


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
            subtitleTv.setText(getString(R.string.auth_subtitleTv_registration_label));
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
            subtitleTv.setText(getString(R.string.auth_subtitleTv_login_label));
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
    }

    private void onRegister () {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null || DEBUG_MODE) {
            String uuid, phoneNumber;
            if (!DEBUG_MODE) {
                uuid = user.getUid();
                phoneNumber = user.getPhoneNumber();

            } else {
                uuid = "Deya";
                phoneNumber = DEBUG_PHONE;
            }

            AsyncTask.execute(() -> {
                new RequestManager(this).attachToken(TOKEN_NONE)
                    .register(uuid, text(passwordEt.getText()), phoneNumber, getFormattedRole(getSelectedRole()))
                    .setCallback(new RequestCallback() {
                        @Override
                        public void onRegisterSuccess() {
                            continueBtn.setProgress(100);
                            isProcessing=false;

                            PreferencesUtil.setRole(AuthenticationActivity.this, getSelectedRole());

                            startHomeActivity();
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
                    .login(uuid, text(passwordEt.getText()), getFormattedRole(getSelectedRole()))
                    .setCallback(new RequestCallback() {
                        @Override
                        public void onLoginSuccess() {
                            continueBtn.setProgress(100);
                            isProcessing=false; //release lock (although not needed as activity will finish)

                            // Save role to prefs
                            PreferencesUtil.setRole(AuthenticationActivity.this, getSelectedRole());

                            startHomeActivity();
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

    private String getFormattedRole(Constants.Role role) {
        return role.toSerializedJSON();
    }

    private Constants.Role getSelectedRole() {
        return Constants.Role.values()[roleSp.getSelectedItemPosition()];
    }

    public void onRoleSetupDismiss(Constants.Role selectedRole) {
        roleSp.setSelection(selectedRole.toIdx());
    }

    private void startOnboardActivity() {
        Intent intent = new Intent(AuthenticationActivity.this, OnboardActivity.class);
        startActivity(intent);
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("phone", phone);
        outState.putString("phoneFormatted", phoneFormatted);
        outState.putBoolean("isRegistered", isRegistered);
    }
}
