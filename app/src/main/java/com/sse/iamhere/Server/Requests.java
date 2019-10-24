package com.sse.iamhere.Server;

import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Server.Body.TokenData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Requests {
    //Auth
    /*
    * Param details:
    * phone_number: 11 digits starting from the country code
    * account type: one of: ACCOUNT_HOST | ACCOUNT_PARTICIPATOR */
    @POST("app/register")
    Call<TokenData> register(@Query("phone_number") String phoneNumber,
                             @Query("UUID") String uuid,
                             @Query("password") String password,
                             @Query("account_type") String accountType);

    /*
     * Param details:
     * phone_number: 11 digits starting from the country code
     * account type: one of: ACCOUNT_HOST | ACCOUNT_PARTICIPATOR */
    @POST("app/login")
    Call<TokenData> login(@Query("phone_number") String phoneNumber,
                          @Query("password") String password,
                          @Query("account_type") String accountType);

    /*
    * Must supply:
    * @Header("refresh_token") String refreshToken
    * */
    @POST("app/refresh")
    Call<TokenData> refresh();

    @GET("check")
    Call<CheckData> check(@Query("UUID") String uuid);


    //Participator - party
    /*
     * Must supply:
     * @Header("access_token") String accessToken
     * */
    @GET("app/participator/find_party")
    Call<PartyData> attendeeFindParty(@Query("code_word") String codeWord);
}
