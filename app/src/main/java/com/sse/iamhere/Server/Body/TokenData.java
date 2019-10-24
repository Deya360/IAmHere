package com.sse.iamhere.Server.Body;

public class TokenData {
    private final String access_token;
    private final String refresh_token;
    private final long access_token_expire_date;
    private final long refresh_token_expire_date;

    public TokenData(String access_token, String refresh_token,
                     long access_token_expire_date, long refresh_token_expire_date) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.access_token_expire_date = access_token_expire_date;
        this.refresh_token_expire_date = refresh_token_expire_date;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public long getAccess_token_expire_date() {
        return access_token_expire_date;
    }

    public long getRefresh_token_expire_date() {
        return refresh_token_expire_date;
    }
}
