package com.example.live_demo.protocol.model.body;

public class PurchaseProductBody {
    public String productId;
    public int count;

    public PurchaseProductBody(String productId, int count) {
        this.productId = productId;
        this.count = count;
    }
}
