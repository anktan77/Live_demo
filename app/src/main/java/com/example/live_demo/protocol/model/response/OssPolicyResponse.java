package com.example.live_demo.protocol.model.response;

public class OssPolicyResponse extends Response{
    //trả về chính sách Oss
    public OssPolicyInfo data;

    public class OssPolicyInfo {
        public String accessKey;    //Khóa truy cập
        public String host;         // tổ chức
        public String policy;       // chính sách
        public String signature;    //chữ ký
        public long expire;         // hết hạn
        public String callback;     // gọi lại
        public String dir;
    }
}
