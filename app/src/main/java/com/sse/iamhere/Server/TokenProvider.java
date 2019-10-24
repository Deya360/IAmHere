package com.sse.iamhere.Server;

import android.content.Context;
import android.text.TextUtils;

import com.sse.iamhere.Server.Body.TokenData;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;

import java.util.Calendar;
import java.util.Date;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;
import static com.sse.iamhere.Utils.Constants.TOKEN_NONE;
import static com.sse.iamhere.Utils.Constants.TOKEN_REFRESH;

class TokenProvider {
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

    static void getUsableAccessToken(Context context, int type, TokenProviderCallback callback) {
        if (type==TOKEN_NONE) {
            callback.onSuccess(type, null);
            return;
        }

        TokenPair tokenPair = getTokenPair(context, type);

        boolean valid = isTokenValid(tokenPair.getExpiry());
        if (valid) {
            callback.onSuccess(type, tokenPair.getToken());

        } else {
            if (type==TOKEN_ACCESS) {
                // Renew using refresh token
                getUsableAccessToken(context, TOKEN_REFRESH, new TokenProviderCallback() {
                    @Override
                    public void onSuccess(int token_type, String token) {
                        if (token_type==TOKEN_REFRESH && !TextUtils.isEmpty(token)) {
                            //call to renew

                            new RequestManager(context).refresh(token).attachToken(token_type)
                                .setCallback(new RequestCallback() {
                                    @Override
                                    public void onRefreshSuccess(TokenData renewedTokenData) {
                                        try {
                                            PreferencesUtil.setToken(context, renewedTokenData);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            onRefreshFailure(Constants.RQM_EC.TOKEN_STORE_FAIL);
                                        }
                                    }

                                    @Override
                                    public void onRefreshFailure(int errorCode) {
                                        callback.onFailure(errorCode);
                                    }
                                });

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
        Calendar expiryCal = Calendar.getInstance();
        expiryCal.setTimeInMillis(expiry);
        expiryCal.add(-5, Calendar.SECOND);
        return !expiryCal.before(new Date());
    }

    private static TokenPair getTokenPair(Context context, int type) {
        TokenPair tokenPair = null;

        try {
            TokenData tokenData = PreferencesUtil.getTokenData(context);

            tokenPair = new TokenPair();
            if (type==TOKEN_ACCESS) {
                tokenPair.setToken(tokenData.getAccess_token());
                tokenPair.setExpiry(tokenData.getAccess_token_expire_date());

            } else if (type==TOKEN_REFRESH) {
                tokenPair.setToken(tokenData.getRefresh_token());
                tokenPair.setExpiry(tokenData.getRefresh_token_expire_date());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tokenPair;
    }
}
