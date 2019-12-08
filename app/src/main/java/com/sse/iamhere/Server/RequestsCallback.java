package com.sse.iamhere.Server;

import androidx.annotation.CallSuper;

import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.Body.CredentialData;
import com.sse.iamhere.Server.Body.PartyBrief;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Server.Body.PartyDataExtra;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.Body.TokenData;
import com.sse.iamhere.Server.Body.VisitData;

import java.util.List;
import java.util.Set;

//Common
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

interface GetCodeWordsCallback {
    void onGetCodeWordsSuccess(List<String> string);
}
interface SetCodeWordsCallback {
    void onSetCodeWordsSuccess(String string);
}
interface RemoveCodeWordsCallback {
    void onRemoveCodeWordsSuccess(String string);
}

interface QRCodeCallback {
    void onQRCodeSuccess(String string);
}

interface GetUserCallback {
    void onGetUser(CredentialData userData);
}


//Attendee
interface FindPartyCallback {
    void onFindPartySuccess(Set<PartyData> partyData);
}
interface FindAllPartiesCallback {
    void onFindAllPartiesSuccess(Set<PartyData> partyData);
}
interface JoinPartyCallback {
    void onJoinPartySuccess(String string);
}
interface LeavePartyCallback {
    void onLeavePartySuccess(String string);
}


interface AttendeePartiesListCallback {
    void onAttendeePartiesListSuccess(Set<PartyData> partyData);
}
interface AttendeeGetEventsByDateCallback {
    void onAttendeeGetEventsByDateSuccess(Set<SubjectData> eventData);
}
interface AttendeeGetVisitsByDateCallback {
    void onAttendeeGetVisitsByDateSuccess(Set<VisitData> visitData);
}


//Host
interface FindEventCallback {
    void onFindEventSuccess(Set<SubjectData> eventData);
}
interface FindAllEventsCallback {
    void onFindAllEventsSuccess(Set<SubjectData> eventData);
}
interface JoinEventCallback {
    void onJoinEventSuccess(String string);
}
interface LeaveEventCallback {
    void onLeaveEventSuccess(String string);
}

interface HostEventsListCallback {
    void onHostEventsListSuccess(Set<SubjectData> eventData);
}
interface HostGetEventsByDateCallback {
    void onHostGetEventsByDateSuccess(Set<SubjectData> eventData);
}
interface HostGetPartiesByEventIdCallback {
    void onHostGetPartiesByEventIdSuccess(Set<PartyDataExtra> partyData);
}
interface HostPartiesListCallback {
    void onHostPartiesListSuccess(Set<PartyData> partyData);
}
interface HostGetAttendanceCallback {
    void onHostGetAttendanceSuccess(Set<PartyBrief> visits);
}
interface HostGetPartyCallback {
    void onHostGetPartySuccess(PartyDataExtra party);
}
interface HostGetEventCallback {
    void onHostGetEventSuccess(SubjectData event);
}
interface HostSendAnnouncementCallback {
    void onHostSendAnnouncementSuccess(String string);
}


//General
interface FailureCallback {
    void onFailure(int errorCode);
}
interface CompleteCallback {
    void onComplete(boolean failed, Integer failCode);
}

public class RequestsCallback implements
        FailureCallback, CompleteCallback,
        RegisterCallback, LoginCallback, LogoutCallback, RefreshCallback, CheckCallback,

        GetCredentialsCallback, SetCredentialsCallback,
        GetCodeWordsCallback, SetCodeWordsCallback, RemoveCodeWordsCallback,
        QRCodeCallback, GetUserCallback,

        FindPartyCallback, FindAllPartiesCallback, JoinPartyCallback, LeavePartyCallback,
        AttendeePartiesListCallback, AttendeeGetEventsByDateCallback, AttendeeGetVisitsByDateCallback,

        FindEventCallback, FindAllEventsCallback, JoinEventCallback, LeaveEventCallback,
        HostEventsListCallback, HostGetEventsByDateCallback, HostGetPartiesByEventIdCallback, HostPartiesListCallback,
        HostGetAttendanceCallback, HostGetPartyCallback, HostGetEventCallback, HostSendAnnouncementCallback {

    //Common
    @CallSuper @Override public void onRegisterSuccess() {
		onComplete(false, null);
	}
    @CallSuper @Override public void onLoginSuccess() {
        onComplete(false, null);
    }
    @CallSuper @Override public void onLogoutSuccess() {
        onComplete(false, null);
    }
    @CallSuper @Override public void onRefreshSuccess(TokenData renewedTokenData) {
		onComplete(false, null);	
}
    @CallSuper @Override public void onCheckSuccess(CheckData checkResult) {
		onComplete(false, null);
	}

    @CallSuper @Override public void onGetCredentialsSuccess(CredentialData credentialData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onSetCredentialsSuccess(String string) {
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

    @CallSuper @Override public void onQRCodeSuccess(String string) {
        onComplete(false, null);
    }
    @CallSuper @Override public void onGetUser(CredentialData userData) {
        onComplete(false, null);
    }


    //Attendee
    @CallSuper @Override public void onFindPartySuccess(Set<PartyData> partyData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onFindAllPartiesSuccess(Set<PartyData> partyData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onJoinPartySuccess(String string) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onLeavePartySuccess(String string) {
		onComplete(false, null);
	}

    @CallSuper @Override public void onAttendeePartiesListSuccess(Set<PartyData> partyData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onAttendeeGetEventsByDateSuccess(Set<SubjectData> eventData) {
        onComplete(false, null);
    }
    @CallSuper @Override public void onAttendeeGetVisitsByDateSuccess(Set<VisitData> visitData) {
        onComplete(false, null);

    }


    //Host
    @CallSuper @Override public void onFindEventSuccess(Set<SubjectData> eventData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onFindAllEventsSuccess(Set<SubjectData> eventData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onJoinEventSuccess(String string) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onLeaveEventSuccess(String string) {
		onComplete(false, null);
	}

    @CallSuper @Override public void onHostEventsListSuccess(Set<SubjectData> eventData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onHostGetEventsByDateSuccess(Set<SubjectData> eventData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onHostGetPartiesByEventIdSuccess(Set<PartyDataExtra> partyData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onHostPartiesListSuccess(Set<PartyData> partyData) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onHostGetAttendanceSuccess(Set<PartyBrief> visits) {
		onComplete(false, null);
	}
    @CallSuper @Override public void onHostGetPartySuccess(PartyDataExtra party) {
        onComplete(false, null);
    }
    @CallSuper @Override public void onHostGetEventSuccess(SubjectData event) {
        onComplete(false, null);
    }
    @CallSuper @Override public void onHostSendAnnouncementSuccess(String string) {
        onComplete(false, null);
    }


    //General
    @CallSuper @Override public void onFailure(int errorCode) {
        onComplete(true, errorCode);
    }
    @Override public void onComplete(boolean failed, Integer failCode) { }
}



