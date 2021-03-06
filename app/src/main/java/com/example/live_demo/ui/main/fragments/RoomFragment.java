package com.example.live_demo.ui.main.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.live_demo.R;
import com.example.live_demo.protocol.model.response.LoginASPResponse;
import com.example.live_demo.ui.live.LivePrepareActivity;
import com.example.live_demo.ui.live.SingleHostLiveActivity;
import com.example.live_demo.ui.live.VirtualImageSelectActivity;
import com.example.live_demo.utils.Global;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;

public class RoomFragment extends AbstractFragment implements View.OnClickListener {
    private static final int TAB_COUNT = 3; //gốc là 5
    private static final int TAB_TEXT_VIEW_INDEX = 1;

    private int mCurrentTap;
    private TabLayout mTabLayout;
    private String[] mTabTitles = new String[TAB_COUNT];

    @SuppressWarnings("unchecked")
    private SoftReference<TextView>[] mTabTexts =
            (SoftReference<TextView>[]) Array.newInstance(SoftReference.class, TAB_COUNT);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            // nhận dữ liệu từ home fragment xem là single host hay multi host
            mCurrentTap = bundle.getInt(Global.Constants.TAB_KEY);
        } else {
            // lấy multi host
            mCurrentTap = application().config().lastTabPosition();
        }

        View view = inflater.inflate(R.layout.fragment_room, container, false);

        // lấy dữ liệu cho Tab trên cùng
        getTabTitles();

        mTabLayout = view.findViewById(R.id.room_tab_layout);

        // ViewPager2: trong room fragment có thể chuyển sang tab layout solo host, multi host
        ViewPager2 viewPager = view.findViewById(R.id.room_list_pager);
        viewPager.setAdapter(new RoomAdapter(this));

        // TabLayoutMediator: sử dụng với ViewPager2. nó sẽ chạy song song khi vuốt tab layout hoặc list pager
        // hợp nhất Viewpage với tab layout
        new TabLayoutMediator(mTabLayout, viewPager, (tab, position) ->
                tab.setText(mTabTitles[position])).attach();

        TabLayout.Tab tab = mTabLayout.getTabAt(mCurrentTap);
        if (tab != null) {
            // When we navigate to this fragment from home fragment,
            // it's better not to switch to this fragment smoothly.
            viewPager.setCurrentItem(mCurrentTap, false);
            application().config().setLastTabPosition(mCurrentTap);
            setTextViewBold(getCachedTabText(tab), true);
        }

        // Note tab selected listener should be set after
        // tab layout and view pager are attached together.
        // Before that, it cannot know how many tabs are there.
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mCurrentTap = tab.getPosition();
                application().config().setLastTabPosition(mCurrentTap);
                setTextViewBold(getCachedTabText(tab), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setTextViewBold(getCachedTabText(tab), false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        view.findViewById(R.id.live_room_start_broadcast).setOnClickListener(this);

        return view;
    }

    // lấy dữ liệu tên tab menu trên đầu
    private void getTabTitles() {
        mTabTitles = new String[TAB_COUNT];
        for (int i = 0; i < TAB_COUNT; i++) {
            mTabTitles[i] = getResources().getString(Global.Constants.TAB_IDS_RES[i]);
        }
    }

    private TextView findTabTextView(@NonNull TabLayout.Tab tab) {
        View view = tab.view.getChildAt(TAB_TEXT_VIEW_INDEX);
        return view == null ? null :
                view instanceof TextView ? (TextView) view : null;
    }

    private TextView getCachedTabText(@NonNull TabLayout.Tab tab) {
        int position = tab.getPosition();
        if (position < 0 || position >= TAB_COUNT) return null;

        if (mTabTexts[position] == null || mTabTexts[position].get() == null) {
            mTabTexts[position] = new SoftReference<>(findTabTextView(tab));
        }

        return mTabTexts[position].get();
    }

    private void setTextViewBold(TextView view, boolean bold) {
        if (view == null) return;
        Typeface typeface = bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT;
        view.setTypeface(typeface);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mTabLayout != null) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) mTabLayout.getLayoutParams();
            int systemBarHeight = getContainer().getSystemBarHeight();
            params.topMargin += systemBarHeight;
            mTabLayout.setLayoutParams(params);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.live_room_start_broadcast) {
            if (config().appIdObtained()) {
                Class<?> activity = mCurrentTap == Global.Constants.TAB_ID_VIRTUAL ?
                        VirtualImageSelectActivity.class :
                        LivePrepareActivity.class;
                Intent intent = new Intent(getActivity(), activity);
                intent.putExtra(Global.Constants.TAB_KEY, mCurrentTap + 1);
                //gửi qua xác nhận là phòng mình tạo
                intent.putExtra(Global.Constants.KEY_IS_ROOM_OWNER, true);
                intent.putExtra(Global.Constants.KEY_CREATE_ROOM, true);
                //chuyển token của user
                intent.putExtra(Global.Constants.KEY_ROOM_OWNER_ID,
                        getContainer().config().getUserProfile().getUserId());
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), R.string.agora_app_id_failed,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLoginASPRespone(LoginASPResponse response) {

    }

    private static class RoomAdapter extends FragmentStateAdapter {
        RoomAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {

            // return qua bên fragment nào
            // thì onGetRoomListType ở AbsPageFragment bằng mã số multi or single ấy
            switch (position) {
                case 1: return new SingleHostFragment();
//                case 2: return new PKHostInFragment();
//                case 3: return new VirtualHostFragment();
//                case 4: return new ECommerceFragment();
                default: return new HostInFragment();
            }
        }

        @Override
        public int getItemCount() {
            return TAB_COUNT;
        }
    }
}