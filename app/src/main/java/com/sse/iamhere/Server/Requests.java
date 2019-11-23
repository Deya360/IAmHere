package com.sse.iamhere.Server;

import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.Body.CredentialData;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.Body.TokenData;
import com.sse.iamhere.Server.Body.VisitData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Requests {
    //region Auth
    /*
    * Param details:
    * account type: one of: ACCOUNT_HOST | ACCOUNT_PARTICIPATOR */
    @POST("app/register")
    Call<TokenData> register(@Header("UUID") String uuid,
                             @Header("password") String password,
                             @Query("account_type") String accountType);


    /*
     * Param details:
     * account type: one of: ACCOUNT_HOST | ACCOUNT_PARTICIPATOR */
    @POST("app/login")
    Call<TokenData> login(@Header("UUID") String uuid,
                          @Header("password") String password,
                          @Query("account_type") String accountType);


    /**
     * Must supply:<p>
     * //@Header("refresh_token") String refreshToken
     */
    @POST("app/logout")
    Call<ResponseBody> logout();


    /**
    * Must supply:<p>
    * //@Header("refresh_token") String refreshToken
    */
    @GET("app/refresh")
    Call<TokenData> refresh();


    @GET("check")
    Call<CheckData> check(@Header("UUID") String uuid);
    //endregion

    //region Attendee
    //region Attendee - Actions
    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/participator/credentials")
    Call<CredentialData> attendeeGetCredentials();


    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @POST("app/participator/credentials")
    @Headers({"Accept: application/json"})
    Call<ResponseBody> attendeeSetCredentials(@Body CredentialData credentialData);


    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @POST("app/participator/submit_qr_token")
    Call<ResponseBody> attendeeSubmitQRCode(@Query("qr_token") String qrToken);


    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/participator/code_words")
    Call<List<String>> attendeeGetCodeWords();

    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @POST("app/participator/code_words")
    @Headers({"Accept: application/json"})
    Call<ResponseBody> attendeeSetCodeWords(@Body List<String> codeWords);

    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @DELETE("app/participator/code_words")
    @Headers({"Accept: application/json"})
    Call<ResponseBody> attendeeRemoveCodeWords(@Body List<String> codeWords);
    //endregion

    //region Attendee - Invites
    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/participator/find_party")
    Call<Set<PartyData>> attendeeFindParty(@Query("code_word") String codeWord);


    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/participator/find_parties_by_code_words")
    Call<Set<PartyData>> attendeeFindAllParties();


    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @POST("app/participator/join_party")
    Call<ResponseBody> attendeeJoinParty(@Query("party_id") Integer partyId,
                                   @Query("code_word") String codeWord);


    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/participator/my_party_list")
    Call<Set<PartyData>> attendeePartiesList();
    //endregion
    //endregion

    //region Host
    //region Host - Actions
    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/host/credentials")
    Call<CredentialData> hostGetCredentials();

    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @POST("app/host/credentials")
    @Headers({"Accept: application/json"})
    Call<ResponseBody> hostSetCredentials(@Body CredentialData credentialData);

    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/host/create_qr_token")
    Call<ResponseBody> hostCreateQRCode(@Query("subject_id") Integer eventId);

    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/host/code_words")
    Call<List<String>> hostGetCodeWords();

    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @POST("app/host/code_words")
    @Headers({"Accept: application/json"})
    Call<ResponseBody> hostSetCodeWords(@Body List<String> codeWords);

    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
//    @DELETE("app/host/code_words")
    @HTTP(method = "DELETE", path = "app/host/code_words", hasBody = true)
    @Headers({"Accept: application/json"})
    Call<ResponseBody> hostRemoveCodeWords(@Body List<String> codeWords);

    //endregion

    //region Host - Invites
    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/host/find_subject")
    Call<Set<SubjectData>> hostFindEvent(@Query("code_word") String codeWord);


    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/host/find_subjects_by_code_words")
    Call<Set<SubjectData>> hostFindAllEvents();


    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @POST("app/host/join_subject")
    Call<ResponseBody> hostJoinEvent(@Query("subject_id") Integer eventId,
                                    @Query("code_word") String codeWord);


    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/host/my_subjects_list")
    Call<Set<SubjectData>> hostEventsList();
    //endregion

    //region Host - Queries
    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/host/subjects_by_date")
    Call<Set<SubjectData>> hostGetEventsByDate(@Query("timestamp") long date);


    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/host/get_party_by_subject_id")
    Call<Set<PartyData>> hostGetPartiesByEventId(@Query("subject_id") Integer eventId);


    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @GET("app/host/get_all_parties")
    Call<Set<PartyData>> hostPartiesList();

    /**
     * Must supply:<p>
     * //@Header("access_token") String accessToken
     */
    @POST("app/host/list_of_visit_times")
    @Headers({"Accept: application/json"})
    Call<Set<VisitData>> hostGetAttendance(@Query("subject_id") Integer eventId,
                                           @Query("timestamp") long timestamp,
                                           @Body ArrayList<Integer> partyIds);
    //endregion
    //endregion
}
