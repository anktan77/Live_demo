package com.example.live_demo.protocol.model.body;

public class SendGiftBody {
    int giftId;
    int count;

    public SendGiftBody(int id, int count) {
        this.giftId = id;
        this.count = count;
    }
}
