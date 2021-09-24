package com.example.live_demo;

public interface ClientProxyListener {
//    void onAppVersionResponse(AppVersionResponse response);
//
//    void onRefreshTokenResponse(RefreshTokenResponse refreshTokenResponse);
//
//    void onOssPolicyResponse(OssPolicyResponse response);
//
//    void onMusicLisResponse(MusicListResponse response);
//
//    void onGiftListResponse(GiftListResponse response);
//
//    void onRoomListResponse(RoomListResponse response);
//
//    void onCreateUserResponse(CreateUserResponse response);
//
//    void onEditUserResponse(EditUserResponse response);
//
//    void onLoginResponse(LoginResponse response);
//
//    void onCreateRoomResponse(CreateRoomResponse response);
//
//    void onEnterRoomResponse(EnterRoomResponse response);
//
//    void onLeaveRoomResponse(LeaveRoomResponse response);
//
//    void onAudienceListResponse(AudienceListResponse response);
//
//    void onRequestSeatStateResponse(SeatStateResponse response);
//
//    void onModifyUserStateResponse(ModifyUserStateResponse response);
//
//    void onSendGiftResponse(SendGiftResponse response);
//
//    void onGiftRankResponse(GiftRankResponse response);
//
//    void onGetProductListResponse(ProductListResponse response);

    void onProductStateChangedResponse(String productId, int state, boolean success);

    void onProductPurchasedResponse(boolean success);

    void onSeatInteractionResponse(long processId, String userId, int seatNo, int type);

    void onResponseError(int requestType, int error, String message);
}
