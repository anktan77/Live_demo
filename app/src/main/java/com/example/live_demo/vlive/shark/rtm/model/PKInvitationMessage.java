package com.example.live_demo.vlive.shark.rtm.model;

public class PKInvitationMessage extends AbsRtmMessage{
    public PKMessageData data;
    public int version;

    public static class PKMessageData {
        public FromRoom fromRoom;
        public int type;
    }

    public class FromRoom {
        public String roomId;
        public String roomName;
        public Owner owner;
    }

    public class Owner {
        public String userId;
        public String userName;
        public int uid;
    }
}
