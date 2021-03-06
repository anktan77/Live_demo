package com.example.live_demo.ui.main.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.live_demo.R;
import com.example.live_demo.protocol.model.ClientProxy;
import com.example.live_demo.protocol.model.model.RoomInfo;
import com.example.live_demo.protocol.model.request.Request;
import com.example.live_demo.protocol.model.request.RoomListRequest;
import com.example.live_demo.protocol.model.response.RoomListResponse;
import com.example.live_demo.ui.components.SquareRelativeLayout;
import com.example.live_demo.utils.Global;
import com.example.live_demo.utils.UserUtil;
import com.example.live_demo.vlive.Config;

import java.util.ArrayList;
import java.util.List;



public abstract class AbsPageFragment extends AbstractFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = AbsPageFragment.class.getSimpleName();

    private static final int SPAN_COUNT = 2;
    private static final int REFRESH_DELAY = 1000 * 60;

    // Theo mặc định, khách hàng yêu cầu thêm 10 phòng để hiển thị trong danh sách
    private static final int REQ_ROOM_COUNT = 10;

    private Handler mHandler;
    private PageRefreshRunnable mPageRefreshRunnable;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RoomListAdapter mAdapter;
    private View mNoDataBg;
    private View mNetworkErrorBg;

    private int mItemSpacing;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        mPageRefreshRunnable = new PageRefreshRunnable();
        mItemSpacing = getContainer().getResources()
                .getDimensionPixelSize(R.dimen.activity_horizontal_margin);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_room_list, container, false);
        mSwipeRefreshLayout = layout.findViewById(R.id.host_in_swipe);
        mSwipeRefreshLayout.setNestedScrollingEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = layout.findViewById(R.id.host_in_room_list_recycler);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), SPAN_COUNT));
        mAdapter = new RoomListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new RoomListItemDecoration());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    stopRefreshTimer();
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        // Bố cục vuốt được làm mới khi
                        // chúng tôi muốn làm mới toàn bộ trang.
                        // Trong trường hợp này, chúng tôi sẽ làm mới
                        // người nghe để xử lý tất cả công việc.
                        return;
                    }

                    startRefreshTimer();
                    int lastItemPosition = recyclerView.getChildAdapterPosition(
                            recyclerView.getChildAt(recyclerView.getChildCount() - 1));
                    if (lastItemPosition == recyclerView.getAdapter().getItemCount() - 1) {
                        refreshPage(mAdapter.getLast() == null ? null : mAdapter.getLast().roomId);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mNoDataBg = layout.findViewById(R.id.no_data_bg);

        //hiển thị toast nếu lỗi mạng
        mNetworkErrorBg = layout.findViewById(R.id.network_error_bg);
        mNetworkErrorBg.setVisibility(View.GONE);

        checkRoomListEmpty();

        return layout;
    }

    private void checkRoomListEmpty() {
        mRecyclerView.setVisibility(mAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        mNoDataBg.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void startRefreshTimer() {
        mHandler.postDelayed(mPageRefreshRunnable, REFRESH_DELAY);
    }

    private void stopRefreshTimer() {
        mHandler.removeCallbacks(mPageRefreshRunnable);
    }

    // Runnable chạy đa luồng, vào là chạy
    private class PageRefreshRunnable implements Runnable {
        @Override
        public void run() {
            onPeriodicRefreshTimerTicked();
            mHandler.postDelayed(mPageRefreshRunnable, REFRESH_DELAY);
        }
    }

    private void onPeriodicRefreshTimerTicked() {
        refreshPage(null);
    }

    @Override
    public void onRefresh() {
        refreshPage(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        startRefreshTimer();
        getContainer().proxy().registerProxyListener(this);
        refreshPage(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRefreshTimer();
        getContainer().proxy().removeProxyListener(this);
    }

//    / **
//            * Làm mới trang từ sau một phòng cụ thể.
//      * @param nextId null nếu làm mới từ đầu danh sách
//      * /
    private void refreshPage(String nextId) {
        if (nextId == null) mAdapter.clear(false);
        refreshPage(nextId, REQ_ROOM_COUNT, onGetRoomListType(), null);
    }
    // trả về nextid == roomId
    // quan trọng nhất là trả về roomId và roomName để bỏ vào recyclerView
    private void refreshPage(String nextId, int count, int type, Integer pkState) {
        // lấy token của user
        RoomListRequest request = new RoomListRequest(
                getContainer().config().getUserProfile().getToken(),
                nextId, count, type, pkState);

        // gửi qua Client để request api
        getContainer().proxy().sendRequest(Request.ROOM_LIST, request);
    }

    // trả về các room đang live
    // nó xử lý đa luồng, nó sự cùng thực hiện khi khởi động ứng dụng
    // cùng thực hiện tại 1 thời điểm
    @Override
    public void onRoomListResponse(RoomListResponse response) {
        final List<RoomInfo> list = response.data.list;

        // runOnUiThread thực hiện đa luồng, cùng run khi load main activity
        getContainer().runOnUiThread(() -> {
            mNetworkErrorBg.setVisibility(View.GONE);

            // nextid == roomId
            // add vào adapter, TextUtils.isEmpty(response.data.nextId) kiểm tra nextId có rỗng k
            mAdapter.append(list, TextUtils.isEmpty(response.data.nextId));
            checkRoomListEmpty();
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private class RoomListAdapter extends RecyclerView.Adapter<RoomListItemViewHolder> {
        private List<RoomInfo> mRoomList = new ArrayList<>();

        @NonNull
        @Override
        public RoomListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RoomListItemViewHolder(LayoutInflater.from(getContext()).
                    inflate(R.layout.live_room_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RoomListItemViewHolder holder, final int position) {
            if (mRoomList.size() <= position) return;

            RoomInfo info = mRoomList.get(position);
            holder.name.setText(info.roomName);
            holder.count.setText(String.valueOf(info.currentUsers));
            holder.layout.setBackgroundResource(UserUtil.getUserProfileIcon(info.roomId));

            // GetRoomListType lấy từ HostInFragment or singlehostfragment
            holder.itemView.setOnClickListener((view) -> {
                if (config().appIdObtained() && position < mRoomList.size()) {
                    goLiveRoom(mRoomList.get(position),
                            serverTypeToTabType(onGetRoomListType()));
                } else {
                    Toast.makeText(getContext(), R.string.agora_app_id_failed,
                            Toast.LENGTH_SHORT).show();
                    checkUpdate();
                }
            });
        }

        private void checkUpdate() {
            if (!config().hasCheckedVersion()) {
                getContainer().proxy().sendRequest(
                        Request.APP_VERSION, getContainer().getAppVersion());
            }
        }

        private int serverTypeToTabType(int serverType) {
            switch (serverType) {
                case ClientProxy.ROOM_TYPE_SINGLE: return Config.LIVE_TYPE_SINGLE_HOST;
                case ClientProxy.ROOM_TYPE_PK: return Config.LIVE_TYPE_PK_HOST;
                case ClientProxy.ROOM_TYPE_VIRTUAL_HOST: return Config.LIVE_TYPE_VIRTUAL_HOST;
                case ClientProxy.ROOM_TYPE_ECOMMERCE: return Config.LIVE_TYPE_ECOMMERCE;
                case ClientProxy.ROOM_TYPE_HOST_IN:
                default: return Config.LIVE_TYPE_MULTI_HOST;
            }
        }

        @Override
        public int getItemCount() {
            return mRoomList.size();
        }

        void append(List<RoomInfo> infoList, boolean reset) {
            if (reset) mRoomList.clear();
            mRoomList.addAll(infoList);
            notifyDataSetChanged();
        }

        void clear(boolean notifyChange) {
            mRoomList.clear();
            if (notifyChange) notifyDataSetChanged();
        }

        RoomInfo getLast() {
            return mRoomList.isEmpty() ? null : mRoomList.get(mRoomList.size() - 1);
        }
    }

    // roomType là kiểu host hay multi
    private void goLiveRoom(RoomInfo info, int roomType) {
        Intent intent = new Intent(getActivity(), getLiveActivityClass());
        intent.putExtra(Global.Constants.TAB_KEY, roomType);
        intent.putExtra(Global.Constants.KEY_IS_ROOM_OWNER, false);
        intent.putExtra(Global.Constants.KEY_ROOM_NAME, info.roomName);
        intent.putExtra(Global.Constants.KEY_ROOM_ID, info.roomId);
        startActivity(intent);
    }

    private static class RoomListItemViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView count;
        AppCompatTextView name;
        SquareRelativeLayout layout;

        RoomListItemViewHolder(@NonNull View itemView) {
            super(itemView);
            count = itemView.findViewById(R.id.live_room_list_item_count);
            name = itemView.findViewById(R.id.live_room_list_item_room_name);
            layout = itemView.findViewById(R.id.live_room_list_item_background);
        }
    }

    private class RoomListItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            int position = parent.getChildAdapterPosition(view);
            int total = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();
            int half = mItemSpacing / 2;

            outRect.top = half;
            outRect.bottom = half;

            if (position < SPAN_COUNT) {
                outRect.top = mItemSpacing;
            } else {
                int remain = total % SPAN_COUNT;
                if (remain == 0) remain = SPAN_COUNT;
                if (position + remain >= total) {
                    outRect.bottom = mItemSpacing;
                }
            }

            if (position % SPAN_COUNT == 0) {
                outRect.left = mItemSpacing;
                outRect.right = mItemSpacing / 2;
            } else {
                outRect.left = mItemSpacing / 2;
                outRect.right = mItemSpacing;
            }
        }
    }

    protected abstract int onGetRoomListType();

    protected abstract Class<?> getLiveActivityClass();

    @Override
    public void onResponseError(int requestType, int error, String message) {
        getContainer().runOnUiThread(() -> {
            Toast.makeText(getContext(),
                    "request type: "+ Request.getRequestString(requestType) +
                            ", error message:" + message, Toast.LENGTH_LONG).show();

            if (requestType == Request.ROOM_LIST) {
                if (mAdapter != null) {
                    mAdapter.clear(true);
                }
                mNoDataBg.setVisibility(View.GONE);
                mNetworkErrorBg.setVisibility(View.VISIBLE);
            }
        });
    }
}
