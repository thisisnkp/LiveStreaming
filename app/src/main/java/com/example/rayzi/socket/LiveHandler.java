package com.example.rayzi.socket;

public interface LiveHandler {

    void onSimpleFilter(Object[] args);

    void onAnimatedFilter(Object[] args);

    void onGif(Object[] args);

    void onComment(Object[] args);

    void onGift(Object[] args);

    void onView(Object[] args);

    void onGetUser(Object[] args);


    void onBlock(Object[] args1);

    void onLiveRejoin(Object[] args1);
}
