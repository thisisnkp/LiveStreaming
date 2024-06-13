package com.example.rayzi.modelclass;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GiftRoot {

    @SerializedName("gift")
    private List<GiftItem> gift;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<GiftItem> getGift() {
        return gift;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class GiftItem {

        @SerializedName("image")
        private String image;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("_id")
        private String id;

        @SerializedName("type")
        private int type;

        @SerializedName("category")
        private String category;

        @SerializedName("coin")
        private int coin;

        @SerializedName("svgaImage")
        private String svgaImage;
        private int count;
        @SerializedName("updatedAt")
        private String updatedAt;

        public GiftItem(String image, String createdAt, String id, int type, String category, int coin, int count, String updatedAt) {
            this.image = image;
            this.createdAt = createdAt;
            this.id = id;
            this.type = type;
            this.category = category;
            this.coin = coin;
            this.count = count;
            this.updatedAt = updatedAt;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getImage() {
            return image;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getId() {
            return id;
        }

        public int getType() {
            return type;
        }

        public String getCategory() {
            return category;
        }

        public int getCoin() {
            return coin;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getSvgaImage() {
            return svgaImage;
        }

        @Override
        public String toString() {
            return "GiftItem{" +
                    "image='" + image + '\'' +
                    ", createdAt='" + createdAt + '\'' +
                    ", id='" + id + '\'' +
                    ", type=" + type +
                    ", category='" + category + '\'' +
                    ", coin=" + coin +
                    ", count=" + count +
                    ", updatedAt='" + updatedAt + '\'' +
                    '}';
        }
    }
}