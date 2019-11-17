package com.sse.iamhere.Server;

import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.Body.CredentialData;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.Body.TokenData;

import java.util.List;
import java.util.Set;

interface RegisterCallback {
    void onRegisterSuccess();
}
interface LoginCallback {
    void onLoginSuccess();
}
interface LogoutCallback {
    void onLogoutSuccess();
}
interface RefreshCallback {
    void onRefreshSuccess(TokenData renewedTokenData);
}
interface CheckCallback {
    void onCheckSuccess(CheckData checkResult);
}

interface GetCredentialsCallback {
    void onGetCredentialsSuccess(CredentialData credentialData);
}
interface SetCredentialsCallback {
    void onSetCredentialsSuccess(String string);
}

interface QRCodeCallback {
    void onQRCodeSuccess(String string);
}

interface GetCodeWordsCallback {
    void onGetCodeWordsSuccess(List<String> string);
}

interface SetCodeWordsCallback {
    void onSetCodeWordsSuccess(String string);
}

interface RemoveCodeWordsCallback {
    void onRemoveCodeWordsSuccess(String string);
}

interface FindPartyCallback {
    void onFindPartySuccess(Set<PartyData> partyData);
}
interface FindAllPartiesCallback {
    void onFindAllPartiesSuccess(Set<PartyData> partyData);
}
interface JoinPartyCallback {
    void onJoinPartySuccess(String string);
}

interface AttendeePartiesListCallback {
    void onAttendeePartiesListSuccess(Set<PartyData> partyData);
}


interface FindEventCallback {
    void onFindEventSuccess(Set<SubjectData> subjectData);
}
interface FindAllEventsCallback {
    void onFindAllEventsSuccess(Set<SubjectData> subjectData);
}
interface JoinEventCallback {
    void onJoinEventSuccess(String string);
}

interface HostEventsListCallback {
    void onHostEventsListSuccess(Set<SubjectData> subjectData);
}
interface HostGetEventsByDateCallback {
    void onHostGetEventsByDateSuccess(Set<SubjectData> subjectBodies);
}
interface HostGetPartiesByEventIdCallback {
    void onHostGetPartiesByEventIdSuccess(Set<PartyData> partyData);
}
interface HostPartiesListCallback {
    void onHostPartiesListSuccess(Set<PartyData> partyData);
}



interface GeneralFailureCallback {
    void onFailure(int errorCode);
}

public class RequestsCallback implements
        GeneralFailureCallback, RegisterCallback, LoginCallback, LogoutCallback, RefreshCallback, CheckCallback,
        GetCredentialsCallback, SetCredentialsCallback, QRCodeCallback,
        GetCodeWordsCallback, SetCodeWordsCallback, RemoveCodeWordsCallback,
        FindPartyCallback, FindAllPartiesCallback, JoinPartyCallback,
        AttendeePartiesListCallback,
        FindEventCallback, FindAllEventsCallback, JoinEventCallback,
        HostEventsListCallback, HostGetEventsByDateCallback, HostGetPartiesByEventIdCallback, HostPartiesListCallback{

    @Override public void onRegisterSuccess() {}
    @Override public void onLoginSuccess() {}
    @Override public void onRefreshSuccess(TokenData renewedTokenData) {}
    @Override public void onCheckSuccess(CheckData checkResult) {}
    @Override public void onLogoutSuccess() {}

    @Override public void onGetCredentialsSuccess(CredentialData credentialData) {}
    @Override public void onSetCredentialsSuccess(String string) {}

    @Override public void onQRCodeSuccess(String string) {}

    @Override public void onGetCodeWordsSuccess(List<String> string) {}
    @Override public void onSetCodeWordsSuccess(String string) {}
    @Override public void onRemoveCodeWordsSuccess(String string) {}

    @Override public void onFindPartySuccess(Set<PartyData> partyData) {}
    @Override public void onFindAllPartiesSuccess(Set<PartyData> partyData) {}
    @Override public void onJoinPartySuccess(String string) {}

    @Override public void onAttendeePartiesListSuccess(Set<PartyData> partyData) {}

    @Override public void onFindEventSuccess(Set<SubjectData> subjectData) {}
    @Override public void onFindAllEventsSuccess(Set<SubjectData> subjectData) {}
    @Override public void onJoinEventSuccess(String string) {}

    @Override public void onHostEventsListSuccess(Set<SubjectData> subjectData) {}
    @Override public void onHostGetEventsByDateSuccess(Set<SubjectData> subjectData) {}
    @Override public void onHostGetPartiesByEventIdSuccess(Set<PartyData> partyData) {}
    @Override public void onHostPartiesListSuccess(Set<PartyData> partyData) {}


    @Override public void onFailure(int errorCode) {}
}



