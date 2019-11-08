package com.sse.iamhere.Server;

import com.sse.iamhere.Server.Body.CheckData;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Server.Body.TokenData;

interface RegisterCallback {
    void onRegisterSuccess();
    void onRegisterFailure(int errorCode);
}

interface LoginCallback {
    void onLoginSuccess();
    void onLoginFailure(int errorCode);
}

interface RefreshCallback {
    void onRefreshSuccess(TokenData renewedTokenData);
    void onRefreshFailure(int errorCode);
}


interface CheckCallback {
    void onCheckSuccess(CheckData checkResult);
    void onCheckFailure(int errorCode);
}

interface FindPartyCallback {
    void onParticipatorSuccess(PartyData partyData);
    void onParticipatorFailure(int errorCode);
}


public class RequestCallback implements RegisterCallback, LoginCallback, RefreshCallback,
        CheckCallback, FindPartyCallback {
    @Override
    public void onRegisterSuccess() { }

    @Override
    public void onRegisterFailure(int errorCode) {}


    @Override
    public void onLoginSuccess() { }

    @Override
    public void onLoginFailure(int errorCode) { }


    @Override
    public void onRefreshSuccess(TokenData renewedTokenData) {}

    @Override
    public void onRefreshFailure(int errorCode) {}


    @Override
    public void onCheckSuccess(CheckData checkResult) {}

    @Override
    public void onCheckFailure(int errorCode) {}


    @Override
    public void onParticipatorSuccess(PartyData partyData) {}

    @Override
    public void onParticipatorFailure(int errorCode) {}
}

