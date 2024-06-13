package com.example.rayzi.modelclass;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LiveUserRoot {

    @SerializedName("message")
    private String message;

    @SerializedName("users")
    private List<UsersItem> users;

    @SerializedName("status")
    private boolean status;

    public String getMessage() {
        return message;
    }

    public List<UsersItem> getUsers() {
        return users;
    }

    public boolean isStatus() {
        return status;
    }

    public static class UsersItem {

        @SerializedName("image")
        private String image;

        @SerializedName("country")
        private String country;

        @SerializedName("diamond")
        private int diamond;

        @SerializedName("view")
        private int view;
        @SerializedName("rCoin")
        private int rCoin;
        @SerializedName("name")
        private String name;
        @SerializedName("channel")
        private String channel;
        @SerializedName("isVIP")
        private boolean isVIP;
        @SerializedName("username")
        private String username;
        @SerializedName("token")
        private String token;
        @SerializedName("isFake")
        private boolean isFake;
        @SerializedName("liveUserId")
        private String liveUserId;
        @SerializedName("countryFlagImage")
        private String countryFlagImage;
        @SerializedName("liveStreamingId")
        private String liveStreamingId;
        @SerializedName("_id")
        private String id;
        @SerializedName("link")
        private String link;

        public int getView() {
            return view;
        }

        public void setView(int view) {
            this.view = view;
        }

        @Override
        public String toString() {
            return "UsersItem{" +
                    "image='" + image + '\'' +
                    ", country='" + country + '\'' +
                    ", diamond=" + diamond +
                    ", view=" + view +
                    ", rCoin=" + rCoin +
                    ", name='" + name + '\'' +
                    ", channel='" + channel + '\'' +
                    ", isVIP=" + isVIP +
                    ", username='" + username + '\'' +
                    ", token='" + token + '\'' +
                    ", isFake=" + isFake +
                    ", liveUserId='" + liveUserId + '\'' +
                    ", liveStreamingId='" + liveStreamingId + '\'' +
                    ", id='" + id + '\'' +
                    ", link='" + link + '\'' +
                    '}';
        }

        public String getLiveStreamingId() {
            return liveStreamingId;
        }

        public void setLiveStreamingId(String liveStreamingId) {
            this.liveStreamingId = liveStreamingId;
        }

        public String getLink() {
            return link;
        }

        public boolean isFake() {
            return isFake;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public int getDiamond() {
            return diamond;
        }

        public void setDiamond(int diamond) {
            this.diamond = diamond;
        }

        public int getRCoin() {
            return rCoin;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public boolean isIsVIP() {
            return isVIP;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getLiveUserId() {
            return liveUserId;
        }

        public void setLiveUserId(String liveUserId) {
            this.liveUserId = liveUserId;
        }

        public void setrCoin(int rCoin) {
            this.rCoin = rCoin;
        }

        public void setVIP(boolean VIP) {
            isVIP = VIP;
        }

        public String getCountryFlagImage() {
            return countryFlagImage;
        }

        public void setCountryFlagImage(String countryFlagImage) {
            this.countryFlagImage = countryFlagImage;
        }
    }
}