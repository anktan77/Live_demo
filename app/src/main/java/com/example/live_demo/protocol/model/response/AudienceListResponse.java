package com.example.live_demo.protocol.model.response;

import java.util.List;

public class AudienceListResponse extends Response{
    //danh sách người xem
    public RoomProfile data;

    public class RoomProfile {
        public int count;
        public int total;
        public String next;
        public List<AudienceInfo> list;
    }

    //thông tin người xem
    public class AudienceInfo {
        public String userId;
        public String userName;
        public String avatar;
        public String uid;
    }
}
