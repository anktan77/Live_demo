package com.example.live_demo.protocol.interfaces;

import com.example.live_demo.protocol.model.body.CreateRoomRequestBody;
import com.example.live_demo.protocol.model.body.ModifyUserStateRequestBody;
import com.example.live_demo.protocol.model.body.SendGiftBody;
import com.example.live_demo.protocol.model.response.AudienceListResponse;
import com.example.live_demo.protocol.model.response.CreateRoomResponse;
import com.example.live_demo.protocol.model.response.EnterRoomResponse;
import com.example.live_demo.protocol.model.response.GiftRankResponse;
import com.example.live_demo.protocol.model.response.LeaveRoomResponse;
import com.example.live_demo.protocol.model.response.ModifyUserStateResponse;
import com.example.live_demo.protocol.model.response.RoomListResponse;
import com.example.live_demo.protocol.model.response.SeatStateResponse;
import com.example.live_demo.protocol.model.response.SendGiftResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RoomService {
    @POST("ent/v1/room")
    Call<CreateRoomResponse> requestCreateLiveRoom(@Header("token") String token, @Header("reqId") long reqId,
                                                   @Header("reqType") int reqType, @Body CreateRoomRequestBody body);

    @POST("ent/v1/room/{roomId}/entry")
    Call<EnterRoomResponse> requestEnterLiveRoom(@Header("token") String token, @Header("reqId") long reqId,
                                                 @Header("reqType") int reqType, @Path("roomId") String roomId);

    @POST("ent/v1/room/{roomId}/exit")
    Call<LeaveRoomResponse> requestLeaveLiveRoom(@Header("token") String token, @Header("reqId") long reqId,
                                                 @Header("reqType") int reqType, @Path("roomId") String roomId);

    @GET("ent/v1/room/{roomId}/user/page")
    Call<AudienceListResponse> requestAudienceList(@Header("token") String token, @Header("reqId") long reqId,
                                                   @Header("reqType") int reqType, @Path("roomId") String roomId,
                                                   @Query("nextId") String nextId, @Query("count") int count,
                                                   @Query("type") int type);

    @GET("ent/v1/room/{roomId}/seats")
    Call<SeatStateResponse> requestSeatState(@Header("token") String token, @Header("reqId") long reqId,
                                             @Header("reqType") int reqType, @Path("roomId") String roomId);

    @POST("ent/v1/room/{roomId}/user/{userId}")
    Call<ModifyUserStateResponse> requestModifyUserState(@Header("token") String token, @Path("roomId") String roomId,
                                                         @Path("userId") String userId, @Body ModifyUserStateRequestBody body);

    @POST("ent/v1/room/{roomId}/gift")
    Call<SendGiftResponse> requestSendGift(@Header("token") String token, @Header("reqId") long reqId,
                                           @Header("reqType") int reqType, @Path("roomId") String roomId,
                                           @Body SendGiftBody body);

    @GET("ent/v1/room/{roomId}/ranks")
    Call<GiftRankResponse> requestGiftRank(@Header("reqId") long reqId, @Header("reqType") int reqType,
                                           @Path("roomId") String roomId);

    @GET("ent/v1/room/page")
    Call<RoomListResponse> requestRoomList(@Header("reqId") long reqId, @Header("token") String token,
                                           @Header("reqType") int reqType, @Query("nextId") String nextId,
                                           @Query("count") int count, @Query("type") int type,
                                           @Query("pkState") Integer pkState);
}
