package com.example.live_demo.protocol.interfaces;


import com.example.live_demo.protocol.model.body.PkRequestBody;
import com.example.live_demo.protocol.model.response.BooleanResponse;
import com.example.live_demo.protocol.model.response.LongResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PKService {
    @POST("ent/v1/room/{roomId}/pk/notice")
    Call<LongResponse> requestPKBehavior(@Header("token") String token, @Path("roomId") String myRoomId, @Body PkRequestBody body);

    @POST("ent/v1/room/{roomId}/pk/end")
    Call<BooleanResponse> requestPKEnd(@Header("token") String token, @Path("roomId") String myRoomId, @Body PkRequestBody body);

}
