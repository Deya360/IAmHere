package com.sse.iamhere.Server;

import android.content.Context;
import android.text.TextUtils;

import com.sse.iamhere.Server.Body.TokenData;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;

import java.util.Date;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;
import static com.sse.iamhere.Utils.Constants.TOKEN_NONE;
import static com.sse.iamhere.Utils.Constants.TOKEN_REFRESH;

public class TokenProvider {
    static class TokenPair {
        private String token;
        private long expiry;

        String getToken() {
            return token;
        }
        void setToken(String token) {
            this.token = token;
        }

        long getExpiry() {
            return expiry;
        }
        void setExpiry(long expiry) {
            this.expiry = expiry;
        }
    }

    public interface TokenProviderCallback {
        void onSuccess(int token_type, String token);
        void onFailure(int errorCode);
    }

    public static void getUsableToken(Context context, int type, Constants.Role role, TokenProviderCallback callback) {
        if (type==TOKEN_NONE) {
            callback.onSuccess(type, null);
            return;
        }

        TokenPair tokenPair = getTokenPair(context, type, role);

        boolean valid = isTokenValid(tokenPair.getExpiry());
        if (valid && !PreferencesUtil.isFlag403()) {
            callback.onSuccess(type, tokenPair.getToken());

        } else {
            if (PreferencesUtil.isFlag403()) {
                ServiceGen.resetCachedServices();
            }

            if (type==TOKEN_ACCESS) {
                // Renew using refresh token
                getUsableToken(context, TOKEN_REFRESH, role, new TokenProviderCallback() {
                    @Override
                    public void onSuccess(int token_type, String token) {
                        if (token_type==TOKEN_REFRESH && !TextUtils.isEmpty(token)) {
                            //call to renew
                            new AuthRequestBuilder(context)
                                .setCallback(new RequestsCallback() {
                                    @Override
                                    public void onRefreshSuccess(TokenData renewedTokenData) {
                                        super.onRefreshSuccess(renewedTokenData);
//                                        Log.e("ServiceGen", renewedTokenData.toString());
                                        try {
                                            PreferencesUtil.setToken(context, renewedTokenData, role);
                                            PreferencesUtil.setFlag403(false);
                                            callback.onSuccess(type, renewedTokenData.getAccessToken());

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            onFailure(Constants.RQM_EC.TOKEN_STORE_FAIL);
                                        }
                                    }

                                    @Override
                                    public void onFailure(int errorCode) {
                                        super.onFailure(errorCode);
                                        callback.onFailure(errorCode);
                                    }
                                })
                                .attachToken(token_type)
                                .refresh(token);

                        } else {
                            callback.onFailure(Constants.RQM_EC.REFRESH_REFRESH_EXPIRED);
                        }
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        callback.onFailure(errorCode);
                    }
                });


            } else if (type==TOKEN_REFRESH) {
                callback.onFailure(Constants.RQM_EC.REFRESH_REFRESH_EXPIRED);

            } else {
                callback.onFailure(Constants.RQM_EC.REFRESH_UNSUPPORTED_TOKEN);
            }
        }
    }

    private static boolean isTokenValid(long expiry) {
        long now = new Date().getTime();
        return now < (expiry-5000);
    }

    private static TokenPair getTokenPair(Context context, int type, Constants.Role role) {
        TokenPair tokenPair = null;

        try {
            TokenData tokenData = PreferencesUtil.getTokenData(context, role);

            tokenPair = new TokenPair();
            if (type==TOKEN_ACCESS) {
                tokenPair.setToken(tokenData.getAccessToken());
                tokenPair.setExpiry(tokenData.getAccessTokenExpiryDate());

            } else if (type==TOKEN_REFRESH) {
                tokenPair.setToken(tokenData.getRefreshToken());
                tokenPair.setExpiry(tokenData.getRefreshTokenExpiryDate());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tokenPair;
    }
}
