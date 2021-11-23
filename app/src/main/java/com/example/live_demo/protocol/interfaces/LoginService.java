package com.example.live_demo.protocol.interfaces;

import android.text.Editable;

import com.example.live_demo.protocol.model.body.CommitGiftBody;
import com.example.live_demo.protocol.model.request.LoginASPRequest;
import com.example.live_demo.protocol.model.response.LoginASPResponse;
import com.example.live_demo.protocol.model.response.PostResponse;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LoginService {
    @POST("api/customers/PostCustomer/")
    Call<LoginASPResponse> requestLoginASP(@Body LoginASPRequest body);

    @GET("api/customers/GetCustomer/")
    Call<LoginASPResponse> requestUser(@Query("email") String email);

    @POST("api/customers/CommitGift/")
    Call<Integer> requestCommitGift(@Body CommitGiftBody body);

    @POST("api/customers/RechargeCoin/")
    Call<Integer> requestRechargeCoin(@Query("email") String email, @Query("rechargeCoin") int rechargeCoin);

    @POST("api/customers/EditName/")
    Call<Integer> requestEditName(@Query("email") String email, @Query("editName") String editName);
}
