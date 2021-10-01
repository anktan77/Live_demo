package com.example.live_demo.protocol.model.response;

import java.util.List;

public class SeatStateResponse extends Response{
    //trạng thái chỗ ngồi
    public List<SeatInfo> data;

    public class SeatInfo {
        public int no;
        public String userId;
        public String userName;
        public int uid;
        public int state;
    }
}
