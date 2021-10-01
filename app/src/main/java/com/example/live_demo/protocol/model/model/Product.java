package com.example.live_demo.protocol.model.model;

public class Product {
    public static final int PRODUCT_LAUNCHED = 1;       //sản phẩm đã ra mắt
    public static final int PRODUCT_UNAVAILABLE = 0;    //sản phẩm không có sẵn

    public String productId;
    public String productName;
    public String description;
    public int price;
    public String thumbnail; //hình
    public int state;   //trạng thái

    public Product(Product product) {
        this.productId = product.productId;
        this.productName = product.productName;
        this.description = product.description;
        this.price = product.price;
        this.thumbnail = product.thumbnail;
        this.state = product.state;
    }
}
