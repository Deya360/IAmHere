package com.sse.iamhere.Server.Body;

public class TokenData {
    private final String access_token;
    private final String refresh_token;
    private final String access_token_expire_date;
    private final String refresh_token_expire_date;

    public TokenData(String access_token, String refresh_token,
                     String access_token_expire_date, String refresh_token_expire_date) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.access_token_expire_date = access_token_expire_date;
        this.refresh_token_expire_date = refresh_token_expire_date;
    }
}
