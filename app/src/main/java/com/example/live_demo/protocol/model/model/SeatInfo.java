package com.example.live_demo.protocol.model.model;

public class SeatInfo {
    public static final int OPEN = 0;
    public static final int TAKEN = 1;
    public static final int CLOSE = 2;

    public Seat seat;
    public User user;

    public static class Seat {
        public int no;
        public int state;   //trạng thái
    }

    public static class User {
        public static final int USER_VIDEO_MUTED = 0;   //tắt tiếng video
        public static final int USER_VIDEO_ENABLE = 1;  //cho phép video
        public static final int USER_AUDIO_MUTED = 0;   //tắt tiếng nhạc
        public static final int USER_AUDIO_ENABLE = 1;  //cho phép nhạc
        public static final int OWNER_AUDIO_MUTED = 2;  //tắt tiếng nhạc chủ phòng

        public String userId;
        public String userName;
        public int uid;
        public int enableVideo;
        public int enableAudio;
    }
}
