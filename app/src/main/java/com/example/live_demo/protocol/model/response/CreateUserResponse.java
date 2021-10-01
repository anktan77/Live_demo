package com.example.live_demo.protocol.model.response;

public class CreateUserResponse extends Response{
    public CreateUserInfo data;

    public class CreateUserInfo {
        public String userId;
    }
}
