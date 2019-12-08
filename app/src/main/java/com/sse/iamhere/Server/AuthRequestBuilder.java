package com.sse.iamhere.Server;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.Body.TokenData;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;

import java.net.HttpURLConnection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sse.iamhere.Utils.ServerConstants.REQUEST_PREFIX;

public class AuthRequestBuilder{
    private Context context;
    private int tokenType = Constants.TOKEN_NONE;

    public AuthRequestBuilder(Context context) {
        this.context = context;
    }

    private RequestsCallback mCallback;
    public AuthRequestBuilder setCallback(RequestsCallback mCallback) {
        this.mCallback = mCallback;
        return this;
    }
    /* This is a null safe getter that will return a new callback instead of a null one*/
    private RequestsCallback getCallback() {
        if (mCallback==null) {
            return new RequestsCallback();
        }
        return mCallback;
    }

    public AuthRequestBuilder attachToken(int tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    // Extract string from body
    private String extract(@Nullable ResponseBody body, String msg) {
        try {
            if (body!=null) return body.string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    // region Auth requests:
    public void register(String uuid, String password, Constants.Role role) {
        TokenProvider.getUsableToken(context, tokenType, role, new TokenProvider.TokenProviderCallback() {
            @Override
            public void onSuccess(int token_type, String token) {
                Requests requests = ServiceGen.createService(Requests.class, REQUEST_PREFIX, token, tokenType);
                Call<TokenData> call = requests.register(uuid, password, role.toSerializedJSON());

                call.enqueue(new Callback<TokenData>() {
                    @Override
                    public void onResponse(@NonNull Call<TokenData> call, @NonNull Response<TokenData> response) {
                        if (response.isSuccessful()) {
                            Log.i("AuthRequestBuilder", "register:onResponse - Code: " + response.code());

                            //Save tokenData
                            try {
                                PreferencesUtil.setToken(context, response.body(), role);
                                getCallback().onRegisterSuccess();

                            } catch (Exception e) {
                                e.printStackTrace();
                                getCallback().onFailure(Constants.RQM_EC.TOKEN_STORE_FAIL);
                            }

                        } else {
                            Log.i("AuthRequestBuilder",
                                    String.format("register:onResponse - Code: %d\nMsg: %s",
                                            response.code(), extract(response.errorBody(), response.message())));

                            switch (response.code()) {
                                case HttpURLConnection.HTTP_BAD_REQUEST:
                                    getCallback().onFailure(Constants.RQM_EC.REGISTRATION_BAD_PHONE);
                                    break;

                                case HttpURLConnection.HTTP_CONFLICT:
                                    getCallback().onFailure(Constants.RQM_EC.REGISTRATION_USER_EXISTS);
                                    break;

                                case HttpURLConnection.HTTP_BAD_GATEWAY:
                                default:
                                    getCallback().onFailure(Constants.RQM_EC.REGISTRATION_BAD_ROLE);
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TokenData> call, @NonNull Throwable t) {
                        Log.i("AuthRequestBuilder", "register:onFailure - Msg: " + t.getMessage());
                        getCallback().onFailure(Constants.RQM_EC.REGISTRATION_UNKNOWN);
                    }
                });
            }

            @Override
            public void onFailure(int errorCode) {
                getCallback().onFailure(errorCode);
            }
        });
    }
    public void login(String uuid, String password, Constants.Role role) {
        TokenProvider.getUsableToken(context, tokenType, role, new TokenProvider.TokenProviderCallback() {
            @Override
            public void onSuccess(int token_type, String token) {
                Requests requests = ServiceGen.createService(Requests.class, REQUEST_PREFIX, token, tokenType);
                Call<TokenData> call = requests.login(uuid, password, role.toSerializedJSON());

                call.enqueue(new Callback<TokenData>() {
                    @Override
                    public void onResponse(@NonNull Call<TokenData> call, @NonNull Response<TokenData> response) {
                        if (response.isSuccessful()) {
                            Log.i("AuthRequestBuilder", "login:onResponse - Code: " + response.code());

                            //Save tokenData
                            try {
                                PreferencesUtil.setToken(context, response.body(), role);
                                getCallback().onLoginSuccess();

                            } catch (Exception e) {
                                e.printStackTrace();
                                getCallback().onFailure(Constants.RQM_EC.TOKEN_STORE_FAIL);
                            }

                        } else {
                            Log.i("AuthRequestBuilder",
                                    String.format("login:onResponse - Code: %d\nMsg: %s",
                                            response.code(), extract(response.errorBody(), response.message())));

                            switch (response.code()) {
                                case HttpURLConnection.HTTP_BAD_REQUEST:
                                    getCallback().onFailure(Constants.RQM_EC.LOGIN_BAD_PHONE);
                                    break;

                                case HttpURLConnection.HTTP_CONFLICT:
                                    getCallback().onFailure(Constants.RQM_EC.LOGIN_USER_NOT_FOUND);
                                    break;

                                case HttpURLConnection.HTTP_BAD_GATEWAY:
                                default:
                                    getCallback().onFailure(Constants.RQM_EC.LOGIN_BAD_ROLE);
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TokenData> call, @NonNull Throwable t) {
                        Log.i("AuthRequestBuilder", "login:onFailure - Msg: " + t.getMessage());
                        getCallback().onFailure(Constants.RQM_EC.LOGIN_UNKNOWN);
                    }
                });
            }

            @Override
            public void onFailure(int errorCode) {
                getCallback().onFailure(errorCode);
            }
        });
    }
    /**
     * Must supply Access Token
     */
    public void logout() {
        TokenProvider.getUsableToken(context, tokenType,
                PreferencesUtil.getRole(context, Constants.Role.NONE), new TokenProvider.TokenProviderCallback() {
            @Override
            public void onSuccess(int token_type, String token) {
                Requests requests = ServiceGen.createService(Requests.class, REQUEST_PREFIX, token, tokenType);
                Call<ResponseBody> call = requests.logout();

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.i("AuthRequestBuilder", "logout:onResponse - Code: " + response.code());
                            getCallback().onLogoutSuccess();

                        } else {
                            Log.i("AuthRequestBuilder",
                                    String.format("logout:onResponse - Code: %d\nMsg: %s",
                                            response.code(), extract(response.errorBody(), response.message())));
                            getCallback().onFailure(Constants.RQM_EC.LOGOUT_UNKNOWN);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.i("AuthRequestBuilder", "logout:onFailure - Msg: " + t.getMessage());
                        getCallback().onFailure(Constants.RQM_EC.LOGOUT_UNKNOWN);
                    }
                });
            }

            @Override
            public void onFailure(int errorCode) {
                getCallback().onFailure(errorCode);
            }
        });

    }
    public void refresh(String refreshToken) {
        Requests requests = ServiceGen.createService(Requests.class, REQUEST_PREFIX, refreshToken, tokenType);
        Call<TokenData> call = requests.refresh();

        call.enqueue(new Callback<TokenData>() {
            @Override
            public void onResponse(@NonNull Call<TokenData> call, @NonNull Response<TokenData> response) {
                if (response.isSuccessful()) {
                    Log.i("AuthRequestBuilder", "refresh:onResponse - Code: " + response.code());
                    getCallback().onRefreshSuccess(response.body());

                } else {
                    Log.i("AuthRequestBuilder",
                            String.format("refresh:onResponse - Code: %d\nMsg: %s",
                                    response.code(), extract(response.errorBody(), response.message())));
                    getCallback().onFailure(Constants.RQM_EC.REFRESH_CALL_BAD_RESPONSE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenData> call, @NonNull Throwable t) {
                Log.i("AuthRequestBuilder", "refresh:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.REFRESH_CALL_FAIL);
            }
        });
    }
    public AuthRequestBuilder check(String uuid) {
        TokenProvider.getUsableToken(context, tokenType, null, new TokenProvider.TokenProviderCallback() {
            @Override
            public void onSuccess(int token_type, String token) {
                Requests requests = ServiceGen.createService(Requests.class, REQUEST_PREFIX, token, tokenType);
                Call<CheckData> call = requests.check(uuid);

                call.enqueue(new Callback<CheckData>() {
                    @Override
                    public void onResponse(@NonNull Call<CheckData> call, @NonNull Response<CheckData> response) {
                        if (response.isSuccessful()) {
                            Log.i("AuthRequestBuilder", "check:onResponse - Code: " + response.code());
                            getCallback().onCheckSuccess(response.body());

                        } else {
                            Log.i("AuthRequestBuilder",
                                    String.format("check:onResponse - Code: %d\nMsg: %s",
                                            response.code(), extract(response.errorBody(), response.message())));
                            getCallback().onFailure(Constants.RQM_EC.CHECK_UNKNOWN);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CheckData> call, @NonNull Throwable t) {
                        Log.i("AuthRequestBuilder", "check:onFailure - Msg: " + t.getMessage());
                        getCallback().onFailure(Constants.RQM_EC.CHECK_UNKNOWN);
                    }
                });
            }

            @Override
            public void onFailure(int errorCode) {
                getCallback().onFailure(errorCode);
            }
        });

        return this;
    }
    //endregion
}