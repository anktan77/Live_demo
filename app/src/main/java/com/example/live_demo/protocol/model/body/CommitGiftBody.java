package com.example.live_demo.protocol.model.body;

public class CommitGiftBody {
    String fromUser;
    String toUser;
    int giftCoin;

    public CommitGiftBody(String fromUser, String toUser, int giftCoin) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.giftCoin = giftCoin;
    }
}
