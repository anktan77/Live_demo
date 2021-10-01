package com.example.live_demo.protocol.model.response;

import com.example.live_demo.protocol.model.model.RoomInfo;

import java.util.List;

public class RoomListResponse extends Response{
    public RoomList data;

    public static class RoomList {
        public int count;
        public int total;
        public String nextId;
        public List<RoomInfo> list;
    }
}
