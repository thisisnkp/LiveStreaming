package com.example.rayzi.models;

public class GiftRoot_dummy {
    public static int IMAGE = 1, LOTTIE = 2, GIF = 3;
    int url;
    int coin, type;
    private int id;

    public GiftRoot_dummy() {
    }

    public GiftRoot_dummy(int id, int url, int coin, int type) {
        this.id = id;
        this.url = url;
        this.coin = coin;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUrl() {
        return url;
    }

    public void setUrl(int url) {
        this.url = url;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
