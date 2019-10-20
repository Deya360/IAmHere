package com.sse.iamhere.Server;

import com.sse.iamhere.Server.Body.TokenData;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static com.sse.iamhere.Utils.ServerConstants.REQUEST_PREFIX;

public interface Requests {
    //Registration
    /*
    * Param details:
    * phone_number: 11 digits starting from the country code
    * account type: one of: ACCOUNT_HOST | ACCOUNT_PARTICIPATOR */
    @POST(REQUEST_PREFIX + "app/register")
    Call<TokenData> registration(@Query("UUID") String uuid,
                                 @Query("password") String password,
                                 @Query("phone_number") String phoneNumber);

    //Login
    /* Param details:
     * phone_number: 11 digits starting from the country code
     * account type: one of: ACCOUNT_HOST | ACCOUNT_PARTICIPATOR */
    @POST(REQUEST_PREFIX + "app/login")
    Call<TokenData> login(@Query("UUID") String uuid,
                          @Query("password") String password,
                          @Query("phone_number") String phoneNumber);

    //Refresh
    @POST(REQUEST_PREFIX + "app/login")
    Call<TokenData> refresh(@Header("refresh_token") String refresh_token);
}
