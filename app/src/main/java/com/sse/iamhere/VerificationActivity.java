package com.sse.iamhere;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.sse.iamhere.Adapters.VerificationAdapter;
import com.sse.iamhere.Views.NoSwipeViewPager;

public class VerificationActivity extends AppCompatActivity {
    private Handler timeoutCounter = new Handler();
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
//            viewPager.setCurrentItem(verificationStage);
        }



//        sendCodeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //TOD: add phone number validity check
//                verifyPhone(authenticationPhoneEt.getText().toString());
//                sendCodeBtn.setVisibility(View.GONE);
//
//            }
//        });

//        signInBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PhoneAuthCredential credential =
//                        PhoneAuthProvider.getCredential(authenticationPhoneEt.getText().toString(),
//                                verificationCodeEt.getText().toString());
//                signInWithPhoneAuthCredential(credential);
//            }
//        });

//        signOutBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signOutUser();
//            }
//        });



//        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        Log.d("android_id", android_id);
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
                        "Verification code sent to " + phoneFormatted, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onContinue(boolean isRegister, String phoneFormatted, String phone) {
                startAuthActivity(isRegister, phoneFormatted, phone);
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
