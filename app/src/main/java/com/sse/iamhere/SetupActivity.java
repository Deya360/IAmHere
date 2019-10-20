package com.sse.iamhere;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.sse.iamhere.Data.Entitites.Account;
import com.sse.iamhere.Data.VM.AccountViewModel;
import com.sse.iamhere.Utils.PreferencesUtil;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.sse.iamhere.Utils.Constants.ACCOUNT_ID;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        if (savedInstanceState!=null) {


        } else {
//            Bundle extras = getIntent().getExtras();
//            if (extras==null) {
//                Log.e("SetupActivity", "getIntent().getExtras() is null onCreate");
//                return;
//            }


        }

        Button helpMeChoose = findViewById(R.id.welcome_helpBtn);
        helpMeChoose.setOnClickListener(v -> {
            showOnboardActivity();
        });

        LinearLayout setup_adminLy = findViewById(R.id.setup_adminLy);
        LinearLayout setup_hostLy = findViewById(R.id.setup_hostLy);
        LinearLayout setup_attendeeLy = findViewById(R.id.setup_attendeeLy);


        setup_adminLy.setOnClickListener(v -> {
            setup_adminLy.setBackground(ContextCompat.getDrawable(this, R.drawable.back_rounded_rect_card_green));
            setup_hostLy.setVisibility(View.GONE);
            setup_attendeeLy.setVisibility(View.GONE);
            helpMeChoose.setText("Sign Out");
            helpMeChoose.setOnClickListener(v1 -> {
                tempSignOut();

            });
        });

        setup_hostLy.setOnClickListener(v -> {
            setup_hostLy.setBackground(getDrawable(R.drawable.back_rounded_rect_card_green));
            setup_adminLy.setVisibility(View.GONE);
            setup_attendeeLy.setVisibility(View.GONE);
            helpMeChoose.setText("Sign Out");
            helpMeChoose.setOnClickListener(v1 -> {
                tempSignOut();
            });
        });

        setup_attendeeLy.setOnClickListener(v -> {
            setup_attendeeLy.setBackground(getDrawable(R.drawable.back_rounded_rect_card_green));
            setup_adminLy.setVisibility(View.GONE);
            setup_hostLy.setVisibility(View.GONE);
            helpMeChoose.setText("Sign Out");
            helpMeChoose.setOnClickListener(v1 -> {
                tempSignOut();
            });
        });


        AccountViewModel accountViewModel = ViewModelProviders.of(SetupActivity.this)
                .get(AccountViewModel.class);

        int id = Integer.parseInt(PreferencesUtil.getPrefByName(getApplicationContext(), ACCOUNT_ID, "-1"));
        accountViewModel.getAccountByID(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableSingleObserver<List<Account>>() {
                @Override
                public void onSuccess(List<Account> accounts) {
                    if (accounts.get(0).getRole().equals("Admin")) {
                        setup_adminLy.setBackground(ContextCompat.getDrawable(SetupActivity.this, R.drawable.back_rounded_rect_card_green));
                        setup_hostLy.setVisibility(View.GONE);
                        setup_attendeeLy.setVisibility(View.GONE);
                        helpMeChoose.setText("Sign Out");
                        helpMeChoose.setOnClickListener(v1 -> {
                            tempSignOut();
                        });
                    } else if (accounts.get(0).getRole().equals("Host")) {
                        setup_hostLy.setBackground(getDrawable(R.drawable.back_rounded_rect_card_green));
                        setup_adminLy.setVisibility(View.GONE);
                        setup_attendeeLy.setVisibility(View.GONE);
                        helpMeChoose.setText("Sign Out");
                        helpMeChoose.setOnClickListener(v1 -> {
                            tempSignOut();
                        });

                    } else if (accounts.get(0).getRole().equals("Attendee")) {
                        setup_attendeeLy.setBackground(getDrawable(R.drawable.back_rounded_rect_card_green));
                        setup_adminLy.setVisibility(View.GONE);
                        setup_hostLy.setVisibility(View.GONE);
                        helpMeChoose.setText("Sign Out");
                        helpMeChoose.setOnClickListener(v1 -> {
                            tempSignOut();
                        });
                    }
                }

                @Override
                public void onError(Throwable e) {

                }
            });
    }

    private void tempSignOut() {
        PreferencesUtil.setPrefByName(getApplicationContext(), ACCOUNT_ID, "-1");
        Bundle bundle = new Bundle();
        bundle.putString("phone", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        bundle.putString("phoneFormatted", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()); //TODO: format properly
        bundle.putBoolean("isRegister", false);

        Intent intent = new Intent(SetupActivity.this, AuthenticationActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void updateRole(String role) {
        AccountViewModel accountViewModel = ViewModelProviders.of(SetupActivity.this)
                .get(AccountViewModel.class);

        int id = Integer.parseInt(PreferencesUtil.getPrefByName(getApplicationContext(), ACCOUNT_ID, "-1"));
        if (id!=-1) {

        };
    }

    private void showOnboardActivity() {
        Intent intent = new Intent(SetupActivity.this, OnboardActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
