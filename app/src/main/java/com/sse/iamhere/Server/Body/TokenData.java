package com.sse.iamhere.Server.Body;

import com.google.gson.annotations.SerializedName;

public class TokenData {
    @SerializedName("access_token")
    private final String accessToken;

    @SerializedName("refresh_token")
    private final String refreshToken;

    @SerializedName("access_token_expire_date")
    private final long accessTokenExpireDate;

    @SerializedName("refresh_token_expire_date")
    private final long refreshTokenExpireDate;

    public TokenData(String accessToken, String refreshToken,
                     long accessTokenExpireDate, long refreshTokenExpireDate) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpireDate = accessTokenExpireDate;
        this.refreshTokenExpireDate = refreshTokenExpireDate;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getAccessTokenExpiryDate() {
        return accessTokenExpireDate;
    }

    public long getRefreshTokenExpiryDate() {
        return refreshTokenExpireDate;
    }
}
