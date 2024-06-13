package com.example.rayzi.socket;

public interface SocketConnectHandler {

    void onConnect();

    void onDisconnect();

    void onReconnecting();

    void onReconnected(Object[] args);

}
