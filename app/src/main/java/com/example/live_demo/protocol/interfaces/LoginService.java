package com.example.live_demo.protocol.interfaces;

import com.example.live_demo.protocol.model.request.LoginASPRequest;
import com.example.live_demo.protocol.model.response.LoginASPResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginService {
    @POST("api/customers/")
    Call<LoginASPResponse> requestLoginASP(@Body LoginASPRequest body);
}
