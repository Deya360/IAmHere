package com.sse.iamhere;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    TextView authenticationStateTv;
    TextView verificationStateTv;
    TextView signInStateTv;
    EditText verificationCodeEt;
    EditText authenticationPhoneEt;
    Button sendCodeBtn;
    Button signInBtn;
    Button signOutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authenticationStateTv = findViewById(R.id.authenticationStateTv);
        verificationStateTv = findViewById(R.id.verificationStateTv);
        signInStateTv = findViewById(R.id.signInStateTv);
        verificationCodeEt = findViewById(R.id.verificationCodeEt);
        authenticationPhoneEt = findViewById(R.id.authenticationPhoneEt);
        sendCodeBtn = findViewById(R.id.sendCodeBtn);
        signInBtn = findViewById(R.id.signInBtn);
        signOutBtn = findViewById(R.id.signOutBtn);

        sendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: add phone number validity check
                verifyPhone(authenticationPhoneEt.getText().toString());
                sendCodeBtn.setVisibility(View.GONE);
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthCredential credential =
                        PhoneAuthProvider.getCredential(authenticationPhoneEt.getText().toString(),
                                verificationCodeEt.getText().toString());
                signInWithPhoneAuthCredential(credential);
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutUser();
            }
        });

        initAuthListener();

//        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        Log.d("android_id", android_id);
    }

    private void signOutUser() {
        firebaseAuth.signOut();
    }

    private void initAuthListener() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser()==null) {
                    authenticationStateTv.setText("User Not Authenticated");
                    authenticationPhoneEt.setVisibility(View.VISIBLE);
                    sendCodeBtn.setVisibility(View.VISIBLE);
                    signOutBtn.setVisibility(View.GONE);
                    verificationStateTv.setText("");
                    signInStateTv.setText("");
                    Log.d("initAuthListener","User Not Authenticated");

                } else {
                    authenticationStateTv.setText("User Authenticated");
                    authenticationPhoneEt.setVisibility(View.GONE);
                    signOutBtn.setVisibility(View.VISIBLE);
                    Log.d("initAuthListener","User Authenticated");
                }
            }
        });
    }

    /* phoneNum format is : "+71231234567*/
    public void verifyPhone(String phoneNum) {
        /* WARNING: This only works when called on an actual phone, wont work on an emulator */
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNum,
                30,
                TimeUnit.SECONDS,
                this,
                vscc);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks vscc
            = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeEt.setVisibility(View.VISIBLE);
            signInBtn.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            verificationStateTv.setText("onCodeAutoRetrievalTimeOut");
            verificationCodeEt.setVisibility(View.GONE);
            signInBtn.setVisibility(View.GONE);
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential);
            verificationStateTv.setText("onVerificationCompleted");
            Log.d("verifyPhone","onVerificationCompleted");
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            verificationStateTv.setText("onVerificationFailed");
            Log.d("verifyPhone","onVerificationFailed", e);
        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();

                            signInStateTv.setText("User authenticated with: " + user.getUid() +
                                    "\n" + user.getPhoneNumber());
                            verificationCodeEt.setVisibility(View.GONE);
                            signInBtn.setVisibility(View.GONE);
                            Log.d("signInWithPhone", "signInWithCredential:success");

//                            updateUI(STATE_SIGNIN_SUCCESS, user);
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.d("signInWithPhone", "signInWithCredential:failure", task.getException());
                            signInStateTv.setText("User authenticated failed");

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Log.d("signInWithPhone", "signInWithCredential:failure:invalid code", task.getException());

                                signInStateTv.setText("User authenticated failed, wrong verification code");
                            }
//                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });
    }

}
