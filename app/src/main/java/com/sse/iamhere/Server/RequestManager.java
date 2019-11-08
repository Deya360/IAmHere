package com.sse.iamhere.Server;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Server.Body.TokenData;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sse.iamhere.Utils.ServerConstants.REQUEST_PREFIX;

public class RequestManager{
    private Context context;
    private int tokenType = Constants.TOKEN_NONE;

    public RequestManager(Context context) {
        this.context = context;
    }

    private RequestCallback mCallback;
    public void setCallback(RequestCallback mCallback) {
        this.mCallback = mCallback;
    }

    public RequestManager attachToken(int tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    /*
    *  Auth requests:
    * */
    public RequestManager register(String uuid, String password, Constants.Role role) {
        TokenProvider.getUsableAccessToken(context, tokenType, role, new TokenProvider.TokenProviderCallback() {
            @Override
            public void onSuccess(int token_type, String token) {
                Requests requests = ServiceGen.createService(Requests.class, REQUEST_PREFIX, token, tokenType);
                Call<TokenData> call = requests.register(uuid, password, role.toSerializedJSON());

                call.enqueue(new Callback<TokenData>() {
                    @Override
                    public void onResponse(@NonNull Call<TokenData> call, @NonNull Response<TokenData> response) {
                        if (response.isSuccessful()) {
                            Log.i("RequestManager", "register:onResponse - Code: " + response.code());

                            //Save tokenData
                            try {
                                PreferencesUtil.setToken(context, response.body(), role);
                                mCallback.onRegisterSuccess();

                            } catch (Exception e) {
                                e.printStackTrace();
                                mCallback.onRegisterFailure(Constants.RQM_EC.TOKEN_STORE_FAIL);
                            }

                        } else {
                            Log.i("RequestManager",
                                    String.format("register:onResponse - Code: %d\nMsg: %s",
                                            response.code(), response.message()));

                            switch (response.code()) {
                                case HttpURLConnection.HTTP_BAD_REQUEST:
                                    mCallback.onRegisterFailure(Constants.RQM_EC.REGISTRATION_BAD_PHONE);
                                    break;

                                case HttpURLConnection.HTTP_CONFLICT:
                                    mCallback.onRegisterFailure(Constants.RQM_EC.REGISTRATION_USER_EXISTS);
                                    break;

                                case HttpURLConnection.HTTP_BAD_GATEWAY:
                                default:
                                    mCallback.onRegisterFailure(Constants.RQM_EC.REGISTRATION_BAD_ROLE);
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TokenData> call, @NonNull Throwable t) {
                        Log.i("RequestManager", "register:onFailure - Msg: " + t.getMessage());
                        mCallback.onRegisterFailure(Constants.RQM_EC.REGISTRATION_UNKNOWN);
                    }
                });
            }

            @Override
            public void onFailure(int errorCode) {
                mCallback.onRegisterFailure(errorCode);
            }
        });
        return this;
    }
    public RequestManager login(String uuid, String password, Constants.Role role) {
        TokenProvider.getUsableAccessToken(context, tokenType, role, new TokenProvider.TokenProviderCallback() {
            @Override
            public void onSuccess(int token_type, String token) {
                Requests requests = ServiceGen.createService(Requests.class, REQUEST_PREFIX, token, tokenType);
                Call<TokenData> call = requests.login(uuid, password, role.toSerializedJSON());

                call.enqueue(new Callback<TokenData>() {
                    @Override
                    public void onResponse(@NonNull Call<TokenData> call, @NonNull Response<TokenData> response) {
                        if (response.isSuccessful()) {
                            Log.i("RequestManager", "login:onResponse - Code: " + response.code());

                            //Save tokenData
                            try {
                                PreferencesUtil.setToken(context, response.body(), role);
                                mCallback.onLoginSuccess();

                            } catch (Exception e) {
                                e.printStackTrace();
                                mCallback.onLoginFailure(Constants.RQM_EC.TOKEN_STORE_FAIL);
                            }

                        } else {
                            Log.i("RequestManager",
                                    String.format("login:onResponse - Code: %d\nMsg: %s",
                                            response.code(), response.message()));

                            switch (response.code()) {
                                case HttpURLConnection.HTTP_BAD_REQUEST:
                                    mCallback.onLoginFailure(Constants.RQM_EC.LOGIN_BAD_PHONE);
                                    break;

                                case HttpURLConnection.HTTP_CONFLICT:
                                    mCallback.onLoginFailure(Constants.RQM_EC.LOGIN_USER_NOT_FOUND);
                                    break;

                                case HttpURLConnection.HTTP_BAD_GATEWAY:
                                default:
                                    mCallback.onLoginFailure(Constants.RQM_EC.LOGIN_BAD_ROLE);
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TokenData> call, @NonNull Throwable t) {
                        Log.i("RequestManager", "login:onFailure - Msg: " + t.getMessage());
                        mCallback.onLoginFailure(Constants.RQM_EC.LOGIN_UNKNOWN);
                    }
                });
            }

            @Override
            public void onFailure(int errorCode) {
                mCallback.onLoginFailure(errorCode);
            }
        });
        return this;
    }
    public RequestManager refresh(String refreshToken) {
        Requests requests = ServiceGen.createService(Requests.class, REQUEST_PREFIX, refreshToken, tokenType);
        Call<TokenData> call = requests.refresh();

        call.enqueue(new Callback<TokenData>() {
            @Override
            public void onResponse(@NonNull Call<TokenData> call, @NonNull Response<TokenData> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestManager", "refresh:onResponse - Code: " + response.code());
                    mCallback.onRefreshSuccess(response.body());

                } else {
                    Log.i("RequestManager",
                            String.format("refresh:onResponse - Code: %d\nMsg: %s",
                                    response.code(), response.message()));
                    mCallback.onRefreshFailure(Constants.RQM_EC.REFRESH_CALL_BAD_RESPONSE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenData> call, @NonNull Throwable t) {
                Log.i("RequestManager", "refresh:onFailure - Msg: " + t.getMessage());
                mCallback.onRefreshFailure(Constants.RQM_EC.REFRESH_CALL_FAIL);
            }
        });
        return this;
    }
    public RequestManager check(String uuid) {
        TokenProvider.getUsableAccessToken(context, tokenType, null, new TokenProvider.TokenProviderCallback() {
            @Override
            public void onSuccess(int token_type, String token) {
                Requests requests = ServiceGen.createService(Requests.class, REQUEST_PREFIX, token, tokenType);
                Call<CheckData> call = requests.check(uuid);

                call.enqueue(new Callback<CheckData>() {
                    @Override
                    public void onResponse(@NonNull Call<CheckData> call, @NonNull Response<CheckData> response) {
                        if (response.isSuccessful()) {
                            Log.i("RequestManager", "check:onResponse - Code: " + response.code());
                            mCallback.onCheckSuccess(response.body());

                        } else {
                            Log.i("RequestManager",
                                    String.format("check:onResponse - Code: %d\nMsg: %s",
                                            response.code(), response.message()));
                            mCallback.onCheckFailure(Constants.RQM_EC.CHECK_UNKNOWN);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CheckData> call, @NonNull Throwable t) {
                        Log.i("RequestManager", "check:onFailure - Msg: " + t.getMessage());
                        mCallback.onCheckFailure(Constants.RQM_EC.CHECK_UNKNOWN);
                    }
                });
            }

            @Override
            public void onFailure(int errorCode) {
                mCallback.onCheckFailure(errorCode);
            }
        });

        return this;
    }


    /*
     *  Participator - party requests:
     * */
    public RequestManager attendeeFindParty(String codeWord) {
        TokenProvider.getUsableAccessToken(context, tokenType,
                PreferencesUtil.getRole(context, Constants.Role.NONE), new TokenProvider.TokenProviderCallback() {
                    @Override
                    public void onSuccess(int token_type, String token) {
                        Requests requests = ServiceGen.createService(Requests.class, REQUEST_PREFIX, token, tokenType);
                        Call<PartyData> call = requests.attendeeFindParty(codeWord);

                        call.enqueue(new Callback<PartyData>() {
                            @Override
                            public void onResponse(@NonNull Call<PartyData> call, @NonNull Response<PartyData> response) {
                                if (response.isSuccessful()) {
                                    Log.i("RequestManager", "attendeeFindParty:onResponse - Code: " + response.code());
                                    mCallback.onParticipatorSuccess(response.body());

                                } else {
                                    Log.i("RequestManager",
                                            String.format("attendeeFindParty:onResponse - Code: %d\nMsg: %s",
                                                    response.code(), response.message()));
                                    mCallback.onParticipatorFailure(Constants.RQM_EC.FIND_PARTY_ATTENDEE_UNKNOWN);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<PartyData> call, @NonNull Throwable t) {
                                Log.i("RequestManager", "attendeeFindParty:onFailure - Msg: " + t.getMessage());
                                mCallback.onParticipatorFailure(Constants.RQM_EC.FIND_PARTY_ATTENDEE_UNKNOWN);
                            }
                        });
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        mCallback.onParticipatorFailure(errorCode);
                    }
                });
        return this;
    }


}
