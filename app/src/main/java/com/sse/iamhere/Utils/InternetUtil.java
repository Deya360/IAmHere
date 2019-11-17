package com.sse.iamhere.Utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class InternetUtil {
    public interface InternetResponse {
        void isConnected();
        void notConnected();
    }

    private InternetResponse internetResponse;
    public InternetUtil(InternetResponse internetResponse) {
        this.internetResponse = internetResponse;
    }

    public InternetUtil hasInternetConnection(Activity context) {
        return hasInternetConnection((Context)context);
    }

    public InternetUtil hasInternetConnection(Context context) {
//        TOD: remove temporary override
//        if (Build.FINGERPRINT.contains("generic")) {
//            internetResponse.isConnected();
//            return;
//        }

        // Check for network connection
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        // Network connection maybe available, however there may not be any connection,
        // so, If network connection is available, then check if network accesses is available too
        if (netInfo != null && netInfo.isConnectedOrConnecting()) { //depreciated refers to this, should switch over to getActiveNetworkInfo();
            new InternetAccessCheck(internet -> {
                //Callback
                if (internet) {
                    internetResponse.isConnected();

                } else {
                    internetResponse.notConnected();
                }
            });
            return this;
        }

        internetResponse.notConnected();
        return this;
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
