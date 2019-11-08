package com.sse.iamhere;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;

public class MainActivity_depreciated extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_depreciated);

//        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        Log.d("android_id", android_id);

        Button logoutFirebase = findViewById(R.id.logout_firebase);
        logoutFirebase.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
        });

        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            logUserOut();
        });

    }

    private boolean logUserOut() {
        Constants.Role role;
        if ((role=PreferencesUtil.getRole(this, null))!=null) {
            try {
                PreferencesUtil.setToken(this, null, role);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
