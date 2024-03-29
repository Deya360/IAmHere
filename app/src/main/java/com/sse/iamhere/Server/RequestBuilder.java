package com.sse.iamhere.Server;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sse.iamhere.Server.Body.CredentialData;
import com.sse.iamhere.Server.Body.PartyBrief;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Server.Body.PartyDataExtra;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.Body.VisitData;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.InternetUtil;
import com.sse.iamhere.Utils.PreferencesUtil;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sse.iamhere.Utils.Constants.RQM_EC.NO_INTERNET_CONNECTION;
import static com.sse.iamhere.Utils.Constants.TOKEN_NONE;
import static com.sse.iamhere.Utils.ServerConstants.REQUEST_PREFIX;

public class RequestBuilder {
    private static final int FAILED_DEPENDENCY = 424;
    private LinkedList<Runnable> buildQueue = new LinkedList<>() ;

    private Requests requestsI;
    private void setRequestsI(Requests requestsI) {
        this.requestsI = requestsI;
    }

    private String token;
    private void setToken(String token) {
        this.token = token;
    }

    private int tokenType = TOKEN_NONE;
    private void setTokenType(int tokenType) {
        this.tokenType = tokenType;
    }

    private RequestsCallback mCallback;
    /* This is a null safe getter that will return a new callback instead of a null one*/
    private RequestsCallback getCallback() {
        if (mCallback==null) {
            return new RequestsCallback();
        }
        return mCallback;
    }

    private void runNext() {
        buildQueue.pollFirst().run();
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

    /**
     * This builder class must be built in the following order:
     *   1. setCallback (skipable)
     *   2. checkInternet (skipable)
     *   3. attachToken (skipable)
     *   4. callRequest
     *   5. call
    * */
    public RequestBuilder() { }

    public RequestBuilder setCallback(RequestsCallback mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    public RequestBuilder checkInternet(Context context) {
        buildQueue.add(() -> {
            new InternetUtil(new InternetUtil.InternetResponse() {
                @Override
                public void isConnected() {
                    runNext();
                }

                @Override
                public void notConnected() {
                    if (mCallback!=null) {
                        mCallback.onFailure(NO_INTERNET_CONNECTION);
                    }
                }
            }).hasInternetConnection(context);
        });
        return this;
    }

    public RequestBuilder attachToken(Context context, int tokenType) {
        buildQueue.add(() -> {
            TokenProvider.getUsableToken(context, tokenType,
                PreferencesUtil.getRole(context, Constants.Role.NONE), new TokenProvider.TokenProviderCallback() {
                    @Override
                    public void onSuccess(int token_type, String token) {
                        setTokenType(token_type);
                        setToken(token);
                        runNext();
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        if (mCallback!=null) {
                            mCallback.onFailure(errorCode);
                        }
                    }
                });
        });
        return this;
    }

    public void callRequest(Runnable qr) {
        buildQueue.add(() -> {
            setRequestsI(ServiceGen.createService(Requests.class, REQUEST_PREFIX, token, tokenType));
            runNext();
        });
        buildQueue.add(qr);
        runNext();
    }

    //region Attendee
    //region Attendee - Credentials

    /**
     * Must supply Access Token
     */
    public void attendeeGetCredentials() {
        requestsI.attendeeGetCredentials().enqueue(new Callback<CredentialData>() {
            @Override
            public void onResponse(@NonNull Call<CredentialData> call, @NonNull Response<CredentialData> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeGetCredentials:onResponse - Code: " + response.code());
                    getCallback().onGetCredentialsSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeGetCredentials:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.GET_CREDENTIALS_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.GET_CREDENTIALS_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CredentialData> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeGetCredentials:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.GET_CREDENTIALS_UNKNOWN);
            }
        });
    }
    /**
     * Must supply Access Token
     */
    public void attendeeSetCredentials(CredentialData credentialData) {
        requestsI.attendeeSetCredentials(credentialData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeSetCredentials:onResponse - Code: " + response.code());
                    getCallback().onSetCredentialsSuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeSetCredentials:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.SET_CREDENTIALS_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.SET_CREDENTIALS_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeSetCredentials:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.SET_CREDENTIALS_UNKNOWN);
            }
        });

    }

    //endregion

    //region Attendee - Invites

    /**
     * Must supply Access Token
     */
    public void attendeeGetCodeWords() {
        requestsI.attendeeGetCodeWords().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeGetCodeWords:onResponse - Code: " + response.code());
                    getCallback().onGetCodeWordsSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeGetCodeWords:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeGetCodeWords:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void attendeeSetCodeWords(List<String> codeWords) {
        requestsI.attendeeSetCodeWords(codeWords).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeSetCodeWords:onResponse - Code: " + response.code());
                    getCallback().onSetCodeWordsSuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeSetCodeWords:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeSetCodeWords:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void attendeeRemoveCodeWords(List<String> codeWords) {
        requestsI.attendeeRemoveCodeWords(codeWords).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeRemoveCodeWords:onResponse - Code: " + response.code());
                    getCallback().onRemoveCodeWordsSuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeRemoveCodeWords:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeRemoveCodeWords:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void attendeeFindAllParties() {
        requestsI.attendeeFindAllParties().enqueue(new Callback<Set<PartyData>>() {
            @Override
            public void onResponse(@NonNull Call<Set<PartyData>> call, @NonNull Response<Set<PartyData>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeFindAllParties:onResponse - Code: " + response.code());
                    getCallback().onFindAllPartiesSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeFindAllParties:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.FIND_BY_CODES_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.FIND_BY_CODES_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<PartyData>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeFindAllParties:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.FIND_BY_CODES_UNKNOWN);
            }
        });
    }

    //endregion

    //region Attendee - Party

    /**
     * Must supply Access Token
     */
    public void attendeeFindParty(String codeWord) {
        requestsI.attendeeFindParty(codeWord).enqueue(new Callback<Set<PartyData>>() {
            @Override
            public void onResponse(@NonNull Call<Set<PartyData>> call, @NonNull Response<Set<PartyData>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeFindParty:onResponse - Code: " + response.code());
                    getCallback().onFindPartySuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeFindParty:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));
                    getCallback().onFailure(Constants.RQM_EC.FIND_BY_CODE_UNKNOWN);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<PartyData>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeFindParty:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.FIND_BY_CODE_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void attendeeJoinParty(Integer partyId, String codeWord, String deviceToken) {
        requestsI.attendeeJoinParty(partyId, codeWord, deviceToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeJoinParty:onResponse - Code: " + response.code());
                    getCallback().onJoinPartySuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeJoinParty:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    switch (response.code()) {
                        case HttpURLConnection.HTTP_CONFLICT:
                            getCallback().onFailure(Constants.RQM_EC.JOIN_USER_NOT_FOUND);
                            break;
                        case FAILED_DEPENDENCY:
                            getCallback().onFailure(Constants.RQM_EC.JOIN_CODE_MISMATCH);
                            break;
                        default:
                            getCallback().onFailure(Constants.RQM_EC.JOIN_UNKNOWN);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeJoinParty:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.JOIN_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void attendeeLeaveParty(Integer partyId, String deviceToken) {
        requestsI.attendeeLeaveParty(partyId, deviceToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeLeaveParty:onResponse - Code: " + response.code());
                    getCallback().onLeavePartySuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeLeaveParty:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    switch (response.code()) {
                        case HttpURLConnection.HTTP_CONFLICT:
                            getCallback().onFailure(Constants.RQM_EC.LEAVE_USER_NOT_FOUND);
                            break;
                        case FAILED_DEPENDENCY:
                            getCallback().onFailure(Constants.RQM_EC.LEAVE_PARTY_NOT_FOUND);
                            break;
                        default:
                            getCallback().onFailure(Constants.RQM_EC.LEAVE_UNKNOWN);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeLeaveParty:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.LEAVE_UNKNOWN);
            }
        });
    }

    //endregion

    //region Attendee - Queries

    /**
     * Must supply Access Token
     */
    public void attendeePartiesList() {
        requestsI.attendeePartiesList().enqueue(new Callback<Set<PartyData>>() {
            @Override
            public void onResponse(@NonNull Call<Set<PartyData>> call, @NonNull Response<Set<PartyData>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeePartiesList:onResponse - Code: " + response.code());
                    getCallback().onAttendeePartiesListSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeePartiesList:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    getCallback().onFailure(Constants.RQM_EC.ATTENDEE_GET_PARTIES_UNKNOWN);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<PartyData>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeePartiesList:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.ATTENDEE_GET_PARTIES_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void attendeeGetEventsByDate(long date) {
        requestsI.attendeeGetEventsByDate(date).enqueue(new Callback<Set<SubjectData>>() {
            @Override
            public void onResponse(@NonNull Call<Set<SubjectData>> call, @NonNull Response<Set<SubjectData>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeGetEventsByDate:onResponse - Code: " + response.code());
                    getCallback().onAttendeeGetEventsByDateSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeGetEventsByDate:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.ATTENDEE_GET_EVENTS_BY_DATE_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.ATTENDEE_GET_EVENTS_BY_DATE_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<SubjectData>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeGetEventsByDate:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.ATTENDEE_GET_EVENTS_BY_DATE_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void attendeeGetVisitsByDate(long date) {
        requestsI.attendeeGetVisitsByDate(date).enqueue(new Callback<Set<VisitData>>() {
            @Override
            public void onResponse(@NonNull Call<Set<VisitData>> call, @NonNull Response<Set<VisitData>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeGetVisitsByDate:onResponse - Code: " + response.code());
                    getCallback().onAttendeeGetVisitsByDateSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeGetVisitsByDate:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    getCallback().onFailure(Constants.RQM_EC.ATTENDEE_GET_VISITS_BY_DATE_UNKNOWN);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<VisitData>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeGetVisitsByDate:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.ATTENDEE_GET_VISITS_BY_DATE_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void attendeeGetUserById(int userId, String userType) {
        requestsI.attendeeGetUserById(userId, userType).enqueue(new Callback<CredentialData>() {
            @Override
            public void onResponse(@NonNull Call<CredentialData> call, @NonNull Response<CredentialData> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeGetUserById:onResponse - Code: " + response.code());
                    getCallback().onGetUser(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeGetUserById:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    switch (response.code()) {
                        case HttpURLConnection.HTTP_CONFLICT:
                            getCallback().onFailure(Constants.RQM_EC.GET_USER_USER_NOT_FOUND);
                            break;
                        case HttpURLConnection.HTTP_BAD_REQUEST:
                            getCallback().onFailure(Constants.RQM_EC.GET_USER_INVALID);
                            break;
                        default:
                            getCallback().onFailure(Constants.RQM_EC.GET_USER_UNKNOWN);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CredentialData> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeGetUserById:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.GET_USER_UNKNOWN);
            }
        });
    }

    //endregion

    //region Attendee - Actions

    /**
     * Must supply Access Token
     */
    public void attendeeSubmitQRCode(String qrToken) {
        requestsI.attendeeSubmitQRCode(qrToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "attendeeSubmitQRCode:onResponse - Code: " + response.code());
                    getCallback().onQRCodeSuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("attendeeSubmitQRCode:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    switch (response.code()) {
                        case HttpURLConnection.HTTP_CONFLICT:
                            getCallback().onFailure(Constants.RQM_EC.QR_CODE_USER_NOT_FOUND);
                            break;

                        case HttpURLConnection.HTTP_BAD_REQUEST:
                            getCallback().onFailure(Constants.RQM_EC.QR_CODE_SUBJECT_NOT_FOUND);
                            break;

                        default:
                            getCallback().onFailure(Constants.RQM_EC.QR_CODE_UNKNOWN);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "attendeeSubmitQRCode:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.QR_CODE_UNKNOWN);
            }
        });
    }

    //endregion
    //endregion

    //region Host
    //region Host - Credentials

    /**
     * Must supply Access Token
     */
    public void hostGetCredentials() {
        requestsI.hostGetCredentials().enqueue(new Callback<CredentialData>() {
            @Override
            public void onResponse(@NonNull Call<CredentialData> call, @NonNull Response<CredentialData> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostGetCredentials:onResponse - Code: " + response.code());
                    getCallback().onGetCredentialsSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostGetCredentials:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.GET_CREDENTIALS_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.GET_CREDENTIALS_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CredentialData> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostGetCredentials:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.GET_CREDENTIALS_UNKNOWN);
            }
        });
    }
    /**
     * Must supply Access Token
     */
    public void hostSetCredentials(CredentialData credentialData) {
        requestsI.hostSetCredentials(credentialData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostSetCredentials:onResponse - Code: " + response.code());
                    getCallback().onSetCredentialsSuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostSetCredentials:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.SET_CREDENTIALS_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.SET_CREDENTIALS_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostSetCredentials:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.SET_CREDENTIALS_UNKNOWN);
            }
        });
    }

    //endregion

    //region Host - Invites

    /**
     * Must supply Access Token
     */
    public void hostGetCodeWords() {
        requestsI.hostGetCodeWords().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostGetCodeWords:onResponse - Code: " + response.code());
                    getCallback().onGetCodeWordsSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostGetCodeWords:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostGetCodeWords:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostSetCodeWords(List<String> codeWords) {
        requestsI.hostSetCodeWords(codeWords).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostSetCodeWords:onResponse - Code: " + response.code());
                    getCallback().onSetCodeWordsSuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostSetCodeWords:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostSetCodeWords:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostRemoveCodeWords(List<String> codeWords) {
        requestsI.hostRemoveCodeWords(codeWords).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostRemoveCodeWords:onResponse - Code: " + response.code());
                    getCallback().onRemoveCodeWordsSuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostRemoveCodeWords:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostRemoveCodeWords:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.CODE_WORDS_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostFindEvent(String codeWord) {
        requestsI.hostFindEvent(codeWord).enqueue(new Callback<Set<SubjectData>>() {
            @Override
            public void onResponse(@NonNull Call<Set<SubjectData>> call, @NonNull Response<Set<SubjectData>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostFindEvent:onResponse - Code: " + response.code());
                    getCallback().onFindEventSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostFindEvent:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));
                    getCallback().onFailure(Constants.RQM_EC.FIND_BY_CODE_UNKNOWN);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<SubjectData>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostFindEvent:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.FIND_BY_CODE_UNKNOWN);
            }
        });
    }

    //endregion

    //region Host - Event

    /**
     * Must supply Access Token
     */
    public void hostEventsList() {
        requestsI.hostEventsList().enqueue(new Callback<Set<SubjectData>>() {
            @Override
            public void onResponse(@NonNull Call<Set<SubjectData>> call, @NonNull Response<Set<SubjectData>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostEventsList:onResponse - Code: " + response.code());
                    getCallback().onHostEventsListSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostEventsList:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    getCallback().onFailure(Constants.RQM_EC.HOST_GET_EVENTS_UNKNOWN);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<SubjectData>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostEventsList:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.HOST_GET_EVENTS_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostJoinEvent(Integer eventId, String codeWord) {
        requestsI.hostJoinEvent(eventId, codeWord).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostJoinEvent:onResponse - Code: " + response.code());
                    getCallback().onJoinEventSuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostJoinEvent:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    switch (response.code()) {
                        case HttpURLConnection.HTTP_CONFLICT:
                            getCallback().onFailure(Constants.RQM_EC.JOIN_USER_NOT_FOUND);
                            break;
                        case FAILED_DEPENDENCY:
                            getCallback().onFailure(Constants.RQM_EC.JOIN_CODE_MISMATCH);
                            break;
                        default:
                            getCallback().onFailure(Constants.RQM_EC.JOIN_UNKNOWN);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostJoinEvent:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.JOIN_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostLeaveEvent(Integer partyId) {
        requestsI.hostLeaveEvent(partyId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostLeaveEvent:onResponse - Code: " + response.code());
                    getCallback().onLeavePartySuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostLeaveEvent:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    switch (response.code()) {
                        case HttpURLConnection.HTTP_CONFLICT:
                            getCallback().onFailure(Constants.RQM_EC.LEAVE_USER_NOT_FOUND);
                            break;
                        case FAILED_DEPENDENCY:
                            getCallback().onFailure(Constants.RQM_EC.LEAVE_PARTY_NOT_FOUND);
                            break;
                        default:
                            getCallback().onFailure(Constants.RQM_EC.LEAVE_UNKNOWN);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostLeaveEvent:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.LEAVE_UNKNOWN);
            }
        });
    }

    //endregion

    //region Host - Queries

    /**
     * Must supply Access Token
     */
    public void hostGetPartiesByEventId(Integer eventId) {
        requestsI.hostGetPartiesByEventId(eventId).enqueue(new Callback<Set<PartyDataExtra>>() {
            @Override
            public void onResponse(@NonNull Call<Set<PartyDataExtra>> call, @NonNull Response<Set<PartyDataExtra>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostGetPartiesByEventId:onResponse - Code: " + response.code());
                    getCallback().onHostGetPartiesByEventIdSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostGetPartiesByEventId:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.HOST_GET_PARTIES_BY_EVENT_ID_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.HOST_GET_PARTIES_BY_EVENT_ID_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<PartyDataExtra>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostGetPartiesByEventId:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.HOST_GET_PARTIES_BY_EVENT_ID_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostFindAllEvents() {
        requestsI.hostFindAllEvents().enqueue(new Callback<Set<SubjectData>>() {
            @Override
            public void onResponse(@NonNull Call<Set<SubjectData>> call, @NonNull Response<Set<SubjectData>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostFindAllEvents:onResponse - Code: " + response.code());
                    getCallback().onFindAllEventsSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostFindAllEvents:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.FIND_BY_CODES_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.FIND_BY_CODES_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<SubjectData>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostFindAllEvents:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.FIND_BY_CODES_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostPartiesList() {
        requestsI.hostPartiesList().enqueue(new Callback<Set<PartyData>>() {
            @Override
            public void onResponse(@NonNull Call<Set<PartyData>> call, @NonNull Response<Set<PartyData>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostPartiesList:onResponse - Code: " + response.code());
                    getCallback().onHostPartiesListSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostPartiesList:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    getCallback().onFailure(Constants.RQM_EC.HOST_GET_PARTIES_UNKNOWN);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<PartyData>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostPartiesList:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.HOST_GET_PARTIES_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostGetEventsByDate(long date) {
        requestsI.hostGetEventsByDate(date).enqueue(new Callback<Set<SubjectData>>() {
            @Override
            public void onResponse(@NonNull Call<Set<SubjectData>> call, @NonNull Response<Set<SubjectData>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostGetEventsByDate:onResponse - Code: " + response.code());
                    getCallback().onHostGetEventsByDateSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostGetEventsByDate:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                        getCallback().onFailure(Constants.RQM_EC.HOST_GET_EVENTS_BY_DATE_USER_NOT_FOUND);
                    } else {
                        getCallback().onFailure(Constants.RQM_EC.HOST_GET_EVENTS_BY_DATE_UNKNOWN);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<SubjectData>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostGetEventsByDate:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.HOST_GET_EVENTS_BY_DATE_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostGetEventById(Integer eventId) {
        requestsI.hostGetEventById(eventId).enqueue(new Callback<SubjectData>() {
            @Override
            public void onResponse(@NonNull Call<SubjectData> call, @NonNull Response<SubjectData> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostGetEventById:onResponse - Code: " + response.code());
                    getCallback().onHostGetEventSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostGetEventById:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    getCallback().onFailure(Constants.RQM_EC.HOST_GET_EVENT_UNKNOWN);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubjectData> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostGetEventById:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.HOST_GET_EVENT_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostGetAttendance(Integer eventId, long timestamp, ArrayList<Integer> partyIds) {
        requestsI.hostGetAttendance(eventId, timestamp, partyIds).enqueue(new Callback<Set<PartyBrief>>() {
            @Override
            public void onResponse(@NonNull Call<Set<PartyBrief>> call, @NonNull Response<Set<PartyBrief>> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostGetAttendance:onResponse - Code: " + response.code());
                    getCallback().onHostGetAttendanceSuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostGetAttendance:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    getCallback().onFailure(Constants.RQM_EC.HOST_GET_ATTENDANCE_UNKNOWN);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Set<PartyBrief>> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostGetAttendance:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.HOST_GET_ATTENDANCE_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostGetPartyById(Integer partyId) {
        requestsI.hostGetPartyById(partyId).enqueue(new Callback<PartyDataExtra>() {
            @Override
            public void onResponse(@NonNull Call<PartyDataExtra> call, @NonNull Response<PartyDataExtra> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostGetPartyById:onResponse - Code: " + response.code());
                    getCallback().onHostGetPartySuccess(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostGetPartyById:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    getCallback().onFailure(Constants.RQM_EC.HOST_GET_PARTY_UNKNOWN);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PartyDataExtra> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostGetPartyById:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.HOST_GET_PARTY_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostGetUserById(int userId, String userType) {
        requestsI.hostGetUserById(userId, userType).enqueue(new Callback<CredentialData>() {
            @Override
            public void onResponse(@NonNull Call<CredentialData> call, @NonNull Response<CredentialData> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostGetUserById:onResponse - Code: " + response.code());
                    getCallback().onGetUser(response.body());

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostGetUserById:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    switch (response.code()) {
                        case HttpURLConnection.HTTP_CONFLICT:
                            getCallback().onFailure(Constants.RQM_EC.GET_USER_USER_NOT_FOUND);
                            break;
                        case HttpURLConnection.HTTP_BAD_REQUEST:
                            getCallback().onFailure(Constants.RQM_EC.GET_USER_INVALID);
                            break;
                        default:
                            getCallback().onFailure(Constants.RQM_EC.GET_USER_UNKNOWN);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CredentialData> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostGetUserById:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.GET_USER_UNKNOWN);
            }
        });
    }

    //endregion

    //region Host - Actions

    /**
     * Must supply Access Token
     */
    public void hostCreateQRCode(Integer eventId) {
        requestsI.hostCreateQRCode(eventId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostCreateQRCode:onResponse - Code: " + response.code());
                    getCallback().onQRCodeSuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostCreateQRCode:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    switch (response.code()) {
                        case HttpURLConnection.HTTP_CONFLICT:
                            getCallback().onFailure(Constants.RQM_EC.QR_CODE_USER_NOT_FOUND);
                            break;

                        case HttpURLConnection.HTTP_BAD_REQUEST:
                            getCallback().onFailure(Constants.RQM_EC.QR_CODE_SUBJECT_NOT_FOUND);
                            break;

                        default:
                            getCallback().onFailure(Constants.RQM_EC.QR_CODE_UNKNOWN);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostCreateQRCode:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.QR_CODE_UNKNOWN);
            }
        });
    }

    /**
     * Must supply Access Token
     */
    public void hostSendAnnouncement(Integer eventId, String messageBody) {
        requestsI.hostSendAnnouncement(eventId, messageBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RequestBuilder", "hostSendAnnouncement:onResponse - Code: " + response.code());
                    getCallback().onHostSendAnnouncementSuccess(extract(response.body(), response.message()));

                } else {
                    Log.i("RequestBuilder",
                          String.format("hostSendAnnouncement:onResponse - Code: %d\nMsg: %s",
                                        response.code(), extract(response.errorBody(), response.message())));

                    switch (response.code()) {
                        case HttpURLConnection.HTTP_CONFLICT:
                            getCallback().onFailure(Constants.RQM_EC.HOST_SEND_ANNOUNCEMENT_USER_NOT_FOUND);
                            break;

                        case FAILED_DEPENDENCY:
                            getCallback().onFailure(Constants.RQM_EC.HOST_SEND_ANNOUNCEMENT_SUBJECT_NOT_FOUND);
                            break;

                        default:
                            getCallback().onFailure(Constants.RQM_EC.HOST_SEND_ANNOUNCEMENT_UNKNOWN);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.i("RequestBuilder", "hostSendAnnouncement:onFailure - Msg: " + t.getMessage());
                getCallback().onFailure(Constants.RQM_EC.HOST_SEND_ANNOUNCEMENT_UNKNOWN);
            }
        });
    }

    //endregion
    //endregion
}