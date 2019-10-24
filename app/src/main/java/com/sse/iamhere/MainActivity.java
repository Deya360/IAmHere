package com.sse.iamhere;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.sse.iamhere.Utils.PreferencesUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        try {
            PreferencesUtil.setToken(this, null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
