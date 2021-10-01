package com.example.live_demo.protocol.manager;

import android.text.TextUtils;

import com.elvishew.xlog.XLog;
import com.example.live_demo.protocol.model.request.ProductRequest;
import com.example.live_demo.protocol.model.request.Request;
import com.example.live_demo.utils.AgoraLiveApplication;


public class ProductServiceManager {
    private AgoraLiveApplication mApplication;

    public ProductServiceManager(AgoraLiveApplication application) {
        mApplication = application;
    }

    private String getValidToken(String errorMessage) {
        String token = mApplication.config().getUserProfile().getToken();
        if (TextUtils.isEmpty(token)) {
            XLog.e(errorMessage);
            token = null;
        }

        return token;
    }

    public void requestProductList(String roomId) {
        String token = getValidToken("ProductManager product list token invalid");
        if (token != null) {
            mApplication.proxy().sendRequest(Request.PRODUCT_LIST,
                    new ProductRequest(token, roomId, null, 0, 0));
        }
    }

    public void requestPurchaseProduct(String roomId, String productId, int count) {
        String token = getValidToken("ProductManager product purchase token invalid");
        if (token != null) {
            mApplication.proxy().sendRequest(Request.PRODUCT_PURCHASE,
                    new ProductRequest(token, roomId, productId, count, 0));
        }
    }

    public void requestChangeProductState(String roomId, String productId, int state) {
        String token = getValidToken("ProductManager product state token invalid");
        if (token != null) {
            mApplication.proxy().sendRequest(Request.PRODUCT_MANAGE,
                    new ProductRequest(token, roomId, productId, 0, state));
        }
    }
}
