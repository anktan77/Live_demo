package com.example.live_demo.protocol.model.response;

import com.example.live_demo.protocol.model.model.EnterRoomUserInfo;
import com.example.live_demo.protocol.model.model.SeatInfo;

import java.util.List;

public class EnterRoomResponse extends Response{
    public RoomData data;

    public static class RoomData {
        public RoomInfo room;
        public EnterRoomUserInfo user;
    }

    public static class RoomInfo {
        public String roomId;
        public String roomName;
        public String channelName;
        public int type;
        public List<SeatInfo> coVideoSeats;
        public List<RankInfo> rankUsers;
        public int roomRank;
        public int currentUsers;
        public Owner owner;
        public PkInfo pk;
    }

    public static class RankInfo {
        public String userId;
        public String userName;
        public String avatar;
    }

    public static class Owner {
        public String userId;
        public int uid;
        public String userName;
        public int enableVideo;
        public int enableAudio;
    }

    public static class PkInfo {
        public int state;
        public long startTime;
        public long countDown;
        public int remoteRank;
        public int localRank;
        public RemoteRoom remoteRoom;   //phòng từ xa
        public RelayConfig relayConfig; //cấu hình chuyển tiếp
    }

    public static class RemoteRoom {
        public String roomId;
        public String channelName;
        public Owner owner;
    }

    public static class RelayConfig {
        public RelayInfo local;
        public RelayInfo proxy;     //ủy quyền
        public RelayInfo remote;    //từ xa
    }

    public static class RelayInfo {
        public String channelName;
        public String token;
        public int uid;
    }
}
