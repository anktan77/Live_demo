package com.example.live_demo;

import com.example.live_demo.protocol.model.response.AppVersionResponse;
import com.example.live_demo.protocol.model.response.AudienceListResponse;
import com.example.live_demo.protocol.model.response.CreateRoomResponse;
import com.example.live_demo.protocol.model.response.CreateUserResponse;
import com.example.live_demo.protocol.model.response.EditUserResponse;
import com.example.live_demo.protocol.model.response.EnterRoomResponse;
import com.example.live_demo.protocol.model.response.GiftListResponse;
import com.example.live_demo.protocol.model.response.GiftRankResponse;
import com.example.live_demo.protocol.model.response.LeaveRoomResponse;
import com.example.live_demo.protocol.model.response.LoginASPResponse;
import com.example.live_demo.protocol.model.response.LoginResponse;
import com.example.live_demo.protocol.model.response.ModifyUserStateResponse;
import com.example.live_demo.protocol.model.response.MusicListResponse;
import com.example.live_demo.protocol.model.response.OssPolicyResponse;
import com.example.live_demo.protocol.model.response.ProductListResponse;
import com.example.live_demo.protocol.model.response.RefreshTokenResponse;
import com.example.live_demo.protocol.model.response.RoomListResponse;
import com.example.live_demo.protocol.model.response.SeatStateResponse;
import com.example.live_demo.protocol.model.response.SendGiftResponse;

public interface ClientProxyListener {
    void onLoginASPRespone(LoginASPResponse response);

    void onAppVersionResponse(AppVersionResponse response);

    void onRefreshTokenResponse(RefreshTokenResponse refreshTokenResponse);

    void onOssPolicyResponse(OssPolicyResponse response);

    void onMusicLisResponse(MusicListResponse response);

    void onGiftListResponse(GiftListResponse response);

    void onRoomListResponse(RoomListResponse response);

    void onCreateUserResponse(CreateUserResponse response);

    void onEditUserResponse(EditUserResponse response);

    void onLoginResponse(LoginResponse response);

    void onCreateRoomResponse(CreateRoomResponse response);

    void onEnterRoomResponse(EnterRoomResponse response);

    void onLeaveRoomResponse(LeaveRoomResponse response);

    void onAudienceListResponse(AudienceListResponse response);

    void onRequestSeatStateResponse(SeatStateResponse response);

    void onModifyUserStateResponse(ModifyUserStateResponse response);

    void onSendGiftResponse(SendGiftResponse response);

    void onGiftRankResponse(GiftRankResponse response);

    void onGetProductListResponse(ProductListResponse response);

    void onProductStateChangedResponse(String productId, int state, boolean success);

    void onProductPurchasedResponse(boolean success);

    void onSeatInteractionResponse(long processId, String userId, int seatNo, int type);

    void onResponseError(int requestType, int error, String message);
}
