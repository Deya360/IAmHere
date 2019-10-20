package com.sse.iamhere.Adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.redmadrobot.inputmask.MaskedTextChangedListener;
import com.redmadrobot.inputmask.helper.AffinityCalculationStrategy;
import com.sse.iamhere.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VerificationAdapter extends PagerAdapter {
    private Activity context;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks codeCallback;
    private OnCompleteListener<AuthResult> loginListener;

    private boolean debug = false; //TODO: remove debug
    private boolean isRegister = true;
    private String phone, phoneFormatted;

    private VerificationAdapterListener verificationAdapterListener;

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public interface VerificationAdapterListener {
        void onNext(int stageNo);
        void onPhoneValidated(String phone);
        void onVerificationCodeSent(String phoneFormatted);
        void onContinue(boolean isRegister, String phoneFormatted, String phone);
//        void onError(String error); //TODO: IMPLEMENT
    }

    public VerificationAdapter(Activity context, VerificationAdapterListener verificationAdapterListener) {
        this.context = context;
        this.verificationAdapterListener = verificationAdapterListener;
        initAuthListener();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view==o);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View view = null;

        if (position==0) {
            view = inflater.inflate(R.layout.item_verifi_phone, container, false);

            final Button continueBtn = view.findViewById(R.id.verifi_phone_continueBtn);

            final TextInputEditText phoneEt = view.findViewById(R.id.verifi_phone_phoneEt);
            List<String> affineFormats = new ArrayList<>();

            phoneEt.setHint("+7 (XXX) XXX-XX-XX");
            final MaskedTextChangedListener listener = MaskedTextChangedListener.Companion.installOn(
                    phoneEt,
                    "+7 ([000]) [000]-[00]-[00]",
                    affineFormats,
                    AffinityCalculationStrategy.WHOLE_STRING,
                    (maskFilled, extractedValue, formattedText) -> {
                        continueBtn.setEnabled(maskFilled);
                        if (maskFilled) {
                            phoneEt.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_check_green_24dp, 0 );
                        } else {
                            phoneEt.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0, 0 );
                        }
                    }
            );
            listener.setAutocomplete(false);
            phoneEt.setText("");
            phoneEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    listener.setAutocomplete(true);
                }
            });
            phoneEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(phoneEt, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            continueBtn.setOnClickListener(v -> {
                phoneFormatted = phoneEt.getText().toString();
                phone = phoneFormatted.replaceAll("[\\s()-]", "");

                verificationAdapterListener.onNext(position+1);
                verificationAdapterListener.onPhoneValidated(phone);
                verifyPhone(phone, codeCallback);
            });


        } else if (position==1) {
            view = inflater.inflate(R.layout.item_verifi_code, container, false);

            final Button continueBtn2 = view.findViewById(R.id.verifi_code_continueBtn);

            final TextInputEditText otpCodeEt = view.findViewById(R.id.verifi_code_otpEt);
            TextView autoCompleteTv = view.findViewById(R.id.verifi_code_autocompleteTv);
            codeCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    verificationAdapterListener.onVerificationCodeSent(phoneFormatted);
//                            final Timer timer = new Timer();
//                            timer.schedule(new TimerTask() {
//                                int counter = 120;
//                                @Override
//                                public void run() {
//                                    if (counter >= 0) {
//                                        timeoutCounterTv.setText("Verification code expires in: " + counter + "s");
//                                        counter--;
//                                    } else {
//                                        timeoutCounterTv.setText("Verification code expires in: " + counter + "s");
//                                    }
//                                    timer.cancel();
//                                }
//                            }, 0, 1000);
                }

                @Override
                public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                    super.onCodeAutoRetrievalTimeOut(s);
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    Log.d("verifyPhone","onVerificationCompleted");
                    autoCompleteTv.setVisibility(View.VISIBLE);
                    if (phoneAuthCredential.getSmsCode()!=null) {
                        otpCodeEt.setText(phoneAuthCredential.getSmsCode());
                        otpCodeEt.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_check_green_24dp, 0 );
                        otpCodeEt.setEnabled(false);

                    } else {
                        otpCodeEt.setVisibility(View.GONE);
                    }
                    continueBtn2.setEnabled(true);
                    signInWithPhoneAuthCredential(phoneAuthCredential, loginListener);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.d("verifyPhone","onVerificationFailed", e);
                    //TODO: ERROR SNACKBAR
                }
            };

            loginListener = task -> {
                if (task.isSuccessful() || debug) {
                    Log.d("signInWithPhone", "signInWithCredential:success");
                    if (!debug) {
                        Log.d("NEW USER?!", "onComplete: " +
                                (task.getResult().getAdditionalUserInfo().isNewUser() ? "new user" : "old user"));
                        FirebaseUser user = task.getResult().getUser();
                        isRegister = task.getResult().getAdditionalUserInfo().isNewUser();
                    }

                    otpCodeEt.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_check_green_24dp, 0 );
                    otpCodeEt.setError(null);
                    continueBtn2.setEnabled(true);

                } else {  // Sign in failed, display a message and update the UI
                    Log.d("signInWithPhone", "signInWithCredential:failure", task.getException());

                    // The verification code entered was invalid
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Log.d("signInWithPhone", "signInWithCredential:failure:invalid code", task.getException());
                        otpCodeEt.setError("Wrong Code");

                    } else {
                        //TODO: ERROR SNACKBAR
                    }
                }
            };

            List<String> affineFormats = new ArrayList<>();
            affineFormats.add("[000000]");

            final MaskedTextChangedListener listener = MaskedTextChangedListener.Companion.installOn(
                otpCodeEt,
                "[000000]",
                affineFormats,
                AffinityCalculationStrategy.WHOLE_STRING,
                (maskFilled, extractedValue, formattedText) -> {
                    otpCodeEt.setError(null);
                    if (maskFilled) {
                        signInWithPhoneAuthCredential(
                            PhoneAuthProvider.getCredential(phone, extractedValue), loginListener);
                    }
                }
            );

            otpCodeEt.setHint("");
            otpCodeEt.requestFocus();
            otpCodeEt.setText("");

            continueBtn2.setOnClickListener(v -> {
                verificationAdapterListener.onContinue(isRegister, phoneFormatted, phone);
            });
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }



    /*
    * Firebase
    * */
    private void initAuthListener() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(firebaseAuth -> {
            if (firebaseAuth.getCurrentUser()==null) {
                Log.d("initAuthListener","User Not Authenticated");

            } else {
                Log.d("initAuthListener","User Authenticated");
            }
        });
    }

    /* phoneNum format is : "+71231234567*/
    public void verifyPhone(String phoneNum, PhoneAuthProvider.OnVerificationStateChangedCallbacks changedCallbacks) {
        /* WARNING: This only works when called on an actual phone, wont work on an emulator */
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNum,
                60,
                TimeUnit.SECONDS,
                context,
                changedCallbacks);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, OnCompleteListener<AuthResult> listener) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(context, listener);
    }

    private void signOutUser() {
        firebaseAuth.signOut();
    }
}

