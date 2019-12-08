package com.sse.iamhere.Adapters;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
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
import androidx.core.os.ConfigurationCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.dd.processbutton.iml.ActionProcessButton;
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
import com.sse.iamhere.Server.AuthRequestBuilder;
import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.sse.iamhere.Utils.Constants.DEBUG_MODE;
import static com.sse.iamhere.Utils.Constants.OTP_TIMEOUT;

public class VerificationAdapter extends PagerAdapter {
    private Activity context;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks codeCallback;
    private OnCompleteListener<AuthResult> loginListener;
    private Timer timer = new Timer();

    private String phone, phoneFormatted;

    private VerificationAdapterListener verificationAdapterListener;

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public interface VerificationAdapterListener {
        void onNext(int stageNo);
        void onPhoneValidated(String phone);
        void onVerificationCodeSent(String phoneFormatted);
        void onContinue(boolean isRegistered, String phoneFormatted, String phone);
        void onError(int errorCode);
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
        View view;

        if (position==0) {
            view = inflater.inflate(R.layout.item_verifi_phone, container, false);
            setupPhoneInput(view, position);

        } else if (position==1) {
            view = inflater.inflate(R.layout.item_verifi_code, container, false);
            setupOTPInput(view);

        } else {
            throw new RuntimeException("Verification Adapter position should never be anything but 0 or 1");
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    private void setupPhoneInput(View view, int position) {
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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                listener.setAutocomplete(true);
            }
        });
        phoneEt.setOnFocusChangeListener((v, hasFocus) -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(phoneEt, InputMethodManager.SHOW_IMPLICIT);
        });

        continueBtn.setOnClickListener(v -> {
            phoneFormatted = phoneEt.getText().toString();
            phone = phoneFormatted.replaceAll("[\\s()-]", "");

            verificationAdapterListener.onNext(position+1);
            verificationAdapterListener.onPhoneValidated(phone);
            verifyPhone(phone, codeCallback);
        });
    }
    private void setupOTPInput(View view) {
        ActionProcessButton continueBtn2 = view.findViewById(R.id.verifi_code_continueBtn);
        continueBtn2.setMode(ActionProcessButton.Mode.ENDLESS);

        TextView autoCompleteTv = view.findViewById(R.id.verifi_code_autocompleteTv);
        TextView otpExpiredTv = view.findViewById(R.id.verifi_code_otp_expiredTv);
        TextInputEditText otpCodeEt = view.findViewById(R.id.verifi_code_otpEt);

        Button resendBtn = view.findViewById(R.id.verifi_code_resendBtn);
        resendBtn.setOnClickListener(v -> {
            verifyPhone(phone, codeCallback);
            resendBtn.setVisibility(View.GONE);
            otpCodeEt.setVisibility(View.VISIBLE);
            otpExpiredTv.setEnabled(false);
            otpExpiredTv.setVisibility(View.GONE);
        });


        codeCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationAdapterListener.onVerificationCodeSent(phoneFormatted);

                timer.schedule(new TimerTask() {
                    int counter = OTP_TIMEOUT;
                    @Override
                    public void run() {
                        context.runOnUiThread(() -> {
                            if (counter >= 0) {
                                resendBtn.setText(String.format(ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0),
                                        "%s%d%s",
                                        context.getString(R.string.verifi_phone_resendBtn_label_inactive_prefix), counter,
                                        context.getString(R.string.verifi_phone_resendBtn_label_inactive_suffix)));
                                if (counter == OTP_TIMEOUT-10) {
                                    resendBtn.setVisibility(View.VISIBLE);
                                }
                                counter--;

                            } else {
                                resendBtn.setText(context.getString(R.string.verifi_phone_resendBtn_label_active));
                                resendBtn.setEnabled(true);
                                timer.cancel();
                            }
                        });
                    }
                }, 0, 1000);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                otpCodeEt.setVisibility(View.GONE);
                otpExpiredTv.setVisibility(View.VISIBLE);
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
                signInWithPhoneAuthCredential(phoneAuthCredential, loginListener);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d("verifyPhone","onVerificationFailed", e);
                verificationAdapterListener.onError(Constants.VerifiEC.VERIFICATION_FAILED);
            }
        };

        loginListener = task -> {
            if (task.isSuccessful() || DEBUG_MODE) {
                Log.d("signInWithPhone", "signInWithCredential:success");

                otpCodeEt.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_check_green_24dp, 0 );
                otpCodeEt.setError(null);
                continueBtn2.setEnabled(true);
                timer.cancel();
                resendBtn.setVisibility(View.GONE);

            } else {
                // The verification code entered was invalid
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.d("signInWithPhone", "signInWithCredential:failure:invalid code", task.getException());
                    otpCodeEt.setError("Wrong Code");

                } else {
                    Log.d("signInWithPhone", "signInWithCredential:failure", task.getException());
                    verificationAdapterListener.onError(Constants.VerifiEC.SIGNIN_FAILED);
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


        continueBtn2.setOnClickListener(new View.OnClickListener() {
            boolean processing = false;
            @Override
            public void onClick(View v) {
                if (processing) return;

                processing = true;
                continueBtn2.setProgress(5);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null || Constants.DEBUG_MODE) {
                    String uuid;
                    if (DEBUG_MODE) {
                        uuid = "Deya";
                    } else {
                        uuid = currentUser.getUid();
                    }

                    AsyncTask.execute(() -> {
                        new AuthRequestBuilder(context).attachToken(Constants.TOKEN_NONE)
                            .setCallback(new RequestsCallback() {
                                @Override
                                public void onCheckSuccess(CheckData checkResult) {
                                    super.onCheckSuccess(checkResult);
                                    continueBtn2.setProgress(100);
                                    boolean isRegistered = checkResult.isRegistered();
                                    verificationAdapterListener.onContinue(isRegistered, phoneFormatted, phone);
                                    processing = false;
                                }

                                @Override
                                public void onFailure(int errorCode) {
                                    super.onFailure(errorCode);
                                    continueBtn2.setProgress(-1);
                                    verificationAdapterListener.onError(Constants.RQM_EC.NO_INTERNET_CONNECTION);
                                    processing = false;
                                }
                            })
                            .check(uuid);
                    });
                } else {
                    continueBtn2.setProgress(-1);
                    processing = false;
                    verificationAdapterListener.onError(Constants.VerifiEC.REVERIFY_PHONE);
                }
            }
        });
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
    private void verifyPhone(String phoneNum, PhoneAuthProvider.OnVerificationStateChangedCallbacks changedCallbacks) {
        /* WARNING: This only works when called on an actual phone, wont work on an emulator */
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNum,
                OTP_TIMEOUT,
                TimeUnit.SECONDS,
                context,
                changedCallbacks);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, OnCompleteListener<AuthResult> listener) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(context, listener);
    }

    private void signOutUser() {
        firebaseAuth.signOut();
    }
}

