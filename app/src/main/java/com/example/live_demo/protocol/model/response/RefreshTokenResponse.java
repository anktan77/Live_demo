package com.example.live_demo.protocol.model.response;

public class RefreshTokenResponse extends Response{
    //Làm mới trả về mã thông báo
    public TokenData data;

    public class TokenData {
        public String rtcToken;
        public String rtmToken;
    }
}
