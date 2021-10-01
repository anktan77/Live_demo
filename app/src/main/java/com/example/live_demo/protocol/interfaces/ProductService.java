package com.example.live_demo.protocol.interfaces;


import com.example.live_demo.protocol.model.body.PurchaseProductBody;
import com.example.live_demo.protocol.model.response.BooleanResponse;
import com.example.live_demo.protocol.model.response.ProductListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ProductService {
    @GET("ent/v1/room/{roomId}/commerce/products")
    Call<ProductListResponse> requestProductList(@Header("token") String token, @Path("roomId") String roomId);

    @POST("ent/v1/room/{roomId}/commerce/products/{productId}/state/{state}")
    Call<BooleanResponse> requestManageProductState(@Header("token") String token, @Path("roomId") String roomId,
                                                    @Path("productId") String productId, @Path("state") int state);

    @POST("ent/v1/room/{roomId}/commerce/purchase")
    Call<BooleanResponse> requestPurchaseProduct(@Header("token") String token, @Path("roomId") String roomId,
                                                 @Body PurchaseProductBody body);
}