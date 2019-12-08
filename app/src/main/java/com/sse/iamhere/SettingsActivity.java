package com.sse.iamhere;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.ConfigurationCompat;

import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.LocaleUtil;
import com.sse.iamhere.Utils.PreferencesUtil;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private boolean isLangDialogShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Theme must be set before setContentView
        Constants.Role role = PreferencesUtil.getRole(this, Constants.Role.NONE);
        setTheme(role.getTheme());

        LocaleUtil.setConfigLang(this);

        setContentView(R.layout.activity_settings);
        overridePendingTransition(R.anim.push_up_in, R.anim.none);

        if (savedInstanceState!=null){
            isLangDialogShown = savedInstanceState.getBoolean("isLangDialogShown");
        }

        setupUI();
        loadSettings();
    }

    private void setupUI() {
        setTitle(R.string.activity_settings_title);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        LinearLayout langLy = findViewById(R.id.settings_langLy);
        langLy.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                final ArrayList<String> languageCodes = new ArrayList<String>() {{
                    add("en");
                    add("ru");
                }};

                Locale cl = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
                int checkedItem = languageCodes.indexOf(cl.getLanguage());

                final CharSequence[] items = {
                        new Locale("en").getDisplayName(cl),
                        new Locale("ru").getDisplayName(cl)
                };

                new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle(getString(R.string.settings_lang_titleTv))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setOnDismissListener(dialog -> isLangDialogShown = false)
                    .setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
                        if (which!=checkedItem) {
                            PreferencesUtil.setPrefByName(SettingsActivity.this, Constants.APP_LANG, languageCodes.get(which));
                            LocaleUtil.setConfigLang(SettingsActivity.this, new Locale(languageCodes.get(which)));
                            finishActivity(true);
                        }
                    })
                    .setIcon(R.drawable.ic_globe_black_28dp)
                    .show();

                isLangDialogShown = true;
            }
        });

        if (isLangDialogShown) langLy.callOnClick();
    }

    private void loadSettings() {
        Locale currentLocale = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
        String currentLanguage = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0).getDisplayName(currentLocale);

        TextView langSubtitleTv = findViewById(R.id.settings_lang_subtitleTv);
        langSubtitleTv.setText(String.format("%s %s", getString(R.string.settings_lang_subtitleTv_prefix), currentLanguage));
    }

//    private void showInfoSnackbar(String msg, int duration) {
//        if (getWindow().getDecorView().isShown()) {
//            if (!TextUtils.isEmpty(msg)) {
//                Snackbar.make(findViewById(android.R.id.content), msg, duration).show();
//            }
//        }
//    }

    private void finishActivity(boolean languageChanged) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("languageChanged", languageChanged);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finishActivity(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.none, R.anim.push_down_out);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("isLangDialogShown", isLangDialogShown);
    }
}
