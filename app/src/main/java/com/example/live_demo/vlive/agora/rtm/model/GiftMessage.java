package com.example.live_demo.vlive.agora.rtm.model;

public class GiftMessage {
    public GiftMessageData data;

    public static class GiftMessageData {
        public String fromUserId;
        public String fromUserName;
        public String toUserId;
        public String toUserName;
        public int giftId;
    }
}
