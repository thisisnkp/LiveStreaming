package com.example.rayzi.socket;

public interface CallHandler {

    void onCallRequest(Object[] args);

    void onCallReceive(Object[] args);

    void onCallConfirm(Object[] args);

    void onCallAnswer(Object[] args);

    void onCallCancel(Object[] args);

    void onCallDisconnect(Object[] args);

}
