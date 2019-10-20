package com.sse.iamhere;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.sse.iamhere.Adapters.VerificationAdapter;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Views.NoSwipeViewPager;

public class VerificationActivity extends AppCompatActivity {
    private int verificationStage = 0;

    private String phone;

    private NoSwipeViewPager viewPager;
    private VerificationAdapter verificationAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        if (savedInstanceState!=null) {
            verificationStage = savedInstanceState.getInt("verificationStage");
        }


        initViewPager();

        if (savedInstanceState!=null) {
            phone = savedInstanceState.getString("phone","");
            verificationAdapter.setPhone(phone);
            if (!phone.isEmpty()) showPhoneTv();
        }
    }

    private void initViewPager() {
        viewPager = findViewById(R.id.verifi_inputVp);

        verificationAdapter = new VerificationAdapter(this, new VerificationAdapter.VerificationAdapterListener() {
            @Override
            public void onNext(int stageNo) {
                verificationStage = stageNo;
                viewPager.setCurrentItem(stageNo);
            }

            @Override
            public void onPhoneValidated(String phoneN) {
                phone = phoneN;
                showPhoneTv();
            }

            @Override
            public void onVerificationCodeSent(String phoneFormatted) {
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.verifi_on_otp_sent) + phoneFormatted, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onContinue(boolean isRegister, String phoneFormatted, String phone) {
                startAuthActivity(isRegister, phoneFormatted, phone);
            }

            @Override
            public void onError(int errorCode) {
                    switch (errorCode) {
                        case Constants.VerifiEC.VERIFICATION_FAILED:
                        case Constants.VerifiEC.SIGNIN_FAILED:
                            Snackbar.make(findViewById(android.R.id.content),
                                    getString(R.string.verifi_on_error), Snackbar.LENGTH_LONG).show();
                            break;
                    }
            }

        });
        viewPager.setAdapter(verificationAdapter);
    }

    private void startAuthActivity(boolean isRegister, String phoneFormatted, String phoneLocal) {
        Bundle bundle = new Bundle();
        bundle.putString("phone", phoneLocal);
        bundle.putString("phoneFormatted", phoneFormatted);
        bundle.putBoolean("isRegister", isRegister);

        Intent intent = new Intent(VerificationActivity.this, AuthenticationActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    void showPhoneTv() {
        TextView phoneNoEt = findViewById(R.id.verifi_phone_numberTv);
        phoneNoEt.setText(phone);
        phoneNoEt.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("verificationStage", verificationStage);
        outState.putString("phone", phone);
    }
}
