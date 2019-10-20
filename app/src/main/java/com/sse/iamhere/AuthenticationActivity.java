package com.sse.iamhere;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sse.iamhere.Data.Entitites.Account;
import com.sse.iamhere.Data.VM.AccountViewModel;
import com.sse.iamhere.Utils.PreferencesUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.sse.iamhere.Utils.Constants.ACCOUNT_ID;
import static com.sse.iamhere.Utils.Constants.IS_AUTHORIZED;

public class AuthenticationActivity extends AppCompatActivity {
    private String phone;
    private String phoneFormatted;
    private boolean isRegister = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        if (savedInstanceState!=null) {
            phone = savedInstanceState.getString("phone");
            phoneFormatted = savedInstanceState.getString("phoneFormatted");
            isRegister = savedInstanceState.getBoolean("isRegister");

        } else {
            Bundle extras = getIntent().getExtras();
            if (extras==null) {
                Log.e("AuthenticationActivity", "getIntent().getExtras() is null onCreate");
                return;
            }

            phone = extras.getString("phone");
            isRegister = extras.getBoolean("isRegister");
            phoneFormatted = extras.getString("phoneFormatted");
        }


        TextView phoneNumberTv = findViewById(R.id.auth_phone_numberTv);
        phoneNumberTv.setText(phoneFormatted);

        TextView titleTv = findViewById(R.id.auth_titleTv);
        TextView subtitleTv = findViewById(R.id.auth_subtitleTv);

        Button continueBtn = findViewById(R.id.auth_continueBtn);
        TextView instructionsTv = findViewById(R.id.auth_instructionsTv);

        TextInputLayout passwordLy = findViewById(R.id.auth_passwordLy);
        TextInputEditText passwordEt = findViewById(R.id.auth_passwordEt);
        passwordEt.requestFocus();

        if (isRegister) {
            titleTv.setText(getString(R.string.auth_titleTv_registration_label));
            subtitleTv.setText(getString(R.string.auth_subtitleTv_registration_label));
            continueBtn.setText(getString(R.string.auth_continueBtn_registration_label));

            passwordEt.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    if (passwordEt.getText()==null || TextUtils.isEmpty(passwordEt.getText().toString())) {
                        passwordLy.setError(getString(R.string.auth_validation_empty));

                    } else if (!isValidPassword(passwordEt.getText().toString())) {
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
                    if (passwordAgainEt.getText()==null || TextUtils.isEmpty(passwordAgainEt.getText().toString())) {
                        passwordAgainLy.setError(getString(R.string.auth_validation_empty));

                    } else if (passwordEt.getText()==null || !passwordEt.getText().toString().equals(passwordAgainEt.getText().toString())) {
                        passwordAgainLy.setError(getString(R.string.auth_validation_mismatch));

                    } else {
                        passwordAgainLy.setError(null);
                        instructionsTv.setVisibility(View.GONE);
                    }
                }
            });

            continueBtn.setOnClickListener(v -> {
                passwordEt.clearFocus();
                passwordAgainEt.clearFocus();
                if (passwordLy.getError()==null && passwordAgainLy.getError()==null) {
                    AccountViewModel accountViewModel = ViewModelProviders.of(AuthenticationActivity.this)
                            .get(AccountViewModel.class);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Account account = new Account(
                            user.getUid(), user.getPhoneNumber(), passwordEt.getText().toString(), "NONE");

                    accountViewModel.insert(account)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<long[]>() {
                            @Override
                            public void onSuccess(long[] longs) {
                                if (longs!=null) {
                                    PreferencesUtil.setPrefByName(getApplicationContext(), ACCOUNT_ID,
                                            String.valueOf(longs[0]));

                                    startWelcomeActivity();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                            }
                        });

                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.auth_bad_input), Snackbar.LENGTH_SHORT).show();
                }
            });

        } else { // Log in
            titleTv.setText(getString(R.string.auth_titleTv_login_label));
            subtitleTv.setText(getString(R.string.auth_subtitleTv_login_label));
            continueBtn.setText(getString(R.string.auth_continueBtn_login_label));
            instructionsTv.setVisibility(View.GONE);

            passwordEt.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    if (passwordEt.getText()==null || TextUtils.isEmpty(passwordEt.getText().toString())) {
                        passwordLy.setError(getString(R.string.auth_validation_empty));

                    } else {
                        passwordLy.setError(null);
                    }
                }
            });

            continueBtn.setOnClickListener(v -> {
                passwordEt.clearFocus();
                if (passwordLy.getError()==null) {
                    AccountViewModel accountViewModel = ViewModelProviders.of(AuthenticationActivity.this)
                            .get(AccountViewModel.class);

                    String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    accountViewModel.getAccountByUUID(uuid)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableSingleObserver<List<Account>>() {
                                @Override
                                public void onSuccess(List<Account> accounts) {
                                    if (accounts.get(0).getPassword().equals(passwordEt.getText().toString())) {
                                        PreferencesUtil.setPrefByName(getApplicationContext(), ACCOUNT_ID,
                                                String.valueOf(accounts.get(0).getId()));
                                        startWelcomeActivity();

                                    } else {
                                        Snackbar.make(findViewById(android.R.id.content),
                                                getString(R.string.auth_wrong), Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {

                                }
                            });

                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.auth_bad_input), Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startWelcomeActivity() {
        PreferencesUtil.setPrefByName(getApplicationContext(), IS_AUTHORIZED, true);
        Intent intent = new Intent(AuthenticationActivity.this, SetupActivity.class);
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
        outState.putBoolean("isRegister", isRegister);
    }
}
