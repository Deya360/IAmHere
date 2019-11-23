package com.sse.iamhere.Server;

import androidx.annotation.CallSuper;

import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.Body.CredentialData;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.Body.TokenData;
import com.sse.iamhere.Server.Body.VisitData;

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
interface HostGetAttendanceCallback {
    void onHostGetAttendanceSuccess(Set<VisitData> visits);
}


interface FailureCallback {
    void onFailure(int errorCode);
}
interface CompleteCallback {
    void onComplete(boolean failed, Integer failCode);
}

public class RequestsCallback implements
        FailureCallback, CompleteCallback,
        RegisterCallback, LoginCallback, LogoutCallback, RefreshCallback, CheckCallback,
        GetCredentialsCallback, SetCredentialsCallback, QRCodeCallback,
        GetCodeWordsCallback, SetCodeWordsCallback, RemoveCodeWordsCallback,
        FindPartyCallback, FindAllPartiesCallback, JoinPartyCallback,
        AttendeePartiesListCallback,
        FindEventCallback, FindAllEventsCallback, JoinEventCallback,
        HostEventsListCallback, HostGetEventsByDateCallback, HostGetPartiesByEventIdCallback, HostPartiesListCallback,
        HostGetAttendanceCallback {

    @CallSuper @Override public void onRegisterSuccess() {
		onComplete(false, null);
	}
    @CallSuper @Override public void onLoginSuccess() {
        onComplete(false, null);
    }
    @CallSuper @Override public void onRefreshSuccess(TokenData renewedTokenData) {
		onComplete(false, null);	
}
    @CallSuper @Override public void onCheckSuccess(CheckData checkResult) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onLogoutSuccess() {
		onComplete(false, null);
	}

    @CallSuper @Override public void onGetCredentialsSuccess(CredentialData credentialData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onSetCredentialsSuccess(String string) {
		onComplete(false, null);
	}

    @CallSuper @Override public void onQRCodeSuccess(String string) {
		onComplete(false, null);
	}

    @CallSuper @Override public void onGetCodeWordsSuccess(List<String> codes) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onSetCodeWordsSuccess(String msg) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onRemoveCodeWordsSuccess(String msg) {
		onComplete(false, null);
	}

    @CallSuper @Override public void onFindPartySuccess(Set<PartyData> partyData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onFindAllPartiesSuccess(Set<PartyData> partyData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onJoinPartySuccess(String string) {
		onComplete(false, null);
	}

    @CallSuper @Override public void onAttendeePartiesListSuccess(Set<PartyData> partyData) {
		onComplete(false, null);
	}

    @CallSuper @Override public void onFindEventSuccess(Set<SubjectData> subjectData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onFindAllEventsSuccess(Set<SubjectData> subjectData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onJoinEventSuccess(String string) {
		onComplete(false, null);
	}

    @CallSuper @Override public void onHostEventsListSuccess(Set<SubjectData> subjectData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onHostGetEventsByDateSuccess(Set<SubjectData> subjectData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onHostGetPartiesByEventIdSuccess(Set<PartyData> partyData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onHostPartiesListSuccess(Set<PartyData> partyData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onHostGetAttendanceSuccess(Set<VisitData> visits) {
		onComplete(false, null);
	}

    @CallSuper @Override public void onFailure(int errorCode) {
        onComplete(true, errorCode);
    }

    @Override public void onComplete(boolean failed, Integer failCode) { }
}



