package com.example.live_demo.protocol.model.body;

public class RequestModifySeatStateBody {
    public int no;
    public int state;

    public RequestModifySeatStateBody(int no, int state) {
        this.no = no;
        this.state = state;
    }
}
