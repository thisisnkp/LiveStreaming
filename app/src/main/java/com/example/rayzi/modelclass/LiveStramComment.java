package com.example.rayzi.modelclass;

public class LiveStramComment {
    String liveStreamingId = "";
    UserRoot.User user;
    String comment;
    boolean isJoined = false;

    public LiveStramComment(String liveStreamingId, String comment, UserRoot.User userDummy, boolean isJoined) {
        this.comment = comment;
        this.liveStreamingId = liveStreamingId;
        this.user = userDummy;
        this.isJoined = isJoined;
    }

    public LiveStramComment() {
    }

    public String getLiveStreamingId() {
        return liveStreamingId;
    }

    public void setLiveStreamingId(String liveStreamingId) {
        this.liveStreamingId = liveStreamingId;
    }

    @Override
    public String toString() {
        return "LiveStramComment{" +
                "liveStreamingId='" + liveStreamingId + '\'' +
                ", user=" + user +
                ", comment='" + comment + '\'' +
                ", isJoined=" + isJoined +
                '}';
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public UserRoot.User getUser() {
        return user;
    }

    public void setUser(UserRoot.User userDummy) {
        this.user = userDummy;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean joined) {
        isJoined = joined;
    }
}
