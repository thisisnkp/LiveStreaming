package com.example.rayzi.modelclass;

import com.google.gson.annotations.SerializedName;

public class UserRoot {

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    @SerializedName("status")
    private boolean status;

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public boolean isStatus() {
        return status;
    }

    public static class AccessibleFunction {

        @SerializedName("uploadPost")
        private boolean uploadPost;

        @SerializedName("freeCall")
        private boolean freeCall;

        @SerializedName("uploadVideo")
        private boolean uploadVideo;

        @SerializedName("cashOut")
        private boolean cashOut;

        @SerializedName("liveStreaming")
        private boolean liveStreaming;

        public boolean isUploadPost() {
            return uploadPost;
        }

        public boolean isFreeCall() {
            return freeCall;
        }

        public boolean isUploadVideo() {
            return uploadVideo;
        }

        public boolean isCashOut() {
            return cashOut;
        }

        public boolean isLiveStreaming() {
            return liveStreaming;
        }
    }

    public static class Ad {

        @SerializedName("date")
        private String date;

        @SerializedName("count")
        private int count;

        public String getDate() {
            return date;
        }

        public int getCount() {
            return count;
        }
    }

    public static class Level {

        @SerializedName("image")
        private String image;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("accessibleFunction")
        private AccessibleFunction accessibleFunction;

        @SerializedName("name")
        private String name;

        @SerializedName("_id")
        private String id;

        @SerializedName("coin")
        private int coin;

        @SerializedName("updatedAt")
        private String updatedAt;

        public String getImage() {
            return image;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public AccessibleFunction getAccessibleFunction() {
            return accessibleFunction;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public int getCoin() {
            return coin;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }

    public static class Notification {

        @SerializedName("likeCommentShare")
        private boolean likeCommentShare;

        @SerializedName("newFollow")
        private boolean newFollow;

        @SerializedName("favoriteLive")
        private boolean favoriteLive;

        @SerializedName("message")
        private boolean message;

        public boolean isLikeCommentShare() {
            return likeCommentShare;
        }

        public boolean isNewFollow() {
            return newFollow;
        }

        public boolean isFavoriteLive() {
            return favoriteLive;
        }

        public boolean isMessage() {
            return message;
        }
    }

    public static class Plan {

        @SerializedName("planId")
        private String planId;

        @SerializedName("planStartDate")
        private String planStartDate;

        public String getPlanId() {
            return planId;
        }

        public String getPlanStartDate() {
            return planStartDate;
        }
    }

    public static class User {
        @SerializedName("spentCoin")
        private int spentCoin;
        @SerializedName("analyticDate")
        private String analyticDate;

        @SerializedName("isReferral")
        private boolean isReferral;

        @SerializedName("referralCount")
        private int referralCount;

        @SerializedName("country")
        private String country;

        @SerializedName("lastLogin")
        private String lastLogin;

        @SerializedName("isBlock")
        private boolean isBlock;

        @SerializedName("gender")
        private String gender;

        @SerializedName("rCoin")
        private int rCoin;

        @SerializedName("loginType")
        private int loginType;

        @SerializedName("channel")
        private String channel;

        @SerializedName("bio")
        private String bio;

        @SerializedName("isOnline")
        private boolean isOnline;

        @SerializedName("video")
        private int video;

        @SerializedName("withdrawalRcoin")
        private int withdrawalRcoin;

        @SerializedName("notification")
        private Notification notification;
        @SerializedName("createdAt")
        private String createdAt;
        @SerializedName("post")
        private int post;
        @SerializedName("identity")
        private String identity;
        @SerializedName("referralCode")
        private String referralCode;
        @SerializedName("plan")
        private Plan plan;
        @SerializedName("fcmToken")
        private String fcmToken;
        @SerializedName("email")
        private String email;
        @SerializedName("updatedAt")
        private String updatedAt;
        @SerializedName("image")
        private String image;
        @SerializedName("isBusy")
        private boolean isBusy;
        @SerializedName("ad")
        private Ad ad;
        @SerializedName("level")
        private Level level;
        @SerializedName("ip")
        private String ip;
        @SerializedName("isVIP")
        private boolean isVIP;
        @SerializedName("token")
        private String token;
        @SerializedName("diamond")
        private int diamond;
        @SerializedName("followers")
        private int followers;
        @SerializedName("following")
        private int following;
        @SerializedName("name")
        private String name;
        @SerializedName("_id")
        private String id;
        @SerializedName("age")
        private int age;
        @SerializedName("username")
        private String username;
        @SerializedName("uniqueId")
        private String uniqueId;
        @SerializedName("isFake")
        private boolean isFake;
        @SerializedName("coverImage")
        private String coverImage;
        @SerializedName("countryFlagImage")
        private String countryFlagImage;

        public User(int spentCoin, String analyticDate, boolean isReferral, int referralCount, String country, String lastLogin, boolean isBlock, String gender, int rCoin, int loginType, String channel, String bio, boolean isOnline, int video, int withdrawalRcoin, Notification notification, String createdAt, int post, String identity, String referralCode, Plan plan, String fcmToken, String email, String updatedAt, String image, boolean isBusy, Ad ad, Level level, String ip, boolean isVIP, String token, int diamond, int followers, int following, String name, String id, int age, String username, boolean isFake, String uniqueId) {
            this.spentCoin = spentCoin;
            this.analyticDate = analyticDate;
            this.isReferral = isReferral;
            this.referralCount = referralCount;
            this.country = country;
            this.lastLogin = lastLogin;
            this.isBlock = isBlock;
            this.gender = gender;
            this.rCoin = rCoin;
            this.loginType = loginType;
            this.channel = channel;
            this.bio = bio;
            this.isOnline = isOnline;
            this.video = video;
            this.withdrawalRcoin = withdrawalRcoin;
            this.notification = notification;
            this.createdAt = createdAt;
            this.post = post;
            this.identity = identity;
            this.referralCode = referralCode;
            this.plan = plan;
            this.fcmToken = fcmToken;
            this.email = email;
            this.updatedAt = updatedAt;
            this.image = image;
            this.isBusy = isBusy;
            this.ad = ad;
            this.level = level;
            this.ip = ip;
            this.isVIP = isVIP;
            this.token = token;
            this.diamond = diamond;
            this.followers = followers;
            this.following = following;
            this.name = name;
            this.id = id;
            this.age = age;
            this.username = username;
            this.isFake = isFake;
            this.uniqueId = uniqueId;
        }

        public int getrCoin() {
            return rCoin;
        }

        public void setrCoin(int rCoin) {
            this.rCoin = rCoin;
        }

        public boolean isFake() {
            return isFake;
        }

        public boolean isIsReferral() {
            return isReferral;
        }

        public int getReferralCount() {
            return referralCount;
        }

        public String getCountry() {
            return country;
        }

        public String getLastLogin() {
            return lastLogin;
        }

        public boolean isIsBlock() {
            return isBlock;
        }

        public String getGender() {
            return gender;
        }

        public int getRCoin() {
            return rCoin;
        }

        public int getLoginType() {
            return loginType;
        }

        public String getChannel() {
            return channel;
        }

        public String getBio() {
            return bio;
        }

        public boolean isIsOnline() {
            return isOnline;
        }

        public int getVideo() {
            return video;
        }

        public int getWithdrawalRcoin() {
            return withdrawalRcoin;
        }

        public Notification getNotification() {
            return notification;
        }

        public int getSpentCoin() {
            return spentCoin;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getAnalyticDate() {
            return analyticDate;
        }

        public int getPost() {
            return post;
        }

        public String getIdentity() {
            return identity;
        }

        public String getReferralCode() {
            return referralCode;
        }

        public Plan getPlan() {
            return plan;
        }

        public String getFcmToken() {
            return fcmToken;
        }

        public String getEmail() {
            return email;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getImage() {
            return image;
        }

        public boolean isIsBusy() {
            return isBusy;
        }

        public Ad getAd() {
            return ad;
        }

        public Level getLevel() {
            return level;
        }

        public String getIp() {
            return ip;
        }

        public boolean isIsVIP() {
            return isVIP;
        }

        public String getToken() {
            return token;
        }

        public int getDiamond() {
            return diamond;
        }

        public int getFollowers() {
            return followers;
        }

        public int getFollowing() {
            return following;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public int getAge() {
            return age;
        }

        public String getUsername() {
            return username;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public String getCoverImage() {
            return coverImage;
        }

        public void setCoverImage(String coverImage) {
            this.coverImage = coverImage;
        }

        public String getCountryFlagImage() {
            return countryFlagImage;
        }

        public void setCountryFlagImage(String countryFlagImage) {
            this.countryFlagImage = countryFlagImage;
        }
    }
}