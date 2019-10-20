package com.sse.iamhere.Server;

import android.util.Log;

import androidx.annotation.NonNull;

import com.sse.iamhere.Utils.Constants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestManager {
    public RequestManager() {}

//    public void register(String phone, String pass) {
//        Requests requests = ServiceGen.createService(Requests.class, Constants.FNS_API_BASE_URL);
//        Call<ResponseBody> call = requests.checkReceipt(qrCode.getFn(), qrCode.getN(), qrCode.getFd(),
//                qrCode.getFpd(), qrCode.getDate(), qrCode.getAmount());
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                switch (response.code()) {
//                    case 204:
//                        // delay is needed for first time requests, otherwise, server might not have detailed return body prepared yet
//                        Log.e("RequestManager", "checkReceipt:onResponse - Code: " +  204); //TEMPPP
//                        detailedReceipt(qrCode,5);
//                        break;
//
//                    case 406:
//                        requestManagerResponse.onRetrievalFail(Constants.RQM_FC.RECEIPT_NOT_FOUND);
//                        break;
//
//                    default:
//                        Log.e("RequestManager", "checkReceipt:onResponse - Code: " + + response.code()
//                                + "\nMsg: " + response.message());
//                        requestManagerResponse.onRetrievalFail(Constants.RQM_FC.UNKNOWN);
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.e("RequestManager", "checkReceipt:onFailure - Msg: " +  t.getMessage());
//                requestManagerResponse.onRetrievalFail(Constants.RQM_FC.UNKNOWN);
//            }
//        });
//    }
//
//    public void registration(String email, String name, String phone) {
//        Requests requests = ServiceGen.createService(Requests.class, Constants.FNS_API_BASE_URL);
//        Call<ResponseBody> call = requests.registration(new TokenData(email, name, phone));
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (!response.isSuccessful()) {
//                    Log.v("OkHttp:LOG:", "Bad Code: " + response.code());
//                    return;
//                }
//
//                Log.v("OkHttp:LOG:", "Good Code: " + response.code());
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.v("OkHttp:LOG:", t.getMessage());
//            }
//        });
//    }
//    public void login(String username, String password) {
//        Requests requests = ServiceGen.createService(Requests.class, Constants.FNS_API_BASE_URL, username, password);
//        Call<ResponseBody> call = requests.login();
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (!response.isSuccessful()) {
//                    Log.v("OkHttp:LOG:", "Bad Code: " + response.code());
//                    return;
//                }
//
//                Log.v("OkHttp:LOG:", "Good Code: " + response.code());
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.v("OkHttp:LOG:", t.getMessage());
//            }
//        });
//
//    }
//    public void passwordReset(String username) {
//        Requests requests = ServiceGen.createService(Requests.class, Constants.FNS_API_BASE_URL);
//        Call<ResponseBody> call = requests.passwordReset(new PasswordResetBody(username));
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (!response.isSuccessful()) {
//                    Log.v("OkHttp:LOG:", "Bad Code: " + response.code());
//                    return;
//                }
//
//                Log.v("OkHttp:LOG:", "Good Code: " + response.code());
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.v("OkHttp:LOG:", t.getMessage());
//            }
//        });
//    }
}
