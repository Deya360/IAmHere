package com.sse.iamhere.Utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class InternetUtil {
    public interface InternetResponse {
        void connectionState(boolean connected);
    }

    private InternetResponse internetResponse;
    public InternetUtil(InternetResponse internetResponse) {
        this.internetResponse = internetResponse;
    }

    public void hasInternetConnection(Activity context) {
        hasInternetConnection((Context)context);
    }

    public void hasInternetConnection(Context context) {
//        TOD: remove temporary override
//        if (Build.FINGERPRINT.contains("generic")) {
//            internetResponse.connectionState(true);
//            return;
//        }

        // Check for network connection
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        // Network connection maybe available, however there may not be any connection,
        // so, If network connection is available, then check if network accesses is available too
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            new InternetAccessCheck(internet -> {
                //Callback
                if (internet) {
                    internetResponse.connectionState(true);

                } else {
                    internetResponse.connectionState(false);

                }
            });
            return;
        }

        internetResponse.connectionState(false);
    }
}

class InternetAccessCheck extends AsyncTask<Void,Void,Boolean> {
    private Consumer mConsumer;
    public  interface Consumer { void accept(Boolean internet); }

    InternetAccessCheck(Consumer consumer) { mConsumer = consumer; execute(); }

    @Override protected Boolean doInBackground(Void... voids) { try {
        Socket sock = new Socket();
        sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
        sock.close();
        return true;
    } catch (IOException e) { return false; } }

    @Override protected void onPostExecute(Boolean internet) { mConsumer.accept(internet); }
}
