package com.example.live_demo.ui.main.fragments;

import androidx.fragment.app.Fragment;

import com.example.live_demo.ClientProxyListener;
import com.example.live_demo.protocol.model.response.AppVersionResponse;
import com.example.live_demo.protocol.model.response.AudienceListResponse;
import com.example.live_demo.protocol.model.response.CreateRoomResponse;
import com.example.live_demo.protocol.model.response.CreateUserResponse;
import com.example.live_demo.protocol.model.response.EditUserResponse;
import com.example.live_demo.protocol.model.response.EnterRoomResponse;
import com.example.live_demo.protocol.model.response.GiftListResponse;
import com.example.live_demo.protocol.model.response.GiftRankResponse;
import com.example.live_demo.protocol.model.response.LeaveRoomResponse;
import com.example.live_demo.protocol.model.response.LoginResponse;
import com.example.live_demo.protocol.model.response.ModifyUserStateResponse;
import com.example.live_demo.protocol.model.response.MusicListResponse;
import com.example.live_demo.protocol.model.response.OssPolicyResponse;
import com.example.live_demo.protocol.model.response.ProductListResponse;
import com.example.live_demo.protocol.model.response.RefreshTokenResponse;
import com.example.live_demo.protocol.model.response.RoomListResponse;
import com.example.live_demo.protocol.model.response.SeatStateResponse;
import com.example.live_demo.protocol.model.response.SendGiftResponse;
import com.example.live_demo.ui.main.MainActivity;
import com.example.live_demo.utils.AgoraLiveApplication;
import com.example.live_demo.vlive.Config;

public abstract class AbstractFragment extends Fragment implements ClientProxyListener {
    protected AgoraLiveApplication application() {
        return (AgoraLiveApplication) getContext().getApplicationContext();
    }

    MainActivity getContainer() {
        return (MainActivity) getActivity();
    }

    protected Config config() {
        return application().config();
    }

    @Override
    public void onAppVersionResponse(AppVersionResponse response) {

    }

    @Override
    public void onRefreshTokenResponse(RefreshTokenResponse refreshTokenResponse) {

    }

    @Override
    public void onOssPolicyResponse(OssPolicyResponse response) {

    }

    @Override
    public void onMusicLisResponse(MusicListResponse response) {

    }

    @Override
    public void onGiftListResponse(GiftListResponse response) {

    }

    @Override
    public void onRoomListResponse(RoomListResponse response) {

    }

    @Override
    public void onCreateUserResponse(CreateUserResponse response) {

    }

    @Override
    public void onLoginResponse(LoginResponse response) {

    }

    @Override
    public void onEditUserResponse(EditUserResponse response) {

    }

    @Override
    public void onCreateRoomResponse(CreateRoomResponse response) {

    }

    @Override
    public void onEnterRoomResponse(EnterRoomResponse response) {

    }

    @Override
    public void onLeaveRoomResponse(LeaveRoomResponse response) {

    }

    @Override
    public void onAudienceListResponse(AudienceListResponse response) {

    }

    @Override
    public void onRequestSeatStateResponse(SeatStateResponse response) {

    }

    @Override
    public void onModifyUserStateResponse(ModifyUserStateResponse response) {

    }

    @Override
    public void onSendGiftResponse(SendGiftResponse response) {

    }

    @Override
    public void onGiftRankResponse(GiftRankResponse response) {

    }

    @Override
    public void onGetProductListResponse(ProductListResponse response) {

    }

    @Override
    public void onProductStateChangedResponse(String productId, int state, boolean success) {

    }

    @Override
    public void onProductPurchasedResponse(boolean success) {

    }

    @Override
    public void onSeatInteractionResponse(long processId, String userId, int seatNo, int type) {

    }

    @Override
    public void onResponseError(int requestType, int error, String message) {

    }
}
